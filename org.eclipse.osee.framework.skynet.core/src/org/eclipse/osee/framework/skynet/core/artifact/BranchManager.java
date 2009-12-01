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

package org.eclipse.osee.framework.skynet.core.artifact;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.framework.core.cache.BranchCache;
import org.eclipse.osee.framework.core.data.SystemUser;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchControlled;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.BranchDoesNotExist;
import org.eclipse.osee.framework.core.exception.MultipleBranchesExist;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.database.core.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExtensionDefinedObjects;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.operation.FinishUpdateBranchOperation;
import org.eclipse.osee.framework.skynet.core.artifact.operation.UpdateBranchOperation;
import org.eclipse.osee.framework.skynet.core.artifact.update.ConflictResolverOperation;
import org.eclipse.osee.framework.skynet.core.commit.actions.CommitAction;
import org.eclipse.osee.framework.skynet.core.conflict.ConflictManagerExternal;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;

/**
 * Provides access to all branches as well as support for creating branches of all types
 * 
 * @author Ryan D. Brooks
 */
public class BranchManager {
   private static final int NULL_PARENT_BRANCH_ID = -1;

   private static final BranchManager instance = new BranchManager();

   private static final String LAST_DEFAULT_BRANCH = "LastDefaultBranch";
   public static final String COMMIT_COMMENT = "Commit Branch ";

   private Branch lastBranch;

   private BranchManager() {
   }

   @Deprecated
   // use static methods instead
   public static BranchManager getInstance() {
      return instance;
   }

   public static BranchCache getCache() {
      return Activator.getInstance().getOseeCacheService().getBranchCache();
   }

   public static Set<Branch> getAssociatedArtifactBranches(Artifact associatedArtifact, boolean includeArchived, boolean includeDeleted) throws OseeCoreException {
      Set<Branch> branches = new HashSet<Branch>();
      Set<Branch> branchesToCheck = new HashSet<Branch>(getNormalBranches());
      if (includeArchived) {
         branchesToCheck.addAll(getArchivedBranches());
      }
      for (Branch branch : branchesToCheck) {
         if (branch.getAssociatedArtifact().getArtId() == associatedArtifact.getArtId()) {
            if (includeDeleted || !branch.getBranchState().isDeleted()) {
               branches.add(branch);
            }
         }
      }
      return branches;
   }

   public static Branch getCommonBranch() throws OseeCoreException {
      return getCache().getCommonBranch();
   }

   /**
    * Excludes branches of type MERGE and SYSTEM_ROOT
    * 
    * @return branches that are not archived and are of type STANDARD, TOP_LEVEL, or BASELINE
    * @throws OseeCoreException
    */
   public static List<Branch> getNormalBranches() throws OseeCoreException {
      List<Branch> branches =
            getBranches(BranchArchivedState.UNARCHIVED, BranchControlled.ALL, BranchType.WORKING, BranchType.BASELINE);
      Collections.sort(branches);
      return branches;
   }

   /**
    * Excludes branches of type MERGE and SYSTEM_ROOT
    * 
    * @return branches that are of type STANDARD, TOP_LEVEL, or BASELINE
    * @throws OseeCoreException
    */
   public static List<Branch> getNormalAllBranches() throws OseeCoreException {
      List<Branch> branches =
            getBranches(BranchArchivedState.ALL, BranchControlled.ALL, BranchType.WORKING, BranchType.BASELINE);
      Collections.sort(branches);
      return branches;
   }

   public static List<Branch> getBranches(BranchArchivedState archivedState, BranchControlled branchControlled, BranchType... branchTypes) throws OseeCoreException {
      List<Branch> branches = new ArrayList<Branch>(1000);
      for (Branch branch : getCache().getAll()) {
         if (branch.getArchiveState().matches(archivedState) && //
         BranchControlled.fromBoolean(isChangeManaged(branch)).matches(branchControlled) && //
         branch.getBranchType().isOfType(branchTypes)) {
            branches.add(branch);
         }
      }
      return branches;
   }

