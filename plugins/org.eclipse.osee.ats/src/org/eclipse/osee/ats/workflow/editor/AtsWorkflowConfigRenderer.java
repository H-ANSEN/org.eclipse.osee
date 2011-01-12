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
package org.eclipse.osee.ats.workflow.editor;

import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERALIZED_EDIT;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.PRODUCE_ATTRIBUTE;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.editor.AtsRenderer;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.workdef.WorkDefinition;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.ui.IEditorInput;

/**
 * @author Donald G. Dunne
 */
public class AtsWorkflowConfigRenderer extends AtsRenderer {

   private static final String COMMAND_ID = "org.eclipse.osee.framework.ui.skynet.atsworkflowconfigeditor.command";

   @Override
   public ImageDescriptor getCommandImageDescriptor(Command command, Artifact artifact) {
      return ImageManager.getImageDescriptor(AtsImage.WORKFLOW_CONFIG);
   }

   @Override
   public String getName() {
      return "ATS Workflow Config Editor";
   }

   @Override
   public AtsWorkflowConfigRenderer newInstance() {
      return new AtsWorkflowConfigRenderer();
   }

   @Override
   public int getApplicabilityRating(PresentationType presentationType, Artifact artifact) throws OseeCoreException {
      if (artifact.isOfType(CoreArtifactTypes.WorkFlowDefinition) && !presentationType.matches(GENERALIZED_EDIT,
         PRODUCE_ATTRIBUTE)) {
         return PRESENTATION_SUBTYPE_MATCH;
      }
      return NO_MATCH;
   }

   @Override
   public List<String> getCommandIds(CommandGroup commandGroup) {
      ArrayList<String> commandIds = new ArrayList<String>(1);
      if (commandGroup.isEdit()) {
         commandIds.add(COMMAND_ID);
      }
      return commandIds;
   }

   @Override
   public void open(final List<Artifact> artifacts, PresentationType presentationType) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            try {
               IEditorInput input = new AtsWorkflowConfigEditorInput(new WorkDefinition("This"));
               AWorkbench.getActivePage().openEditor(input, AtsWorkflowConfigEditor.EDITOR_ID);
            } catch (CoreException ex) {
               OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });
   }
}