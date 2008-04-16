/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.osee.framework.jdk.core.util.OseeUser;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactTypeSearch;
import org.eclipse.osee.framework.skynet.core.artifact.search.Operator;
import org.eclipse.osee.framework.skynet.core.artifact.search.UserIdSearch;
import org.eclipse.osee.framework.skynet.core.attribute.ConfigurationPersistenceManager;
import org.eclipse.osee.framework.skynet.core.dbinit.SkynetDbInit;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.user.UserEnum;
import org.eclipse.osee.framework.skynet.core.user.UserNotInDatabase;
import org.eclipse.osee.framework.ui.plugin.event.AuthenticationEvent;
import org.eclipse.osee.framework.ui.plugin.security.AuthenticationDialog;
import org.eclipse.osee.framework.ui.plugin.security.OseeAuthentication;
import org.eclipse.osee.framework.ui.plugin.security.UserCredentials.UserCredentialEnum;
import org.eclipse.swt.widgets.Display;

/**
 * <b>Skynet Authentication</b><br/> Provides mapping of the current Authenticated User Id to its User Artifact in the
 * Skynet Database.
 * 
 * @author Roberto E. Escobar
 */
public class SkynetAuthentication implements PersistenceManager {
   private static final Logger logger = ConfigUtil.getConfigFactory().getLogger(SkynetAuthentication.class);
   private OseeAuthentication oseeAuthentication;
   private ArtifactPersistenceManager artifactManager;
   private BranchPersistenceManager branchManager;
   private int noOneArtifactId;

   public static enum UserStatusEnum {
      Active, InActive, Both
   }
   private boolean firstTimeThrough;
   private final Map<String, User> nameOrIdToUserCache;
   private final Map<Integer, User> artIdToUserCache;
   private final ArrayList<User> activeUserCache;
   private String[] activeUserNameCache;
   private User currentUser;
   private final Map<OseeUser, User> enumeratedUserCache;
   private boolean duringUserCreation;

   private static final SkynetAuthentication instance = new SkynetAuthentication();

   private SkynetAuthentication() {
      firstTimeThrough = true;
      enumeratedUserCache = new HashMap<OseeUser, User>(30);
      nameOrIdToUserCache = new HashMap<String, User>(800);
      artIdToUserCache = new HashMap<Integer, User>(800);
      activeUserCache = new ArrayList<User>(700);
   }

   public static SkynetAuthentication getInstance() {
      PersistenceManagerInit.initManagerWeb(instance);
      return instance;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.osee.framework.skynet.core.PersistenceManager#onManagerWebInit()
    */
   public void onManagerWebInit() throws Exception {
      artifactManager = ArtifactPersistenceManager.getInstance();
      oseeAuthentication = OseeAuthentication.getInstance();
      branchManager = BranchPersistenceManager.getInstance();
   }

   public boolean isAuthenticated() {
      return oseeAuthentication.isAuthenticated();
   }

   private void forceAuthenticationRoutine() {
      if (!oseeAuthentication.isAuthenticated()) {
         if (oseeAuthentication.isLoginAllowed()) {
            AuthenticationDialog.openDialog();
         } else {
            oseeAuthentication.authenticate("", "", "", false);
         }
         notifyListeners();
      }
   }

   private static void notifyListeners() {
      Display.getDefault().asyncExec(new Runnable() {
         public void run() {
            SkynetEventManager.getInstance().kick(new AuthenticationEvent(this));
         }
      });
   }

   public synchronized User getAuthenticatedUser() throws IllegalStateException {
      try {
         if (SkynetDbInit.isPreArtifactCreation()) {
            return BootStrapUser.getInstance();
         } else {
            if (firstTimeThrough) {
               forceAuthenticationRoutine();
            }

            if (!oseeAuthentication.isAuthenticated()) {
               currentUser = getUser(UserEnum.Guest);
            } else {
               String userId = oseeAuthentication.getCredentials().getField(UserCredentialEnum.Id);
               if (currentUser == null || !currentUser.getUserId().equals(userId)) {
                  try {
                     currentUser = getUserByIdWithError(userId);
                  } catch (UserNotInDatabase ex) {
                     currentUser =
                           createUser(oseeAuthentication.getCredentials().getField(UserCredentialEnum.Name),
                                 "spawnedBySkynet", userId, true);
                     persistUser(currentUser); // this is done outside of the crateUser call to avoid recursion
                  }
               }
            }
            firstTimeThrough = false; // firstTimeThrough must be set false after last use of its value
         }
      } catch (UserNotInDatabase ex) {
         logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      } catch (IllegalArgumentException ex) {
         logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      } catch (SQLException ex) {
         logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }

      return currentUser;
   }

   private void persistUser(User user) {
      duringUserCreation = true;
      try {
         user.persistAttributes();
         user.getLinkManager().persistLinks();
      } catch (SQLException ex) {
         duringUserCreation = false;
         logger.log(Level.SEVERE, ex.toString(), ex);
      }
   }

   public User createUser(OseeUser userEnum) {
      User user = createUser(userEnum.getName(), userEnum.getEmail(), userEnum.getUserID(), userEnum.isActive());
      persistUser(user);
      enumeratedUserCache.put(userEnum, user);
      return user;
   }

   public User getUser(OseeUser userEnum) throws IllegalArgumentException, SQLException, IllegalStateException, UserNotInDatabase {
      User user = enumeratedUserCache.get(userEnum);
      if (user == null) {
         user = getUserByIdWithError(userEnum.getUserID());
         enumeratedUserCache.put(userEnum, user);
      }
      return user;
   }

   public User createUser(String name, String email, String userID, boolean active) {
      duringUserCreation = true;
      User user = null;
      try {
         user =
               (User) ConfigurationPersistenceManager.getInstance().getArtifactSubtypeDescriptor(User.ARTIFACT_NAME).makeNewArtifact(
                     branchManager.getCommonBranch());
         user.setActive(active);
         user.setUserID(userID);
         user.setName(name);
         user.setEmail(email);
         addUserToMap(user);
         // this is here in case a user is created at an unexpected time
         if (!SkynetDbInit.isDbInit()) logger.log(Level.INFO, "Created user " + user, new Exception(
               "just wanted the stack trace"));
      } catch (SQLException ex) {
         logger.log(Level.WARNING, "Error Creating User.\n", ex);
      } finally {
         duringUserCreation = false;
      }
      return user;
   }

   /**
    * @return shallow copy of ArrayList of all active users in the datastore sorted by user name
    */
   @SuppressWarnings("unchecked")
   public ArrayList<User> getUsers() {
      if (activeUserCache.size() == 0) {
         try {
            Collection<Artifact> dbUsers =
                  artifactManager.getArtifacts(new ArtifactTypeSearch(User.ARTIFACT_NAME, Operator.EQUAL),
                        branchManager.getCommonBranch());
            for (Artifact a : dbUsers) {
               User user = (User) a;
               if (user.isActive()) {
                  activeUserCache.add(user);
               }
               addUserToMap(user);
            }
            Collections.sort(activeUserCache);
            int i = 0;
            activeUserNameCache = new String[activeUserCache.size()];
            for (User user : activeUserCache) {
               activeUserNameCache[i++] = user.getName();
            }
         } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error Searching for User in DB.\n", ex);
         }
      }

      return (ArrayList<User>) activeUserCache.clone();
   }