   public static Collection<Branch> getWorkingBranches(Branch parentBranch) throws OseeCoreException {
      List<Branch> branches = new ArrayList<Branch>(500);
      for (Branch branch : getCache().getAll()) {
         if (branch.getArchiveState().isUnArchived() && //
         branch.getBranchType().isOfType(BranchType.WORKING) && //
         parentBranch.equals(branch.getParentBranch())) {
            branches.add(branch);
         }
      }
      return branches;
   }

   public static void refreshBranches() throws OseeCoreException {
      getCache().reloadCache();
   }

   public static Branch getBranch(String branchName) throws OseeCoreException {
      Collection<Branch> branches = getBranchesByName(branchName);
      if (branches.isEmpty()) {
         throw new BranchDoesNotExist(String.format("No branch exists with the name: [%s]", branchName));
      }
      if (branches.size() > 1) {
         throw new MultipleBranchesExist(String.format("More than 1 branch exists with the name: [%s]", branchName));
      }
      return branches.iterator().next();
   }

   public static Collection<Branch> getBranchesByName(String branchName) throws OseeCoreException {
      return getCache().getByName(branchName);
   }

   public static Branch getBranchByGuid(String guid) throws OseeCoreException {
      if (!GUID.isValid(guid)) {
         throw new OseeArgumentException(String.format("[%s] is not a valid guid", guid));
      }
      Branch branch = getCache().getByGuid(guid);
      if (branch == null) {
         throw new BranchDoesNotExist(String.format("Branch with guid [%s] does not exist", guid));
      }
      return branch;
   }

   public static boolean branchExists(String branchName) throws OseeCoreException {
      return !getBranchesByName(branchName).isEmpty();
   }

   /**
    * returns the merge branch for this source destination pair from the cache or null if not found
    */
   public static Branch getMergeBranch(Branch sourceBranch, Branch destinationBranch) throws OseeCoreException {
      return getCache().getMergeBranch(sourceBranch, destinationBranch);
   }

   public static boolean isMergeBranch(Branch sourceBranch, Branch destBranch) throws OseeCoreException {
      return getMergeBranch(sourceBranch, destBranch) != null;
   }

   public static Collection<Branch> getArchivedBranches() throws OseeCoreException {
      return getBranches(BranchArchivedState.ARCHIVED, BranchControlled.ALL, BranchType.WORKING, BranchType.BASELINE);
   }

   /**
    * Calls the getMergeBranch method and if it returns null it will create a new merge branch based on the artIds from
    * the source branch.
    */
   public static Branch getOrCreateMergeBranch(Branch sourceBranch, Branch destBranch, ArrayList<Integer> expectedArtIds) throws OseeCoreException {
      Branch mergeBranch = getMergeBranch(sourceBranch, destBranch);
      if (mergeBranch == null) {
         mergeBranch = createMergeBranch(sourceBranch, destBranch, expectedArtIds);
         getCache().cacheMergeBranch(mergeBranch, sourceBranch, destBranch);
      } else {
         UpdateMergeBranch dbTransaction = new UpdateMergeBranch(mergeBranch, expectedArtIds, destBranch, sourceBranch);
         dbTransaction.execute();
      }
      return mergeBranch;
   }

   private static Branch createMergeBranch(final Branch sourceBranch, final Branch destBranch, final ArrayList<Integer> expectedArtIds) throws OseeCoreException {
      Timestamp insertTime = GlobalTime.GreenwichMeanTimestamp();
      int populateBaseTxFromAddressingQueryId = ArtifactLoader.getNewQueryId();
      List<Object[]> datas = new LinkedList<Object[]>();
      for (int artId : expectedArtIds) {
         datas.add(new Object[] {populateBaseTxFromAddressingQueryId, insertTime, artId, sourceBranch.getId(),
               SQL3DataType.INTEGER});
      }
      Branch mergeBranch = null;
      try {
         ArtifactLoader.insertIntoArtifactJoin(datas);

         int parentTxId = sourceBranch.getBaseTransaction().getId();
         String creationComment =
               String.format("New Merge Branch from %s(%s) and %s", sourceBranch.getName(), parentTxId,
                     destBranch.getName());
         String branchName = "Merge " + sourceBranch.getShortName() + " <=> " + destBranch.getShortName();
         mergeBranch =
               HttpBranchCreation.createBranch(BranchType.MERGE, parentTxId, sourceBranch.getId(), branchName,
                     null, null, UserManager.getUser(), creationComment, populateBaseTxFromAddressingQueryId,
                     destBranch.getId());
      } finally {
         ArtifactLoader.clearQuery(populateBaseTxFromAddressingQueryId);
      }
      return mergeBranch;
   }

