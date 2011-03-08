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
import org.eclipse.osee.ats.artifact.AbstractTaskableArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.util.ActionManager;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.swt.SWT;

public class NumberOfTasksRemainingColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static NumberOfTasksRemainingColumn instance = new NumberOfTasksRemainingColumn();

   public static NumberOfTasksRemainingColumn getInstance() {
      return instance;
   }

   private NumberOfTasksRemainingColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".numberOfTasksRemain", "Number of Tasks Remaining", 40, SWT.CENTER,
         false, SortDataType.Integer, false, null);
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public NumberOfTasksRemainingColumn copy() {
      NumberOfTasksRemainingColumn newXCol = new NumberOfTasksRemainingColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (ActionManager.isOfTypeAction(element)) {
            Set<String> strs = new HashSet<String>();
            for (TeamWorkFlowArtifact team : ActionManager.getTeams(element)) {
               String str = getColumnText(team, column, columnIndex);
               if (Strings.isValid(str)) {
                  strs.add(str);
               }
            }
            return Collections.toString(", ", strs);
         }
         if (element instanceof AbstractTaskableArtifact) {
            int num = ((AbstractTaskableArtifact) element).getNumTasksInWork();
            if (num == 0) {
               return "";
            }
            return String.valueOf(num);
         }
      } catch (OseeCoreException ex) {
         XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }
}
