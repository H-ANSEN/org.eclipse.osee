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
package org.eclipse.osee.ote.ui.test.manager.batches.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.ote.ui.test.manager.batches.TestBatchProjectNature;
import org.eclipse.osee.ote.ui.test.manager.batches.util.SelectionUtil;
import org.eclipse.osee.ote.ui.test.manager.internal.TestManagerPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author Roberto E. Escobar
 */
public class RemoveTestBatchProjectNature implements IObjectActionDelegate {
   private static final String NATURE_REMOVED = "Test Batch Nature Removed";
   private static final String REMOVING_NATURE = "Removing Test Batch Nature";
   private IJavaProject currentJavaProject;
   private IProject currentProject;

   @Override
   public void setActivePart(IAction action, IWorkbenchPart targetPart) {
      // Do Nothing
   }

   private static String[] removeTestBatchNature(String[] natures) {
      List<String> list = new ArrayList<String>();

      for (int i = 0; i < natures.length; i++) {
         if (!natures[i].equalsIgnoreCase(TestBatchProjectNature.NATURE_ID)) {
            list.add(natures[i]);
         }
      }
      String[] newNatures = new String[list.size()];
      for (int i = 0; i < list.size(); i++) {
         newNatures[i] = list.get(i);
      }
      return newNatures;
   }

   @Override
   public void run(IAction action) {
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      try {
         ((ApplicationWindow) window).setStatus(REMOVING_NATURE);

         if (currentJavaProject == null) {
            currentJavaProject = JavaCore.create(currentProject);
         }
         IProjectDescription description = currentJavaProject.getProject().getDescription();
         String[] natures = description.getNatureIds();

         description.setNatureIds(removeTestBatchNature(natures));
         currentJavaProject.getProject().setDescription(description, null);
         // refresh project so user sees changes
         currentJavaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
         ((ApplicationWindow) window).setStatus(NATURE_REMOVED);
      } catch (Exception ex) {
         OseeLog.logf(TestManagerPlugin.class, Level.SEVERE,
            ex, "Error removing test batch nature on [%s]", currentJavaProject.getProject().getName());
         Shell shell = new Shell();
         MessageDialog.openInformation(shell, TestManagerPlugin.PLUGIN_ID,
            "Error removing test batch nature:\n" + SelectionUtil.getStatusMessages(ex));
      }
   }

   @Override
   public void selectionChanged(IAction action, ISelection selection) {
      currentJavaProject = SelectionUtil.findSelectedJavaProject(selection);
      if (currentJavaProject == null) {
         currentProject = SelectionUtil.findSelectedProject(selection);
      }
   }
}
