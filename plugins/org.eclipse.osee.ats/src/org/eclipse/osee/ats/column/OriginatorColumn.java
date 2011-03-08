/*
 * Created on Oct 27, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.IAltLeftClickProvider;
import org.eclipse.nebula.widgets.xviewer.IMultiColumnEditProvider;
import org.eclipse.nebula.widgets.xviewer.IXViewerValueColumn;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerCells;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.ActionManager;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.xviewer.column.XViewerAtsColumn;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.UserListDialog;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class OriginatorColumn extends XViewerAtsColumn implements IXViewerValueColumn, IAltLeftClickProvider, IMultiColumnEditProvider {

   public static OriginatorColumn instance = new OriginatorColumn();

   public static OriginatorColumn getInstance() {
      return instance;
   }

   private OriginatorColumn() {
      super(WorldXViewerFactory.COLUMN_NAMESPACE + ".originator", "Originator", 80, SWT.LEFT, false,
         SortDataType.String, true, null);
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public OriginatorColumn copy() {
      OriginatorColumn newXCol = new OriginatorColumn();
      super.copy(this, newXCol);
      return newXCol;
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      try {
         if (treeItem.getData() instanceof Artifact) {
            Artifact useArt = (Artifact) treeItem.getData();
            if (useArt.isOfType(AtsArtifactTypes.Action)) {
               if (ActionManager.getTeams(useArt).size() == 1) {
                  useArt = ActionManager.getFirstTeam(useArt);
               } else {
                  return false;
               }
            }
            if (!(useArt instanceof TeamWorkFlowArtifact)) {
               return false;
            }
            boolean modified = promptChangeOriginator(Arrays.asList((TeamWorkFlowArtifact) useArt), isPersistViewer());
            XViewer xViewer = ((XViewerColumn) treeColumn.getData()).getTreeViewer();
            if (modified && isPersistViewer(xViewer)) {
               useArt.persist("persist goals via alt-left-click");
            }
            if (modified) {
               xViewer.update(useArt, null);
               return true;
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }

      return false;
   }

   public static boolean promptChangeOriginator(AbstractWorkflowArtifact sma, boolean persist) throws OseeCoreException {
      return promptChangeOriginator(Arrays.asList(sma), persist);
   }

   public static boolean promptChangeOriginator(final Collection<? extends AbstractWorkflowArtifact> awas, boolean persist) throws OseeCoreException {
      UserListDialog ld = new UserListDialog(Displays.getActiveShell(), "Select New Originator", Active.Active);
      int result = ld.open();
      if (result == 0) {
         User selectedUser = ld.getSelection();
         Date createdDate = new Date();

         SkynetTransaction transaction = null;
         if (persist) {
            transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "ATS Prompt Change Originator");
         }

         for (AbstractWorkflowArtifact awa : awas) {
            awa.setCreatedBy(selectedUser, true, createdDate);
            if (persist) {
               awa.persist(transaction);
            }
         }
         if (persist) {
            transaction.execute();
         }
         return true;
      }
      return false;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      try {
         if (element instanceof AbstractWorkflowArtifact) {
            return ((AbstractWorkflowArtifact) element).getCreatedBy().getName();
         }
         if (Artifacts.isOfType(element, AtsArtifactTypes.Action)) {
            Set<String> strs = new HashSet<String>();
            for (TeamWorkFlowArtifact team : ActionManager.getTeams(element)) {
               strs.add(team.getCreatedBy().getName());
            }
            return Collections.toString("; ", strs);
         }
         return "";
      } catch (OseeCoreException ex) {
         return XViewerCells.getCellExceptionString(ex);
      }
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      try {
         Set<AbstractWorkflowArtifact> awas = new HashSet<AbstractWorkflowArtifact>();
         for (TreeItem item : treeItems) {
            Artifact art = (Artifact) item.getData();
            if (art instanceof AbstractWorkflowArtifact) {
               awas.add((AbstractWorkflowArtifact) art);
            }
         }
         promptChangeOriginator(awas, true);
         return;
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

}
