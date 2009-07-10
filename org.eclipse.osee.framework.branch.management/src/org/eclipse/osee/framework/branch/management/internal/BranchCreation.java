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
package org.eclipse.osee.framework.branch.management.internal;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.eclipse.osee.framework.branch.management.IBranchCreation;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.DbTransaction;
import org.eclipse.osee.framework.db.connection.OseeConnection;
import org.eclipse.osee.framework.db.connection.core.SequenceManager;
import org.eclipse.osee.framework.db.connection.exception.OseeArgumentException;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;

/**
 * @author Andrew M. Finkbeiner
 */
public class BranchCreation implements IBranchCreation {

   private static final String COPY_BRANCH_ADDRESSING =
         "INSERT INTO osee_txs (transaction_id, gamma_id, mod_type, tx_current) SELECT ?, gamma_id, mod_type, tx_current FROM osee_txs txs1, osee_tx_details txd1 WHERE txs1.tx_current = 1 AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = ?";

   private static final String BRANCH_TABLE_INSERT =
         "INSERT INTO osee_branch (branch_id, branch_name, parent_branch_id, parent_transaction_id, archived, associated_art_id, branch_type, branch_state) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

   private static final String INSERT_DEFAULT_BRANCH_NAMES =
         "INSERT INTO OSEE_BRANCH_DEFINITIONS (static_branch_name, mapped_branch_id) VALUES (?, ?)";

   private static final String INSERT_TX_DETAILS =
         "INSERT INTO osee_TX_DETAILS ( branch_id, transaction_id, OSEE_COMMENT, time, author, tx_type ) VALUES ( ?, ?, ?, ?, ?, ?)";

   public int createBranch(BranchType branchType, int parentTransactionId, int parentBranchId, String childBranchName, String creationComment, int associatedArtifactId, int authorId, String staticBranchName) throws Exception {
      CreateBranchTx createBranchTx =
            new CreateBranchTx(branchType, parentTransactionId, parentBranchId, childBranchName, creationComment,
                  associatedArtifactId, authorId, staticBranchName);
      createBranchTx.execute();
      return createBranchTx.getNewBranchId();
   }

   public static class CreateBranchTx extends DbTransaction {
      protected String childBranchName;
      protected int parentBranchId;
      protected int associatedArtifactId;
      protected boolean success = false;
      protected int branchId;
      protected int authorId;
      protected String creationComment;
      protected final BranchType branchType;
      private final int parentTransactionId;
      private final String staticBranchName;

      public CreateBranchTx(BranchType branchType, int parentTransactionId, int parentBranchId, String childBranchName, String creationComment, int associatedArtifactId, int authorId, String staticBranchName) throws OseeCoreException {
         this.parentBranchId = parentBranchId;
         this.childBranchName = childBranchName;
         this.associatedArtifactId = associatedArtifactId;
         this.authorId = authorId;
         this.creationComment = creationComment;
         this.branchType = branchType;
         this.parentTransactionId = parentTransactionId;
         this.staticBranchName = staticBranchName;
      }

      public int getNewBranchId() {
         return branchId;
      }

      @Override
      protected void handleTxWork(OseeConnection connection) throws OseeCoreException {
         Timestamp timestamp = GlobalTime.GreenwichMeanTimestamp();
         branchId =
               initializeBranch(connection, childBranchName, parentBranchId, parentTransactionId, authorId, timestamp,
                     creationComment, associatedArtifactId, branchType, BranchState.CREATED);
         int newTransactionNumber = SequenceManager.getNextTransactionId();
         ConnectionHandler.runPreparedUpdate(connection, INSERT_TX_DETAILS, branchId, newTransactionNumber,
               creationComment, timestamp, authorId, 1);

         specializedBranchOperations(branchId, newTransactionNumber, connection);

         success = true;
      }

      private int initializeBranch(OseeConnection connection, String branchName, int parentBranchId, int parentTransactionId, int authorId, Timestamp creationDate, String creationComment, int associatedArtifactId, BranchType branchType, BranchState branchState) throws OseeDataStoreException, OseeArgumentException {
         int branchId = SequenceManager.getNextBranchId();

         ConnectionHandler.runPreparedUpdate(connection, BRANCH_TABLE_INSERT, branchId, branchName, parentBranchId,
               parentTransactionId, 0, associatedArtifactId, branchType.getValue(), branchState.getValue());

         return branchId;
      }

      public void specializedBranchOperations(int newBranchId, int newTransactionNumber, OseeConnection connection) throws OseeDataStoreException {
         if (branchType != BranchType.SYSTEM_ROOT) {
            int updates = insertAddressing(parentBranchId, newTransactionNumber, connection, false);
            System.out.println(String.format("Create child branch - inserted [%d] records", updates));
         }
         if (staticBranchName != null) {
            insertKeyedBranchIntoDatabase(connection, staticBranchName, newBranchId);
         }
      }

      /**
       * @return the parentBranchId
       */
      public int getParentBranchId() {
         return parentBranchId;
      }

   }

   // descending order is used so that the most recent entry will be used if there are multiple rows with the same gamma (an error case)
   private static final String SELECT_ADDRESSING =
         "SELECT gamma_id, mod_type FROM osee_txs txs, osee_tx_details txd WHERE txs.tx_current = 1 AND txs.transaction_id = txd.transaction_id AND txd.branch_id = ? order by txd.transaction_id desc";
   private static final String INSERT_ADDRESSING =
         "INSERT INTO osee_txs (transaction_id, gamma_id, mod_type, tx_current) VALUES (?,?,?,?)";

   private static int insertAddressing(int parentBranchId, int newTransactionNumber, OseeConnection connection, boolean slow) throws OseeDataStoreException {
      if (slow) {
         return ConnectionHandler.runPreparedUpdate(connection, COPY_BRANCH_ADDRESSING, newTransactionNumber,
               parentBranchId);
      }
      ConnectionHandlerStatement chStmt = new ConnectionHandlerStatement();
      List<Object[]> data = new ArrayList<Object[]>();
      HashSet<Integer> gammas = new HashSet<Integer>(100000);
      try {
         chStmt.runPreparedQuery(10000, SELECT_ADDRESSING, parentBranchId);
         while (chStmt.next()) {
            Integer gamma = chStmt.getInt("gamma_id");
            if (!gammas.contains(gamma)) {
               data.add(new Object[] {newTransactionNumber, gamma, chStmt.getInt("mod_type"), 1});
               gammas.add(gamma);
            }
         }
      } finally {
         chStmt.close();
      }

      ConnectionHandler.runBatchUpdate(connection, INSERT_ADDRESSING, data);
      return data.size();
   }

   public static void insertKeyedBranchIntoDatabase(OseeConnection connection, String staticBranchName, int branchId) throws OseeDataStoreException {
      ConnectionHandler.runPreparedUpdate(connection, INSERT_DEFAULT_BRANCH_NAMES, staticBranchName, branchId);
   }
}
