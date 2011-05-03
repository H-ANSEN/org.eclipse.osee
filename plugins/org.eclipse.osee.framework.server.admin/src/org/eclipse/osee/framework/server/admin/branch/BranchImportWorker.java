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
package org.eclipse.osee.framework.server.admin.branch;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.branch.management.ImportOptions;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.operation.CommandInterpreterReporter;
import org.eclipse.osee.framework.core.operation.OperationReporter;
import org.eclipse.osee.framework.resource.management.Options;
import org.eclipse.osee.framework.resource.management.util.ResourceLocator;
import org.eclipse.osee.framework.server.admin.BaseServerCommand;
import org.eclipse.osee.framework.server.admin.internal.Activator;

/**
 * @author Roberto E. Escobar
 */
public class BranchImportWorker extends BaseServerCommand {

   protected BranchImportWorker() {
      super("Branch Import");
   }

   private boolean isValidArg(String arg) {
      return arg != null && arg.length() > 0;
   }

   @Override
   protected void doCommandWork(IProgressMonitor monitor) throws Exception {
      Options options = new Options();
      String arg = null;
      int count = 0;

      List<Integer> branchIds = new ArrayList<Integer>();
      List<String> importFiles = new ArrayList<String>();
      do {
         arg = getCommandInterpreter().nextArgument();
         if (isValidArg(arg)) {
            if (arg.equals("-excludeBaselineTxs")) {
               options.put(ImportOptions.EXCLUDE_BASELINE_TXS.name(), true);
            } else if (arg.equals("-clean")) {
               options.put(ImportOptions.CLEAN_BEFORE_IMPORT.name(), true);
            } else if (arg.equals("-allAsRootBranches")) {
               options.put(ImportOptions.ALL_AS_ROOT_BRANCHES.name(), true);
            } else if (arg.equals("-minTx")) {
               arg = getCommandInterpreter().nextArgument();
               if (isValidArg(arg)) {
                  options.put(ImportOptions.MIN_TXS.name(), arg);
               }
               count++;
            } else if (arg.equals("-maxTx")) {
               arg = getCommandInterpreter().nextArgument();
               if (isValidArg(arg)) {
                  options.put(ImportOptions.MAX_TXS.name(), arg);
               }
               count++;
            } else if (count == 0 && !arg.startsWith("-")) {
               importFiles.add(arg);
            } else {
               branchIds.add(new Integer(arg));
            }
            count++;
         }
      } while (isValidArg(arg));

      if (importFiles.isEmpty()) {
         throw new OseeArgumentException("Files to import were not specified");
      }

      //      for (File file : importFiles) {
      //         if (file == null || !file.exists() || !file.canRead()) {
      //            throw new OseeArgumentException("File was not accessible: [%s]", file);
      //         } else if (file.isFile() && !Lib.isCompressed(file)) {
      //            throw new OseeArgumentException("Invalid File: [%s]", file);
      //         }
      //      }

      for (String fileToImport : importFiles) {
         URI uri = new URI("exchange://" + fileToImport);
         OperationReporter reporter = new CommandInterpreterReporter(getCommandInterpreter());
         Activator.getBranchExchange().importBranch(new ResourceLocator(uri), options, branchIds, reporter);
      }
   }
}
