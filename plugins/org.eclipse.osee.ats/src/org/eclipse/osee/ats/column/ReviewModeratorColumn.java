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
import org.eclipse.osee.ats.core.review.role.Role;
import org.eclipse.osee.ats.core.review.role.UserRoleManager;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.swt.SWT;

public class ReviewModeratorColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static ReviewModeratorColumn instance = new ReviewModeratorColumn();

   public static ReviewModeratorColumn getInstance() {
      return instance;
   }

   private ReviewModeratorColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".reviewModerator", "Review Moderator", 100, SWT.LEFT, false,
         SortDataType.String, false, "Review Moderator(s)");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ReviewModeratorColumn copy() {
      ReviewModeratorColumn newXCol = new ReviewModeratorColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof PeerToPeerReviewArtifact) {
            UserRoleManager roleMgr = new UserRoleManager(((PeerToPeerReviewArtifact) element));
            return Artifacts.toString("; ", roleMgr.getRoleUsers(Role.Moderator));
         }
      } catch (OseeCoreException ex) {
         XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }
}
