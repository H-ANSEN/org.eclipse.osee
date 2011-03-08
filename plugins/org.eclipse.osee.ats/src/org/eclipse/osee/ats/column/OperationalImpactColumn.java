/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.ActionManager;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.IATSArtifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.swt.SWT;

public class OperationalImpactColumn extends XViewerValueColumn {

   public static OperationalImpactColumn instance = new OperationalImpactColumn();

   public static OperationalImpactColumn getInstance() {
      return instance;
   }

   private OperationalImpactColumn() {
      super("ats.Operational Impact", "Operational Impact", 80, SWT.LEFT, false, SortDataType.String, true,
         "Does this change have an operational impact to the product.");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public OperationalImpactColumn copy() {
      OperationalImpactColumn newXCol = new OperationalImpactColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      if (element instanceof IATSArtifact) {
         try {
            return getOperationalImpact((IATSArtifact) element);
         } catch (OseeCoreException ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
            return XViewerCells.getCellExceptionString(ex);
         }
      }
      return "";
   }

   private String getOperationalImpact(IATSArtifact wva) throws OseeCoreException {
      if (wva instanceof TeamWorkFlowArtifact) {
         return ((TeamWorkFlowArtifact) wva).getArtifact().getSoleAttributeValue(AtsAttributeTypes.OperationalImpact,
            "");
      }
      if (Artifacts.isOfType(wva, AtsArtifactTypes.Action)) {
         Set<String> strs = new HashSet<String>();
         for (TeamWorkFlowArtifact team : ActionManager.getTeams(wva)) {
            strs.add(getOperationalImpact(team));
         }
         return Collections.toString(", ", strs);
      }
      if (wva instanceof TaskArtifact) {
         return getOperationalImpact(((TaskArtifact) wva).getParentTeamWorkflow());
      }
      return "";
   }

}
