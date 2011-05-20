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

package org.eclipse.osee.ats.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.actions.wizard.NewNoteWizard;
import org.eclipse.osee.ats.core.workdef.StateDefinition;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.workflow.note.NoteType;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.ui.PlatformUI;

/**
 * @author Donald G. Dunne
 */
public class AddNoteAction extends Action {

   private final AbstractWorkflowArtifact sma;
   private final SMAEditor editor;

   public AddNoteAction(AbstractWorkflowArtifact sma, SMAEditor editor) {
      super();
      this.sma = sma;
      this.editor = editor;
      setText("Add Note");
      setToolTipText(getText());
   }

   @Override
   public void run() {
      performAddNote();
   }

   private void performAddNote() {
      try {
         ArrayList<String> artifactNames = new ArrayList<String>();
         Map<String, String> selectedToStateName = new HashMap<String, String>();
         artifactNames.add("Whole \"" + sma.getArtifactTypeName() + "\"");
         for (StateDefinition stateDefinition : sma.getWorkDefinition().getStatesOrdered()) {
            String displayName = "\"" + stateDefinition.getPageName() + "\" State";
            artifactNames.add(displayName);
            selectedToStateName.put(displayName, stateDefinition.getPageName());
         }
         NewNoteWizard noteWizard = new NewNoteWizard(artifactNames);
         WizardDialog dialog =
            new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), noteWizard);
         dialog.create();
         if (dialog.open() == 0) {
            String selected = noteWizard.mainPage.artifactList.getSelected().iterator().next().getName();
            String state = "";
            if (!selected.startsWith(sma.getName() + " - ")) {
               state = selectedToStateName.get(selected);
            }
            sma.getNotes().addNote(
               NoteType.getType(noteWizard.mainPage.typeList.getSelected().iterator().next().getName()), state,
               noteWizard.mainPage.noteText.get(), UserManager.getUser());
            editor.onDirtied();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(AtsImage.NEW_NOTE);
   }

}