   public User getUserByIdWithError(String userId) throws SQLException, IllegalArgumentException, IllegalStateException, UserNotInDatabase {
      if (userId == null || userId.equals("")) {
         throw new IllegalArgumentException("UserId can't be null or \"\"");
      }
      User user = nameOrIdToUserCache.get(userId);

      if (user == null) {
         Collection<Artifact> users =
               artifactManager.getArtifacts(new UserIdSearch(userId, Operator.EQUAL), branchManager.getCommonBranch());
         if (users.size() == 1) {
            user = (User) users.iterator().next();
            addUserToMap(user);
         } else if (users.size() > 1) {
            for (Artifact duplicate : users) {
               logger.log(
                     Level.WARNING,
                     "Duplicate User userId: \"" + userId + "\" with the name: \"" + duplicate.getDescriptiveName() + "\"");
            }
            throw new IllegalStateException(String.format("User name: \"%s\" userId: \"%s\" in DB more than once",
                  users.iterator().next().getDescriptiveName(), userId));
         } else {
            // Note this is normal for the creation of this user (i.e. db init)
            throw new UserNotInDatabase("User requested by id \"" + userId + "\" was not found.");
         }
      }
      return user;
   }

   public User getUserById(String userId) throws SQLException, IllegalArgumentException, IllegalStateException {
      if (userId == null || userId.equals("")) throw new IllegalStateException("UserId can't be null or \"\"");
      User user = nameOrIdToUserCache.get(userId);

      if (user == null) {
         Collection<Artifact> users =
               artifactManager.getArtifacts(new UserIdSearch(userId, Operator.EQUAL), branchManager.getCommonBranch());
         if (users.size() == 1) {
            user = (User) users.iterator().next();
            addUserToMap(user);
         }
      }
      return user;
   }

   /**
    * Return sorted list of active User.getName() in database
    * 
    * @return String[]
    */
   public String[] getUserNames() {
      getUsers(); // ensure users are cached
      return activeUserNameCache;
   }

   /**
    * @param name
    * @param create if true, will create a temp user artifact; should only be used for dev purposes
    * @return user
    */
   public User getUserByName(String name, boolean create) {
      User user = nameOrIdToUserCache.get(name);
      if (user == null) {
         try {
            user =
                  (User) artifactManager.getArtifactFromTypeName(User.ARTIFACT_NAME, name,
                        branchManager.getCommonBranch());
         } catch (IllegalStateException ex) {
            if (create && ex.getLocalizedMessage().contains("There must be exactly one")) {
               user = createUser(name, "", name, true);
               try {
                  user.persistAttributes();
               } catch (SQLException ex2) {
                  logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex2);
               }
               addUserToMap(user);
               return user;
            }
         } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
         }
         if (user != null) addUserToMap(user);
      }
      return user;
   }

   public User getUserByArtId(int authorId) {
      User user = null;
      // Anything under 1 will never be acquirable
      if (authorId < 1) {
         return null;
      } else if (artIdToUserCache.containsKey(authorId)) {
         user = artIdToUserCache.get(authorId);
      } else {
         try {
            user = (User) artifactManager.getArtifactFromId(authorId, branchManager.getCommonBranch());
            addUserToMap(user);
         } catch (SQLException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            artIdToUserCache.put(authorId, null);
         } catch (IllegalArgumentException ex) {
            artIdToUserCache.put(authorId, null);
         }
      }
      return user;
   }

   private void addUserToMap(User user) {
      nameOrIdToUserCache.put(user.getDescriptiveName(), user);
      nameOrIdToUserCache.put(user.getUserId(), user);
      if (user.isInDb()) {
         artIdToUserCache.put(user.getArtId(), user);
      }
   }

   /**
    * @return whether the Authentification manager is in the middle of creating a user
    */
   public boolean duringUserCreation() {
      return duringUserCreation;
   }

   public int getNoOneArtifactId() {
      if (noOneArtifactId == 0) {
         try {
            noOneArtifactId = getUser(UserEnum.NoOne).getArtId();
         } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.toString(), ex);
            noOneArtifactId = -1;
         }
      }
      return noOneArtifactId;
   }
}