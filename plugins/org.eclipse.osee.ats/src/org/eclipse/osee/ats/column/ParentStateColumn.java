/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.swt.SWT;

public class ParentStateColumn extends XViewerAtsColumn implements IXViewerValueColumn {

   public static ParentStateColumn instance = new ParentStateColumn();

   public static ParentStateColumn getInstance() {
      return instance;
   }

   private ParentStateColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".parentstate", "Parent State", 75, SWT.LEFT, false,
         SortDataType.String, false, "State of the Parent Team Workflow or Action");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public ParentStateColumn copy() {
      ParentStateColumn newXCol = new ParentStateColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof AbstractWorkflowArtifact && ((AbstractWorkflowArtifact) element).getParentAWA() != null) {
            return ((AbstractWorkflowArtifact) element).getParentAWA().getStateMgr().getCurrentStateName();
         }
      } catch (OseeCoreException ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
      return "";
   }

}
