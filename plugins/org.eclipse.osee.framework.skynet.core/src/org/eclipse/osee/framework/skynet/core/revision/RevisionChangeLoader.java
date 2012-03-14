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
package org.eclipse.osee.framework.skynet.core.revision;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.INCLUDE_DELETED;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeSql;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.change.ChangeBuilder;
import org.eclipse.osee.framework.skynet.core.revision.acquirer.ArtifactChangeAcquirer;
import org.eclipse.osee.framework.skynet.core.revision.acquirer.AttributeChangeAcquirer;
import org.eclipse.osee.framework.skynet.core.revision.acquirer.RelationChangeAcquirer;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

/**
 * Acquires changes for either branches or transactions.
 * 
 * @author Jeff C. Phillips
 */
public final class RevisionChangeLoader {

   protected RevisionChangeLoader() {
      super();
   }

   /**
    * @return Returns artifact, relation and attribute changes from a specific artifact
    */
   public Collection<Change> getChangesPerArtifact(Artifact artifact, IProgressMonitor monitor) throws OseeCoreException {
      return getChangesPerArtifact(artifact, monitor, LoadChangeType.artifact, LoadChangeType.attribute,
         LoadChangeType.relation);
   }

   /**
    * @return Returns artifact, relation and attribute changes from a specific artifact
    */
   public Collection<Change> getChangesPerArtifact(Artifact artifact, IProgressMonitor monitor, LoadChangeType... loadChangeTypes) throws OseeCoreException {
      return getChangesPerArtifact(artifact, monitor, true, loadChangeTypes);
   }

   /**
    * @return Returns artifact, relation and attribute changes from a specific artifact made on the current branch only
    */
   public Collection<Change> getChangesMadeOnCurrentBranch(Artifact artifact, IProgressMonitor monitor) throws OseeCoreException {
      return getChangesPerArtifact(artifact, monitor, false, LoadChangeType.artifact, LoadChangeType.attribute,
         LoadChangeType.relation);
   }

   /**
    * @return Returns artifact, relation and attribute changes from a specific artifact
    */
   private Collection<Change> getChangesPerArtifact(Artifact artifact, IProgressMonitor monitor, boolean recurseThroughBranchHierarchy, LoadChangeType... loadChangeTypes) throws OseeCoreException {
      Branch branch = artifact.getFullBranch();
      Set<TransactionRecord> transactionIds = new LinkedHashSet<TransactionRecord>();
      loadBranchTransactions(branch, artifact, transactionIds, TransactionManager.getHeadTransaction(branch),
         recurseThroughBranchHierarchy);

      Collection<Change> changes = new ArrayList<Change>();

      for (TransactionRecord transactionId : transactionIds) {
         loadChanges(null, transactionId, monitor, artifact, changes, loadChangeTypes);
      }
      return changes;
   }

   private void loadBranchTransactions(Branch branch, Artifact artifact, Set<TransactionRecord> transactionIds, TransactionRecord transactionId, boolean recurseThroughBranchHierarchy) throws OseeCoreException {
      loadTransactions(branch, artifact, transactionId, transactionIds);

      if (recurseThroughBranchHierarchy) {
         if (branch.hasParentBranch() && !branch.getParentBranch().getBranchType().isSystemRootBranch()) {
            loadBranchTransactions(branch.getParentBranch(), artifact, transactionIds, branch.getBaseTransaction(),
               recurseThroughBranchHierarchy);
         }
      }
   }

   private void loadTransactions(Branch branch, Artifact artifact, TransactionRecord transactionId, Set<TransactionRecord> transactionIds) throws OseeCoreException {
      IOseeStatement chStmt = ConnectionHandler.getStatement();
      try {
         chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.LOAD_REVISION_HISTORY_TRANSACTION_ATTR),
            artifact.getArtId(), branch.getId(), transactionId.getId());

         while (chStmt.next()) {
            transactionIds.add(TransactionManager.getTransactionId(chStmt.getInt("transaction_id")));
         }

         chStmt.runPreparedQuery(ClientSessionManager.getSql(OseeSql.LOAD_REVISION_HISTORY_TRANSACTION_REL),
            artifact.getArtId(), artifact.getArtId(), branch.getId(), transactionId.getId());

         while (chStmt.next()) {
            transactionIds.add(TransactionManager.getTransactionId(chStmt.getInt("transaction_id")));
         }
      } finally {
         chStmt.close();
      }
   }

   /**
    * Not Part of Change Report Acquires artifact, relation and attribute changes from a source branch since its
    * creation.
    */
   private void loadChanges(Branch sourceBranch, TransactionRecord transactionId, IProgressMonitor monitor, Artifact specificArtifact, Collection<Change> changes, LoadChangeType... loadChangeTypes) throws OseeCoreException {
      @SuppressWarnings("unused")
      //This is so weak references do not get collected from bulk loading
      Collection<Artifact> bulkLoadedArtifacts;
      ArrayList<ChangeBuilder> changeBuilders = new ArrayList<ChangeBuilder>();

      Set<Integer> artIds = new HashSet<Integer>();
      Set<Integer> newAndDeletedArtifactIds = new HashSet<Integer>();
      boolean historical = sourceBranch == null;

      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }

      monitor.beginTask("Find Changes", 100);

      for (LoadChangeType changeType : loadChangeTypes) {
         switch (changeType) {
            case attribute:
               AttributeChangeAcquirer attributeChangeAcquirer =
                  new AttributeChangeAcquirer(sourceBranch, transactionId, monitor, specificArtifact, artIds,
                     changeBuilders, newAndDeletedArtifactIds);
               changeBuilders = attributeChangeAcquirer.acquireChanges();
               break;
            case relation:
               RelationChangeAcquirer relationChangeAcquirer =
                  new RelationChangeAcquirer(sourceBranch, transactionId, monitor, specificArtifact, artIds,
                     changeBuilders, newAndDeletedArtifactIds);
               changeBuilders = relationChangeAcquirer.acquireChanges();
               break;
            case artifact:
               ArtifactChangeAcquirer artifactChangeAcquirer =
                  new ArtifactChangeAcquirer(sourceBranch, transactionId, monitor, specificArtifact, artIds,
                     changeBuilders, newAndDeletedArtifactIds);
               changeBuilders = artifactChangeAcquirer.acquireChanges();
               break;
            default:
               break;
         }
      }
      monitor.subTask("Loading Artifacts from the Database");

      Branch branch = historical ? transactionId.getBranch() : sourceBranch;

      if (historical) {
         bulkLoadedArtifacts = ArtifactQuery.getHistoricalArtifactListFromIds(artIds, transactionId, INCLUDE_DELETED);
      } else {
         bulkLoadedArtifacts = ArtifactQuery.getArtifactListFromIds(artIds, branch, INCLUDE_DELETED);
      }

      //We build the changes after the artifact loader has been run so we can take advantage of bulk loading.
      for (ChangeBuilder builder : changeBuilders) {
         changes.add(builder.build(branch));
      }

      monitor.done();
   }
}