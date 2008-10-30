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
package org.eclipse.osee.framework.skynet.core.dbinit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.database.IDbInitializationTask;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionPoints;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.exportImport.HttpBranchExchange;
import org.osgi.framework.Bundle;

/**
 * @author Roberto E. Escobar
 */
public class SkynetDbBranchDataImport implements IDbInitializationTask {
   private static final String ELEMENT_NAME = "OseeDbImportData";
   private static final String EXTENSION_POINT = SkynetActivator.PLUGIN_ID + "." + ELEMENT_NAME;
   private static final String BRANCH_NAME = "branchName";
   private static final String BRANCH_DATA = "branchData";
   private static final String BRANCHES_TO_IMPORT = "BranchesToImport";

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.database.initialize.tasks.IDbInitializationTask#run(java.sql.Connection)
    */
   @Override
   public void run(Connection connection) throws OseeCoreException {
      if (OseeProperties.getInstance().getDbOseeSkynetBranchImport()) {
         // Clean up and delete all branches except Common
         for (Branch branch : BranchPersistenceManager.getNormalBranches()) {
            if (!branch.getBranchName().equals(Branch.COMMON_BRANCH_CONFIG_ID)) {
               BranchPersistenceManager.deleteBranch(branch);
            }
         }

         Collection<ImportData> importDatas = loadDataFromExtensions();
         for (ImportData importData : importDatas) {
            OseeLog.log(SkynetActivator.class, Level.INFO, String.format("Import Branch Data: [%s]", importData));
            try {
               File importFile = importData.getExchangeFile();
               //TODO not yet supported               importData.getSelectedBranches();
               HttpBranchExchange.importBranches(importFile.toURI().toASCIIString(), true, true);
            } catch (OseeDataStoreException ex) {
               OseeLog.log(SkynetActivator.class, Level.SEVERE, String.format("Exception while importing branch: [%s]",
                     importData), ex);
               throw ex;
            }
         }
      }
   }

   private Collection<ImportData> loadDataFromExtensions() throws OseeDataStoreException {
      List<ImportData> toReturn = new ArrayList<ImportData>();
      Map<String, String> selectedBranches = new HashMap<String, String>();
      List<IConfigurationElement> elements = ExtensionPoints.getExtensionElements(EXTENSION_POINT, ELEMENT_NAME);
      for (IConfigurationElement element : elements) {
         String bundleName = element.getContributor().getName();
         String branchData = element.getAttribute(BRANCH_DATA);

         if (Strings.isValid(bundleName) && Strings.isValid(branchData)) {
            File exchangeFile;
            try {
               exchangeFile = getExchangeFile(bundleName, branchData);
            } catch (Exception ex) {
               throw new OseeDataStoreException(ex);
            }
            ImportData importData = new ImportData(exchangeFile);
            for (IConfigurationElement innerElement : element.getChildren(BRANCHES_TO_IMPORT)) {
               String branchName = innerElement.getAttribute(BRANCH_NAME);
               if (Strings.isValid(branchName)) {
                  importData.addSelectedBranch(branchName);
                  if (!selectedBranches.containsKey(branchName.toLowerCase())) {
                     selectedBranches.put(branchName.toLowerCase(),
                           element.getDeclaringExtension().getUniqueIdentifier());
                  } else {
                     throw new OseeDataStoreException(
                           String.format(
                                 "Branch import error - cannot import twice into a branch - [%s] was already specified by [%s] ",
                                 branchName, selectedBranches.get(branchName.toLowerCase())));
                  }
               }
            }
            toReturn.add(importData);
         } else {
            throw new OseeDataStoreException(String.format("Branch import error: [%s] attributes were empty.",
                  element.getDeclaringExtension().getExtensionPointUniqueIdentifier()));
         }
      }
      return toReturn;
   }

   private File getExchangeFile(String bundleName, String exchangeFile) throws IOException, URISyntaxException {
      if (exchangeFile.endsWith("zip") != true) {
         throw new IOException(String.format("Branch data file is invalid [%s] ", exchangeFile));
      }
      Bundle bundle = Platform.getBundle(bundleName);
      URL url = bundle.getResource(exchangeFile);
      url = FileLocator.toFileURL(url);
      String urlValue = url.toString();
      URI uri = new URI(urlValue.replaceAll(" ", "%20"));
      File toReturn = new File(uri);
      if (toReturn.exists() != true) {
         throw new FileNotFoundException(String.format("Branch data file cannot be found [%s]", exchangeFile));
      }
      return toReturn;
   }

   private final class ImportData {
      private File exchangeFile;
      private Set<String> selectedBranches;

      public ImportData(File exchangeFile) {
         super();
         this.exchangeFile = exchangeFile;
         this.selectedBranches = new HashSet<String>();
      }

      public void addSelectedBranch(String branchName) {
         this.selectedBranches.add(branchName);
      }

      public String toString() {
         return String.format("%s - %s", exchangeFile, selectedBranches);
      }

      public boolean areAllSelected() {
         return this.selectedBranches.size() == 0;
      }

      public Set<String> getSelectedBranches() {
         return selectedBranches;
      }

      public File getExchangeFile() {
         return exchangeFile;
      }
   }
}