   public static Branch getBranch(Integer branchId) throws OseeCoreException {
      return getBranch(branchId, true);
   }

   public static Branch getBranchNoExistenceExcpetion(Integer branchId) throws OseeCoreException {
      return getBranch(branchId, false);
   }

   // Always exception for invalid id's, they won't ever be found in the
   private static Branch getBranch(Integer branchId, boolean throwExcpetion) throws OseeCoreException {
      // database or cache.
      if (branchId == null) {
         throw new BranchDoesNotExist("Branch Id is null");
      }

      BranchCache cache = getCache();
      // If someone else made a branch on another machine, we may not know about it
      // so refresh the cache.
      if (cache.getById(branchId) == null) {
         cache.reloadCache();
      }
      Branch branch = cache.getById(branchId);
      if (throwExcpetion && branch == null) {
         throw new BranchDoesNotExist("Branch could not be acquired for branch id: " + branchId);
      }
      return branch;
   }

   /**
    * Update branch
    * 
    * @param Job
    */
   public static Job updateBranch(final Branch branch, final ConflictResolverOperation resolver) {
      IOperation operation = new UpdateBranchOperation(Activator.PLUGIN_ID, branch, resolver);
      return Operations.executeAsJob(operation, true);
   }

   /**
    * Completes the update branch operation by committing latest parent based branch with branch with changes. Then
    * swaps branches so we are left with the most current branch containing latest changes.
    * 
    * @param Job
    */
   public static Job completeUpdateBranch(final ConflictManagerExternal conflictManager, final boolean archiveSourceBranch, final boolean overwriteUnresolvedConflicts) {
      IOperation operation =
            new FinishUpdateBranchOperation(Activator.PLUGIN_ID, conflictManager, archiveSourceBranch,
                  overwriteUnresolvedConflicts);
      return Operations.executeAsJob(operation, true);
   }

   /**
    * Purges a branch from the system. (This operation cannot be undone.) All branch data will be removed.
    * 
    * @param branch
    */
   public static Job purgeBranchInJob(final Branch branch) {
      return Operations.executeAsJob(new PurgeBranchOperation(branch), true);
   }

   public static void purgeBranch(final Branch branch) throws OseeCoreException {
      IOperation operation = new PurgeBranchOperation(branch);
      Operations.executeWork(operation, new NullProgressMonitor(), -1);
      try {
         Operations.checkForStatusSeverityMask(operation.getStatus(), IStatus.ERROR | IStatus.WARNING);
      } catch (Exception ex) {
         throw new OseeWrappedException(ex);
      }
   }

   /**
    * Delete a branch from the system. (This operation will set the branch state to deleted. This operation is
    * undo-able)
    * 
    * @param branchId
    */
   public static Job deleteBranch(final Branch branch) {
      return Operations.executeAsJob(new DeleteBranchOperation(branch), true);
   }

