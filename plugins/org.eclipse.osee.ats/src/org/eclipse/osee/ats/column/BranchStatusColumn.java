/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.util.ActionManager;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.swt.SWT;

public class BranchStatusColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static BranchStatusColumn instance = new BranchStatusColumn();

   public static BranchStatusColumn getInstance() {
      return instance;
   }

   private BranchStatusColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".branchStatus", "Branch Status", 40, SWT.CENTER, false,
         SortDataType.String, false, null);
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public BranchStatusColumn copy() {
      BranchStatusColumn newXCol = new BranchStatusColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (Artifacts.isOfType(element, AtsArtifactTypes.Action)) {
            Set<String> strs = new HashSet<String>();
            for (TeamWorkFlowArtifact team : ActionManager.getTeams(element)) {
               String str = getColumnText(team, column, columnIndex);
               if (Strings.isValid(str)) {
                  strs.add(str);
               }
            }
            return Collections.toString(", ", strs);
         }
         if (Artifacts.isOfType(element, AtsArtifactTypes.TeamWorkflow)) {
            TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) element;
            try {
               if (teamArt.getBranchMgr().isWorkingBranchInWork()) {
                  return "Working";
               } else if (teamArt.getBranchMgr().isCommittedBranchExists()) {
                  if (!teamArt.getBranchMgr().isAllObjectsToCommitToConfigured() || !teamArt.getBranchMgr().isBranchesAllCommitted()) {
                     return "Needs Commit";
                  }
                  return "Committed";
               }
               return "";
            } catch (Exception ex) {
               return "Exception: " + ex.getLocalizedMessage();
            }
         }
      } catch (OseeCoreException ex) {
         XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }
}
