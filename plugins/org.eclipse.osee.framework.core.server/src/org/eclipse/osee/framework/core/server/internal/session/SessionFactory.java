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
package org.eclipse.osee.framework.core.server.internal.session;

import java.sql.DatabaseMetaData;
import java.util.Date;
import java.util.Properties;
import org.eclipse.osee.framework.core.data.IUserToken;
import org.eclipse.osee.framework.core.data.OseeSessionGrant;
import org.eclipse.osee.framework.core.enums.StorageState;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.cache.IOseeTypeFactory;
import org.eclipse.osee.framework.core.server.OseeServerProperties;
import org.eclipse.osee.framework.core.server.internal.BuildTypeIdentifier;
import org.eclipse.osee.framework.core.server.internal.compatibility.OseeSql_0_9_1;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.DatabaseInfoRegistry;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.database.core.OseeSql;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.logger.Log;

/**
 * @author Roberto E. Escobar
 */
public final class SessionFactory implements IOseeTypeFactory {
   private final BuildTypeIdentifier typeIdentifier;
   private final Log logger;
   private final DatabaseInfoRegistry registry;
   private final IOseeDatabaseService dbService;

   public SessionFactory(Log logger, DatabaseInfoRegistry registry, IOseeDatabaseService dbService, BuildTypeIdentifier typeIdentifier) {
      this.typeIdentifier = typeIdentifier;
      this.logger = logger;
      this.registry = registry;
      this.dbService = dbService;
   }

   public Session createOrUpdate(IOseeCache<String, Session> cache, int uniqueId, StorageState storageState, String guid, String userId, Date creationDate, String managedByServerId, String clientVersion, String clientMachineName, String clientAddress, int clientPort, Date lastInteractionDate, String lastInteractionDetails) throws OseeCoreException {
      Conditions.checkNotNull(cache, "SessionCache");
      Session session = cache.getById(uniqueId);
      if (session == null) {
         session =
            create(guid, userId, creationDate, managedByServerId, clientVersion, clientMachineName, clientAddress,
               clientPort, lastInteractionDate, lastInteractionDetails);
         session.setId(uniqueId);
         session.setStorageState(storageState);
      } else {
         cache.decache(session);
         session.setClientMachineName(clientMachineName);
         session.setClientAddress(clientAddress);
         session.setClientPort(clientPort);
         session.setLastInteractionDate(lastInteractionDate);
         session.setLastInteractionDetails(lastInteractionDetails);
      }
      cache.cache(session);
      return session;
   }

   public Session create(String guid, String userId, Date creationDate, String managedByServerId, String clientVersion, String clientMachineName, String clientAddress, int clientPort, Date lastInteractionDate, String lastInteractionDetails) {
      return new Session(guid, userId, userId, creationDate, managedByServerId, clientVersion, clientMachineName,
         clientAddress, clientPort, lastInteractionDate, lastInteractionDetails);
   }

   public OseeSessionGrant createSessionGrant(Session session, IUserToken userToken) throws OseeCoreException {
      Conditions.checkNotNull(session, "Session");
      Conditions.checkNotNull(userToken, "IUserToken");

      OseeSessionGrant sessionGrant = new OseeSessionGrant(session.getGuid());
      sessionGrant.setCreationRequired(userToken.isCreationRequired());
      sessionGrant.setUserToken(userToken);
      sessionGrant.setDatabaseInfo(registry.getSelectedDatabaseInfo());

      Properties properties = getSQLProperties(dbService, session.getClientVersion());
      sessionGrant.setSqlProperties(properties);

      sessionGrant.setDataStorePath(OseeServerProperties.getOseeApplicationServerData(logger));
      sessionGrant.setClientBuildDesination(typeIdentifier.getBuildDesignation(session.getClientVersion()));
      return sessionGrant;
   }

   private static Properties getSQLProperties(IOseeDatabaseService dbService, String clientVersion) throws OseeCoreException {
      Properties properties = null;
      OseeConnection connection = dbService.getConnection();
      try {
         DatabaseMetaData metaData = connection.getMetaData();
         if (is_0_9_2_Compatible(clientVersion)) {
            properties = OseeSql.getSqlProperties(metaData);
         } else {
            properties = OseeSql_0_9_1.getSqlProperties(metaData);
         }
      } finally {
         if (connection != null) {
            connection.close();
         }
      }
      return properties;
   }

   private static boolean is_0_9_2_Compatible(String clientVersion) {
      boolean result = false;
      if (Strings.isValid(clientVersion)) {
         String toCheck = clientVersion.toLowerCase();
         if (!toCheck.startsWith("0.9.0") && !toCheck.startsWith("0.9.1")) {
            result = true;
         }
      }
      return result;
   }
}
