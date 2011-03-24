/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.ActionManager;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.swt.SWT;

public class OriginatingWorkFlowColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static OriginatingWorkFlowColumn instance = new OriginatingWorkFlowColumn();

   public static OriginatingWorkFlowColumn getInstance() {
      return instance;
   }

   private OriginatingWorkFlowColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".origWf", "Originating Workflow", 150, SWT.LEFT, false,
         SortDataType.String, false,
         "Team Workflow(s) that were created upon origination of this Action.  Cancelled workflows not included.");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public OriginatingWorkFlowColumn copy() {
      OriginatingWorkFlowColumn newXCol = new OriginatingWorkFlowColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (Artifacts.isOfType(element, AtsArtifactTypes.Action)) {
            getWorldViewOriginatingWorkflowStr((Artifact) element);
         }
         if (element instanceof AbstractWorkflowArtifact) {
            return getColumnText(((AbstractWorkflowArtifact) element).getParentActionArtifact(), column, columnIndex);
         }
      } catch (OseeCoreException ex) {
         XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }

   public static String getWorldViewOriginatingWorkflowStr(Artifact actionArt) throws OseeCoreException {
      Set<String> strs = new HashSet<String>();
      for (TeamWorkFlowArtifact team : getWorldViewOriginatingWorkflows(actionArt)) {
         strs.add(TeamColumn.getName(team));
      }
      return Collections.toString(";", strs);
   }

   public static Collection<TeamWorkFlowArtifact> getWorldViewOriginatingWorkflows(Artifact actionArt) throws OseeCoreException {
      if (ActionManager.getTeams(actionArt).size() == 1) {
         return ActionManager.getTeams(actionArt);
      }
      Collection<TeamWorkFlowArtifact> results = new ArrayList<TeamWorkFlowArtifact>();
      Date origDate = null;
      for (TeamWorkFlowArtifact teamArt : ActionManager.getTeams(actionArt)) {
         if (teamArt.isCancelled()) {
            continue;
         }
         Date teamArtDate = CreatedDateColumn.getDate(teamArt);
         if (origDate == null || teamArtDate.before(origDate)) {
            results.clear();
            origDate = teamArtDate;
            results.add(teamArt);
         } else if (origDate.equals(teamArtDate)) {
            results.add(teamArt);
         }
      }
      return results;
   }
}
