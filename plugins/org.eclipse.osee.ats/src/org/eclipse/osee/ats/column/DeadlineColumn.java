/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import java.util.Date;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.ActionArtifact;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.util.DeadlineManager;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsAttributeValueColumn;
import org.eclipse.osee.ats.world.IWorldViewArtifact;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

public class DeadlineColumn extends XViewerAtsAttributeValueColumn {

   public static DeadlineColumn instance = new DeadlineColumn();

   public static DeadlineColumn getInstance() {
      return instance;
   }

   private DeadlineColumn() {
      super(AtsAttributeTypes.NeedBy, WorldXViewerFactory.COLUMN_NAMESPACE + ".deadline",
         AtsAttributeTypes.NeedBy.getUnqualifiedName(), 75, SWT.LEFT, true, SortDataType.Date, true, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public DeadlineColumn copy() {
      DeadlineColumn newXCol = new DeadlineColumn();
      copy(this, newXCol);
      return newXCol;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) {
      try {
         if (!(element instanceof IWorldViewArtifact)) {
            return null;
         }
         if (isDeadlineAlerting(element).isTrue()) {
            return ImageManager.getImage(FrameworkImage.WARNING);
         }
      } catch (Exception ex) {
         // do nothing
      }
      return null;
   }

   public static Result isDeadlineAlerting(Object object) throws OseeCoreException {
      if (object instanceof AbstractWorkflowArtifact) {
         return DeadlineManager.isDeadlineDateAlerting((AbstractWorkflowArtifact) object);
      } else if (object instanceof ActionArtifact) {
         for (TeamWorkFlowArtifact team : ((ActionArtifact) object).getTeamWorkFlowArtifacts()) {
            Result result = isDeadlineAlerting(team);
            if (result.isTrue()) {
               return result;
            }
         }
      }
      return Result.FalseResult;
   }

   public static Date getDate(Object object) throws OseeCoreException {
      if (object instanceof ActionArtifact) {
         return getDate(((ActionArtifact) object).getTeamWorkFlowArtifacts().iterator().next());
      } else if (object instanceof TeamWorkFlowArtifact) {
         return ((TeamWorkFlowArtifact) object).getSoleAttributeValue(AtsAttributeTypes.NeedBy, null);
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

}
