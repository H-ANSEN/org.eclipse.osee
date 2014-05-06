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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.database.core.OseeInfo;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public class User extends Artifact {
   private PropertyStore userSettings;

   public User(String guid, IOseeBranch branch, IArtifactType artifactType) throws OseeCoreException {
      super(guid, branch, artifactType);
   }

   public void setFieldsBasedon(User u) throws Exception {
      setName(u.getName());
      setPhone(u.getPhone());
      setEmail(u.getEmail());
      setUserID(u.getUserId());
      setActive(u.isActive());
   }

   @Override
   public String toString() {
      try {
         return String.format("%s (%s)", getName(), getUserId());
      } catch (Exception ex) {
         return "Exception: " + ex.getLocalizedMessage();
      }
   }

   public String getUserId() throws OseeCoreException {
      return getSoleAttributeValue(CoreAttributeTypes.UserId, "");
   }

   public void setUserID(String userId) throws OseeCoreException {
      setSoleAttributeValue(CoreAttributeTypes.UserId, userId);
   }

   public String getEmail() throws OseeCoreException {
      return getSoleAttributeValue(CoreAttributeTypes.Email, "");
   }

   public void setEmail(String email) throws OseeCoreException {
      setSoleAttributeValue(CoreAttributeTypes.Email, email);
   }

   public String getPhone() throws OseeCoreException {
      return getSoleAttributeValue(CoreAttributeTypes.Phone, "");
   }

   public void setPhone(String phone) throws OseeCoreException {
      setSoleAttributeValue(CoreAttributeTypes.Phone, phone);
   }

   public boolean isActive() throws OseeCoreException {
      return getSoleAttributeValue(CoreAttributeTypes.Active);
   }

   public void setActive(boolean active) throws OseeCoreException {
      setSoleAttributeValue(CoreAttributeTypes.Active, active);
   }

   public void toggleFavoriteBranch(Branch favoriteBranch) throws OseeCoreException {
      if (OseeInfo.isBooleanUsingCache(OseeProperties.OSEE_USING_LEGACY_BRANCH_GUID_FOR_EVENTS)) {
         OseeLog.log(this.getClass(), OseeLevel.SEVERE_POPUP,
            "Toggle Favorite Branch is disabled for this version until OSEE database branches are converted to Uuid");
      }
      HashSet<Long> branchUuids = new HashSet<Long>();
      for (Branch branch : BranchManager.getBranches(BranchArchivedState.UNARCHIVED, BranchType.WORKING,
         BranchType.BASELINE)) {
         branchUuids.add(branch.getUuid());
      }

      boolean found = false;
      Collection<Attribute<String>> attributes = getAttributes(CoreAttributeTypes.FavoriteBranch);
      for (Attribute<String> attribute : attributes) {
         // Remove attributes that are no longer valid
         Long uuid = 0L;
         try {
            uuid = Long.valueOf(attribute.getValue());
         } catch (Exception ex) {
            // do nothing
         }
         if (!branchUuids.contains(uuid)) {
            attribute.delete();
         } else if (favoriteBranch.getUuid() == uuid) {
            attribute.delete();
            found = true;
            break;
         }
      }

      if (!found) {
         addAttribute(CoreAttributeTypes.FavoriteBranch, String.valueOf(favoriteBranch.getUuid()));
      }
      setSetting(CoreAttributeTypes.FavoriteBranch.getName(), String.valueOf(favoriteBranch.getUuid()));
   }

   public boolean isFavoriteBranch(IOseeBranch branch) throws OseeCoreException {
      Collection<Attribute<String>> attributes = getAttributes(CoreAttributeTypes.FavoriteBranch);
      for (Attribute<String> attribute : attributes) {
         String value = attribute.getValue();
         if (OseeInfo.isBooleanUsingCache(OseeProperties.OSEE_USING_LEGACY_BRANCH_GUID_FOR_EVENTS)) {
            if (branch.getGuid().equals(attribute.getValue())) {
               return true;
            }
         } else {
            // Backward compatibility until db is converted
            if (GUID.isValid(value)) {
               if (branch.getGuid().equals(value)) {
                  return true;
               }
            } else {
               try {
                  if (Long.valueOf(value).equals(branch.getUuid())) {
                     return true;
                  }
               } catch (Exception ex) {
                  // do nothing
               }
            }
         }

      }
      return false;
   }

   public String getSetting(String key) throws OseeCoreException {
      ensureUserSettingsAreLoaded();
      return userSettings.get(key);
   }

   public boolean getBooleanSetting(String key) throws OseeCoreException {
      return Boolean.parseBoolean(getSetting(key));
   }

   public void setSetting(String key, String value) throws OseeCoreException {
      ensureUserSettingsAreLoaded();
      userSettings.put(key, value);

   }

   public void setSetting(String key, Long value) throws OseeCoreException {
      ensureUserSettingsAreLoaded();
      userSettings.put(key, value);

   }

   public void saveSettings() throws OseeCoreException {
      saveSettings(null);
   }

   public void saveSettings(SkynetTransaction transaction) throws OseeCoreException {
      if (userSettings != null) {
         StringWriter stringWriter = new StringWriter();
         try {
            userSettings.save(stringWriter);
         } catch (Exception ex) {
            OseeExceptions.wrapAndThrow(ex);
         }
         setSoleAttributeFromString(CoreAttributeTypes.UserSettings, stringWriter.toString());
         if (transaction == null) {
            persist("User - Save Settings");
         } else {
            persist(transaction);
         }
      }
   }

   private void ensureUserSettingsAreLoaded() throws OseeCoreException {
      if (userSettings == null) {
         PropertyStore store = new PropertyStore(getGuid());
         try {
            String settings = getSoleAttributeValue(CoreAttributeTypes.UserSettings, null);
            if (settings != null) {
               store.load(new StringReader(settings));
            }
         } catch (Exception ex) {
            OseeExceptions.wrapAndThrow(ex);
         }
         userSettings = store;
      }
   }

   public boolean isSystemUser() {
      return SystemUser.isSystemUser(this);
   }

   public void setBooleanSetting(String key, boolean value) throws OseeCoreException {
      setSetting(key, String.valueOf(value));
   }

}
