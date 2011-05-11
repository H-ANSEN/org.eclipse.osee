/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.util.widgets.commit;

import java.util.logging.Level;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.osee.ats.core.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.branch.CommitStatus;
import org.eclipse.osee.ats.core.commit.ICommitConfigArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.version.VersionArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;

public class XCommitLabelProvider extends XViewerLabelProvider {

   private final CommitXManager commitXManager;

   public XCommitLabelProvider(CommitXManager commitXManager) {
      super(commitXManager);
      this.commitXManager = commitXManager;
   }

   @Override
   public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      ICommitConfigArtifact configArt = (ICommitConfigArtifact) element;
      Branch branch = configArt.getParentBranch();
      if (xCol.equals(CommitXManagerFactory.Action_Col)) {
         return ImageManager.getImage(FrameworkImage.ARROW_RIGHT_YELLOW);
      }
      if (branch == null) {
         return null;
      }
      if (xCol.equals(CommitXManagerFactory.Status_Col)) {
         try {
            CommitStatus commitStatus =
               AtsBranchManagerCore.getCommitStatus(commitXManager.getXCommitViewer().getTeamArt(), configArt);
            if (commitStatus == CommitStatus.Branch_Not_Configured ||
            //
            commitStatus == CommitStatus.Branch_Commit_Disabled ||
            //
            commitStatus == CommitStatus.Commit_Needed ||
            //
            commitStatus == CommitStatus.Working_Branch_Not_Created) {
               return ImageManager.getImage(FrameworkImage.DOT_RED);
            }

            if (commitStatus == CommitStatus.Merge_In_Progress) {
               return ImageManager.getImage(FrameworkImage.DOT_YELLOW);
            }

            if (commitStatus == CommitStatus.Committed ||
            //
            commitStatus == CommitStatus.Committed_With_Merge) {
               return ImageManager.getImage(FrameworkImage.DOT_GREEN);
            }
            return null;
         } catch (Exception ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         }
      } else if (xCol.equals(CommitXManagerFactory.Merge_Col)) {
         try {
            CommitStatus commitStatus =
               AtsBranchManagerCore.getCommitStatus(commitXManager.getXCommitViewer().getTeamArt(), configArt);
            if (commitStatus == CommitStatus.Merge_In_Progress || commitStatus == CommitStatus.Committed_With_Merge) {
               return ImageManager.getImage(FrameworkImage.OUTGOING_MERGED);
            }
            return null;
         } catch (Exception ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         }
      }
      return null;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws OseeCoreException {
      ICommitConfigArtifact configArt = (ICommitConfigArtifact) element;
      Branch branch = configArt.getParentBranch();

      if (xCol.equals(CommitXManagerFactory.Status_Col)) {
         return AtsBranchManagerCore.getCommitStatus(commitXManager.getXCommitViewer().getTeamArt(), configArt).getDisplayName();
      } else if (xCol.equals(CommitXManagerFactory.Merge_Col)) {
         return "";
      } else if (xCol.equals(CommitXManagerFactory.Version_Col)) {
         return ((Artifact) element).getName();
      } else if (xCol.equals(CommitXManagerFactory.Configuring_Object_Col)) {
         return ((Artifact) element).getArtifactTypeName();
      } else if (xCol.equals(CommitXManagerFactory.Commit_Date)) {
         return handleCommitDateColumn(configArt);
      } else if (xCol.equals(CommitXManagerFactory.Commit_Comment)) {
         return handleCommitCommentColumn(configArt);
      } else if (xCol.equals(CommitXManagerFactory.Dest_Branch_Col)) {
         return handleDestBranchColumn(element, branch);
      } else if (xCol.equals(CommitXManagerFactory.Action_Col)) {
         return handleActionColumn(configArt);
      }
      return "unhandled column";
   }

   private String handleCommitDateColumn(ICommitConfigArtifact configArt) throws OseeCoreException {
      TransactionRecord transactionRecord =
         AtsBranchManagerCore.getCommitTransactionRecord(commitXManager.getXCommitViewer().getTeamArt(), configArt);
      if (transactionRecord != null) {
         new DateUtil();
         return DateUtil.getMMDDYYHHMM(transactionRecord.getTimeStamp());
      }
      return "Not Committed";
   }

   private String handleCommitCommentColumn(ICommitConfigArtifact configArt) throws OseeCoreException {
      TransactionRecord transactionRecord =
         AtsBranchManagerCore.getCommitTransactionRecord(commitXManager.getXCommitViewer().getTeamArt(), configArt);
      if (transactionRecord != null) {
         return transactionRecord.getComment();
      }
      return "Not Committed";
   }

   private String handleDestBranchColumn(Object element, Branch branch) {
      if (element instanceof VersionArtifact) {
         return branch == null ? "Parent Branch Not Configured for Version [" + element + "]" : branch.getShortName();
      } else if (element instanceof TeamDefinitionArtifact) {
         return branch == null ? "Parent Branch Not Configured for Team Definition [" + element + "]" : branch.getShortName();
      }
      return "";
   }

   private String handleActionColumn(ICommitConfigArtifact configArt) throws OseeCoreException {
      CommitStatus commitStatus =
         AtsBranchManagerCore.getCommitStatus(commitXManager.getXCommitViewer().getTeamArt(), configArt);
      if (commitStatus == CommitStatus.Branch_Not_Configured) {
         return "Configure Branch";
      } else if (commitStatus == CommitStatus.Branch_Commit_Disabled) {
         return "Enable Branch Commit";
      } else if (commitStatus == CommitStatus.Commit_Needed) {
         return "Start Commit";
      } else if (commitStatus == CommitStatus.Merge_In_Progress) {
         return "Merge Conflicts and Commit";
      } else if (commitStatus == CommitStatus.Committed) {
         return "Show Change Report";
      } else if (commitStatus == CommitStatus.Committed_With_Merge) {
         return "Show Change/Merge Report";
      } else if (commitStatus == CommitStatus.Working_Branch_Not_Created) {
         return "Working Branch Not Created";
      }
      return "Error: Need to handle this";
   }

   @Override
   public void dispose() {
      // do nothing
   }

   @Override
   public boolean isLabelProperty(Object element, String property) {
      return false;
   }

   @Override
   public void addListener(ILabelProviderListener listener) {
      // do nothing
   }

   @Override
   public void removeListener(ILabelProviderListener listener) {
      // do nothing
   }

   public CommitXManager getTreeViewer() {
      return commitXManager;
   }

}