   /**
    * Commit the net changes from the source branch into the destination branch. If there are conflicts between the two
    * branches, the source branch changes will override those on the destination branch.
    * 
    * @param monitor
    * @param conflictManager
    * @param archiveSourceBranch
    * @throws OseeCoreException
    */
   public static void commitBranch(IProgressMonitor monitor, ConflictManagerExternal conflictManager, boolean archiveSourceBranch, boolean overwriteUnresolvedConflicts) throws OseeCoreException {
      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }
      if (conflictManager.remainingConflictsExist() && !overwriteUnresolvedConflicts) {
         throw new OseeCoreException("Commit failed due to unresolved conflicts");
      }
      if (!conflictManager.getDestinationBranch().isEditable()) {
         throw new OseeCoreException("Commit failed - unable to commit into a non-editable branch");
      }
      runCommitExtPointActions(conflictManager.getSourceBranch());
      //      HttpCommitDataRequester.commitBranch(monitor, UserManager.getUser(), conflictManager.getSourceBranch(),
      //            conflictManager.getDestinationBranch(), archiveSourceBranch);
      Activator.getInstance().getCommitBranchService().commitBranch(monitor, conflictManager, archiveSourceBranch);
   }

   private static void runCommitExtPointActions(Branch branch) throws OseeCoreException {
      ExtensionDefinedObjects<CommitAction> extensions =
            new ExtensionDefinedObjects<CommitAction>("org.eclipse.osee.framework.skynet.core.CommitActions",
                  "CommitActions", "className");
      for (CommitAction commitAction : extensions.getObjects()) {
         commitAction.runCommitAction(branch);
      }
   }

   /**
    * Permanently removes transactions and any of their backing data that is not referenced by any other transactions.
    * 
    * @param transactionIdNumber
    */
   public static void purgeTransactions(final int... transactionIdNumbers) {
      purgeTransactions(null, transactionIdNumbers);
   }

   /**
    * Permanently removes transactions and any of their backing data that is not referenced by any other transactions.
    * 
    * @param transactionIdNumber
    */
   public static void purgeTransactions(IJobChangeListener jobChangeListener, final int... transactionIdNumbers) {
      Jobs.startJob(new PurgeTransactionJob(transactionIdNumbers), jobChangeListener);

   }

   /**
    * Creates a new Branch based on the transaction number selected and the parent branch.
    * 
    * @param parentTransactionId
    * @param childBranchName
    * @throws OseeCoreException
    */
   public static Branch createWorkingBranch(TransactionRecord parentTransactionId, String childBranchName, Artifact associatedArtifact) throws OseeCoreException {
      int parentBranchId = parentTransactionId.getBranchId();
      int parentTransactionNumber = parentTransactionId.getId();

      Branch parentBranch = BranchManager.getBranch(parentBranchId);
      String creationComment = "New Branch from " + parentBranch.getName() + "(" + parentTransactionNumber + ")";

      return HttpBranchCreation.createBranch(BranchType.WORKING, parentTransactionNumber, parentBranchId,
            childBranchName, null, null, associatedArtifact, creationComment, -1, -1);
   }

   /**
    * Creates a new Branch based on the most recent transaction on the parent branch.
    * 
    * @param parentTransactionId
    * @param childBranchName
    * @throws OseeCoreException
    */
   public static Branch createWorkingBranch(Branch parentBranch, String childBranchName, Artifact associatedArtifact) throws OseeCoreException {
      TransactionRecord parentTransactionId = TransactionManager.getLastTransaction(parentBranch);
      return createWorkingBranch(parentTransactionId, childBranchName, associatedArtifact);
   }

   /**
    * Creates a new Branch based on the most recent transaction on the parent branch.
    * 
    * @param parentTransactionId
    * @param childBranchName
    * @throws OseeCoreException
    */
   public static Branch createBaselineBranch(Branch parentBranch, String branchName, Artifact associatedArtifact) throws OseeCoreException {
      TransactionRecord parentTransactionId = TransactionManager.getLastTransaction(parentBranch);
      String creationComment = String.format("Root Branch [%s] Creation", branchName);
      return HttpBranchCreation.createBranch(BranchType.BASELINE, parentTransactionId.getId(),
            parentTransactionId.getBranch().getId(), branchName, null, null, associatedArtifact, creationComment, -1,
            -1);
   }

   /**
    * Creates a new root branch, imports skynet types and initializes. If programatic access is necessary, setting the
    * staticBranchName will add a key for this branch and allow access to the branch through
    * getKeyedBranch(staticBranchName).
    * 
    * @param branchName
    * @param staticBranchName will allow programatic access to branch from getKeyedBranch
    * @param initializeArtifacts adds common artifacts needed by most normal root branches
    * @throws Exception
    * @see BranchManager#intializeBranch
    * @see BranchManager#getKeyedBranch(String)
    */
   public static Branch createTopLevelBranch(String branchName, String staticBranchName, String branchGuid) throws OseeCoreException {
      Branch systemRootBranch = BranchManager.getSystemRootBranch();
      TransactionRecord parentTransactionId = TransactionManager.getLastTransaction(systemRootBranch);
      String creationComment = String.format("Root Branch [%s] Creation", branchName);
      Branch branch =
            HttpBranchCreation.createBranch(BranchType.BASELINE, parentTransactionId.getId(),
                  systemRootBranch.getId(), branchName, staticBranchName, branchGuid, null, creationComment, -1, -1);
      if (staticBranchName != null) {
         branch.setAliases(staticBranchName);
      }
      return branch;
   }

   public static Branch createSystemRootBranch() throws OseeCoreException {
      return HttpBranchCreation.createBranch(BranchType.SYSTEM_ROOT, 1, NULL_PARENT_BRANCH_ID,
            CoreBranches.SYSTEM_ROOT.getName(), null, CoreBranches.SYSTEM_ROOT.getGuid(), null,
            CoreBranches.SYSTEM_ROOT.getName() + " Creation", -1, -1);
   }

   public static List<Branch> getBaselineBranches() throws OseeCoreException {
      return getBranches(BranchArchivedState.UNARCHIVED, BranchControlled.ALL, BranchType.BASELINE);
   }

   public static List<Branch> getChangeManagedBranches() throws OseeCoreException {
      return getBranches(BranchArchivedState.UNARCHIVED, BranchControlled.CHANGE_MANAGED, BranchType.WORKING,
            BranchType.BASELINE);
   }

   private void initializeLastBranchValue() {
      try {
         String branchIdStr = UserManager.getUser().getSetting(LAST_DEFAULT_BRANCH);
         if (branchIdStr == null) {
            lastBranch = getDefaultInitialBranch();
            UserManager.getUser().setSetting(LAST_DEFAULT_BRANCH, String.valueOf(lastBranch.getId()));
         } else {
            lastBranch = getBranchNoExistenceExcpetion(Integer.parseInt(branchIdStr));
            if (lastBranch == null) {
               lastBranch = getDefaultInitialBranch();
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);

      }
   }

   private Branch getDefaultInitialBranch() throws OseeCoreException {
      ExtensionDefinedObjects<IDefaultInitialBranchesProvider> extensions =
            new ExtensionDefinedObjects<IDefaultInitialBranchesProvider>(
                  "org.eclipse.osee.framework.skynet.core.DefaultInitialBranchProvider",
                  "DefaultInitialBranchProvider", "class");
      for (IDefaultInitialBranchesProvider provider : extensions.getObjects()) {
         try {
            // Guard against problematic extensions
            for (Branch branch : provider.getDefaultInitialBranches()) {
               if (branch != null) {
                  return branch;
               }
            }
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.WARNING,
                  "Exception occurred while trying to determine initial default branch", ex);
         }
      }
      return getCommonBranch();
   }

   public static Branch getLastBranch() {
      if (instance.lastBranch == null) {
         instance.initializeLastBranchValue();
      }
      return instance.lastBranch;
   }

   public static void setLastBranch(Branch branch) {
      if (branch != null) {
         instance.lastBranch = branch;
      }
   }

   public static Branch getSystemRootBranch() throws OseeCoreException {
      return getCache().getSystemRootBranch();
   }

   public static void persist(Branch... branches) throws OseeCoreException {
      getCache().storeItems(Arrays.asList(branches));
   }

   public static void persist(Collection<Branch> branches) throws OseeCoreException {
      getCache().storeItems(branches);
   }

   public static String toFileName(Branch branch) throws OseeCoreException {
      return BranchUtility.toFileName(branch);
   }

   public static Branch fromFileName(String fileName) throws OseeCoreException {
      return BranchUtility.fromFileName(getCache(), fileName);
   }

   public static Branch getKeyedBranch(String alias) throws OseeCoreException {
      return getCache().getUniqueByAlias(alias);
   }

   public static void decache(Branch branch) throws OseeCoreException {
      getCache().decache(branch);
   }

   public static boolean hasChanges(Branch branch) throws OseeCoreException {
      Pair<TransactionRecord, TransactionRecord> transactions = TransactionManager.getStartEndPoint(branch);
      return transactions.getFirst() != transactions.getSecond();
   }

   public static boolean isChangeManaged(Branch branch) throws OseeCoreException {
      // TODO use Associated Artifacts
      int systemUserArtId = UserManager.getUser(SystemUser.OseeSystem).getArtId();

      int assocArtId = branch.getAssociatedArtifact().getArtId();
      return assocArtId > 0 && assocArtId != systemUserArtId;
   }
}