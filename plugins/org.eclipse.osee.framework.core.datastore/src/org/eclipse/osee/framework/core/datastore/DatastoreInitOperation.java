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
package org.eclipse.osee.framework.core.datastore;

import java.io.File;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.branch.management.IOseeBranchService;
import org.eclipse.osee.framework.core.datastore.internal.Activator;
import org.eclipse.osee.framework.core.datastore.schema.operations.OperationFactory;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.server.IApplicationServerManager;
import org.eclipse.osee.framework.core.server.OseeServerProperties;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.core.services.IdentityService;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.OseeInfo;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public class DatastoreInitOperation extends AbstractOperation {

   private static final String ADD_PERMISSION =
      "INSERT INTO OSEE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES (?,?)";

   private final IOseeCachingService cachingService;
   private final IOseeBranchService branchService;
   private final IOseeDatabaseService dbService;
   private final IOseeSchemaProvider schemaProvider;
   private final SchemaCreationOptions options;
   private final IApplicationServerManager appServerManager;
   private final IdentityService identityService;

   public DatastoreInitOperation(IApplicationServerManager appServerManager, IOseeDatabaseService dbService, IOseeCachingService cachingService, IOseeBranchService branchService, IOseeSchemaProvider schemaProvider, SchemaCreationOptions options, IdentityService identityService) {
      super("Datastore Initialization", Activator.PLUGIN_ID);
      this.appServerManager = appServerManager;
      this.dbService = dbService;
      this.cachingService = cachingService;
      this.branchService = branchService;
      this.schemaProvider = schemaProvider;
      this.options = options;
      this.identityService = identityService;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      Conditions.checkExpressionFailOnTrue(dbService.isProduction(),
         "Error - attempting to initialize a production datastore.");

      IOperation subOp = OperationFactory.createDbSchema(dbService, schemaProvider, options);
      doSubWork(subOp, monitor, 0.30);

      dbService.getSequence().clear();
      identityService.clear();

      appServerManager.executeLookupRegistration();

      deleteBinaryBackingData();
      String binaryDataPath = OseeServerProperties.getOseeApplicationServerData(null);
      Lib.deleteDir(new File(binaryDataPath + File.separator + "attr"));

      OseeInfo.putValue(OseeInfo.DB_ID_KEY, GUID.create());
      addDefaultPermissions();

      //      cachingService.clearAll();

      subOp = branchService.createSystemRootBranch(monitor);
      doSubWork(subOp, monitor, 0.30);
   }

   @SuppressWarnings("unchecked")
   private void addDefaultPermissions() throws OseeCoreException {
      for (PermissionEnum permission : PermissionEnum.values()) {
         dbService.runPreparedUpdate(ADD_PERMISSION, permission.getPermId(), permission.getName());
      }
   }

   private static void deleteBinaryBackingData() {
      String binaryDataPath = OseeServerProperties.getOseeApplicationServerData(null);
      OseeLog.logf(Activator.class, Level.INFO, "Deleting application server binary data [%s]...", binaryDataPath);
      Lib.deleteDir(new File(binaryDataPath + File.separator + "attr"));
   }
}
