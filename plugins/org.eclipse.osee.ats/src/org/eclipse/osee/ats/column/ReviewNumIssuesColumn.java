/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.core.review.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.core.review.defect.ReviewDefectManager;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.swt.SWT;

public class ReviewNumIssuesColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static ReviewNumIssuesColumn instance = new ReviewNumIssuesColumn();

   public static ReviewNumIssuesColumn getInstance() {
      return instance;
   }

   private ReviewNumIssuesColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".reviewIssues", "Review Issues", 40, SWT.CENTER, false,
         SortDataType.Integer, false, "Number of Issues found in Review");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ReviewNumIssuesColumn copy() {
      ReviewNumIssuesColumn newXCol = new ReviewNumIssuesColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof PeerToPeerReviewArtifact) {
            return String.valueOf(new ReviewDefectManager((PeerToPeerReviewArtifact) element).getNumIssues());
         }
      } catch (OseeCoreException ex) {
         XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }
}
