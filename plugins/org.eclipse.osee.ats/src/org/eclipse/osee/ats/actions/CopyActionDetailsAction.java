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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.task.TaskArtifact;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.type.AtsArtifactTypes;
import org.eclipse.osee.ats.core.type.AtsAttributeTypes;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.workflow.ChangeTypeUtil;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

/**
 * @author Donald G. Dunne
 */
public class CopyActionDetailsAction extends Action {

   private Clipboard clipboard;
   private final AbstractWorkflowArtifact sma;

   public CopyActionDetailsAction(AbstractWorkflowArtifact sma) {
      super();
      this.sma = sma;
      String title = "Copy";
      title = "Copy " + sma.getArtifactTypeName() + " details to clipboard";
      setText(title);
      setToolTipText(getText());
   }

   private void performCopy() {
      if (clipboard == null) {
         this.clipboard = new Clipboard(null);
      }
      try {
         String detailsStr = "";
         if (sma.getParentTeamWorkflow() != null) {
            TeamDefinitionArtifact teamDef = sma.getParentTeamWorkflow().getTeamDefinition();
            String formatStr = getFormatStr(teamDef);
            if (Strings.isValid(formatStr)) {
               detailsStr = formatStr;
               detailsStr = detailsStr.replaceAll("<hrid>", sma.getHumanReadableId());
               detailsStr = detailsStr.replaceAll("<name>", sma.getName());
               detailsStr = detailsStr.replaceAll("<artType>", sma.getArtifactTypeName());
               detailsStr = detailsStr.replaceAll("<changeType>", getChangeTypeOrObjectType(sma));
            }
         }
         if (!Strings.isValid(detailsStr)) {
            detailsStr =
               "\"" + sma.getArtifactTypeName() + "\" - " + sma.getHumanReadableId() + " - \"" + sma.getName() + "\"";
         }
         clipboard.setContents(new Object[] {detailsStr}, new Transfer[] {TextTransfer.getInstance()});
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   private String getChangeTypeOrObjectType(AbstractWorkflowArtifact awa) throws OseeCoreException {
      String result = "";
      if (awa instanceof TeamWorkFlowArtifact) {
         result = ChangeTypeUtil.getChangeTypeStr(sma);
      } else if (awa instanceof TaskArtifact) {
         result = "Task";
      } else if (awa.isOfType(AtsArtifactTypes.ReviewArtifact)) {
         result = "Review";
      } else if (awa.isOfType(AtsArtifactTypes.Goal)) {
         result = "Goal";
      }
      if (!Strings.isValid(result)) {
         result = "unknown";
      }
      return result;
   }

   private String getFormatStr(TeamDefinitionArtifact teamDef) throws OseeCoreException {
      if (teamDef != null) {
         String formatStr = teamDef.getSoleAttributeValue(AtsAttributeTypes.ActionDetailsFormat, "");
         if (Strings.isValid(formatStr)) {
            return formatStr;
         }
         if (teamDef.getParent() instanceof TeamDefinitionArtifact) {
            return getFormatStr((TeamDefinitionArtifact) teamDef.getParent());
         }
      }
      return null;
   }

   @Override
   public void run() {
      performCopy();
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(AtsImage.COPY_TO_CLIPBOARD);
   }

}
