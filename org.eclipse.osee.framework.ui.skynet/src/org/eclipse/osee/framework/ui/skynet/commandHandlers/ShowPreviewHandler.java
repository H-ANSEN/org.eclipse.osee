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
package org.eclipse.osee.framework.ui.skynet.commandHandlers;

import java.sql.SQLException;
import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.revision.ArtifactChange;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;

/**
 * @author Paul K. Waldfogel
 */
public class ShowPreviewHandler extends AbstractSelectionChangedHandler {
   public ShowPreviewHandler() {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
    */
   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      IStructuredSelection myIStructuredSelection = getActiveSiteSelection();

      List<ArtifactChange> mySelectedArtifactChangeList =
            Handlers.getArtifactChangesFromStructuredSelection(myIStructuredSelection);
      for (ArtifactChange mySelectedArtifactChange : mySelectedArtifactChangeList) {
         Artifact selectedArtifact;
         try {
            selectedArtifact = mySelectedArtifactChange.getArtifact();
            ArtifactEditor.editArtifact(selectedArtifact);
         } catch (SQLException ex) {
            OSEELog.logException(getClass(), ex, true);
         }
      }
      return null;
   }
}
