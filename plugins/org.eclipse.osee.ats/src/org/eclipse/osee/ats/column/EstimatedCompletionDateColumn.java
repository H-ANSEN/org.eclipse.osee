/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import java.util.Date;
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.ActionManager;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.DeadlineManager;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.IATSArtifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

public class EstimatedCompletionDateColumn extends XViewerAtsAttributeValueColumn {

   public static EstimatedCompletionDateColumn instance = new EstimatedCompletionDateColumn();

   public static EstimatedCompletionDateColumn getInstance() {
      return instance;
   }

   private EstimatedCompletionDateColumn() {
      super(AtsAttributeTypes.EstimatedCompletionDate,
         WorldXViewerFactory.COLUMN_NAMESPACE + ".estimatedCompletionDate",
         AtsAttributeTypes.EstimatedCompletionDate.getUnqualifiedName(), 80, SWT.LEFT, false, SortDataType.Date, true,
         "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public EstimatedCompletionDateColumn copy() {
      EstimatedCompletionDateColumn newXCol = new EstimatedCompletionDateColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) {
      try {
         if (!(element instanceof IATSArtifact)) {
            return null;
         }
         if (isWorldViewEcdAlerting(element).isTrue()) {
            return ImageManager.getImage(FrameworkImage.WARNING);
         }
      } catch (Exception ex) {
         // do nothing
      }
      return null;
   }

   public static Result isWorldViewEcdAlerting(Object object) throws OseeCoreException {
      if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(object)) {
            Result result = isWorldViewEcdAlerting(team);
            if (result.isTrue()) {
               return result;
            }
         }
      } else if (object instanceof AbstractWorkflowArtifact) {
         return DeadlineManager.isEcdDateAlerting((AbstractWorkflowArtifact) object);
      }
      return Result.FalseResult;
   }

   public static Date getDate(Object object) throws OseeCoreException {
      if (Artifacts.isOfType(object, AtsArtifactTypes.Action)) {
         return getDate((ActionManager.getFirstTeam(object)));
      } else if (object instanceof TeamWorkFlowArtifact) {
         Date date =
            ((TeamWorkFlowArtifact) object).getSoleAttributeValue(AtsAttributeTypes.EstimatedCompletionDate, null);
         if (date == null) {
            date = EstimatedReleaseDateColumn.getDateFromWorkflow(object);
         }
         if (date == null) {
            date = EstimatedReleaseDateColumn.getDateFromTargetedVersion(object);
         }
         return date;
      } else if (object instanceof AbstractWorkflowArtifact) {
         TeamWorkFlowArtifact teamArt = ((AbstractWorkflowArtifact) object).getParentTeamWorkflow();
         if (teamArt != null) {
            return getDate(teamArt);
         }
      }
      return null;
   }

   public static String getDateStr(AbstractWorkflowArtifact artifact) throws OseeCoreException {
      return DateUtil.getMMDDYY(getDate(artifact));
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         return DateUtil.getMMDDYY(getDate(element));
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
      return "";
   }
}
