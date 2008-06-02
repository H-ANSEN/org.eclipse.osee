/*
 * Created on May 8, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.branch.management.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.eclipse.osee.framework.branch.management.IBranchCreation;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.DbUtil;
import org.eclipse.osee.framework.db.connection.OseeDbConnection;
import org.eclipse.osee.framework.db.connection.core.query.Query;
import org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase;
import org.eclipse.osee.framework.db.connection.core.transaction.DbTransaction;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.jdk.core.util.time.GlobalTime;

/**
 * @author Andrew M Finkbeiner
 */
public class BranchCreation implements IBranchCreation {

   private static final String COPY_BRANCH_ADDRESSING =
         "INSERT INTO osee_define_txs (transaction_id, gamma_id, mod_type, tx_current) SELECT ?, gamma_id, mod_type, tx_current FROM osee_define_txs txs1, osee_define_tx_details txd1 WHERE txs1.tx_current = 1 AND txs1.transaction_id = txd1.transaction_id AND txd1.branch_id = ?";

   private static final String BRANCH_TABLE_INSERT =
         "INSERT INTO OSEE_DEFINE_BRANCH (branch_id, short_name, branch_name, parent_branch_id, archived, associated_art_id) VALUES (?, ?, ?, ?, ?, ?)";

   private static final String SELECT_BRANCH_BY_NAME = "SELECT * FROM osee_define_branch WHERE branch_name = ?";

   private static final String INSERT_DEFAULT_BRANCH_NAMES =
         "INSERT INTO OSEE_BRANCH_DEFINITIONS (static_branch_name, mapped_branch_id) VALUES (?, ?)";

   private static final String INSERT_TX_DETAILS =
         "INSERT INTO OSEE_DEFINE_TX_DETAILS ( branch_id, transaction_id, OSEE_COMMENT, time, author, tx_type ) VALUES ( ?, ?, ?, ?, ?, ?)";

   public int createRootBranch(int parentBranchId, String childBranchShortName, String childBranchName, String creationComment, int associatedArtifactId, int authorId, String staticBranchName) throws Exception {
      CreateRootBranchTx createRootBranchTx =
            new CreateRootBranchTx(parentBranchId, childBranchShortName, childBranchName, creationComment,
                  associatedArtifactId, authorId, staticBranchName);
      createRootBranchTx.execute();
      return createRootBranchTx.getNewBranchId();
   }

   public int createChildBranch(int parentBranchId, String childBranchShortName, String childBranchName, String creationComment, int associatedArtifactId, int authorId) throws Exception {
      CreateChildBranchTx createChildBranchTx =
            new CreateChildBranchTx(parentBranchId, childBranchShortName, childBranchName, creationComment,
                  associatedArtifactId, authorId);
      createChildBranchTx.execute();
      return createChildBranchTx.getNewBranchId();
   }

   private abstract class CreateBranchTx extends DbTransaction {
      protected String childBranchShortName;
      protected String childBranchName;
      protected int parentBranchId;
      protected int associatedArtifactId;
      protected boolean success = false;
      protected int branchId;
      protected int authorId;
      protected String creationComment;

      public CreateBranchTx(int parentBranchId, String childBranchShortName, String childBranchName, String creationComment, int associatedArtifactId, int authorId) {
         this.parentBranchId = parentBranchId;
         this.childBranchShortName = childBranchShortName;
         this.childBranchName = childBranchName;
         this.associatedArtifactId = associatedArtifactId;
         this.authorId = authorId;
         this.creationComment = creationComment;
      }

      public int getNewBranchId() {
         return branchId;
      }

      protected void handleTxWork(Connection connection) throws Exception {
         Timestamp timestamp = GlobalTime.GreenwichMeanTimestamp();
         branchId =
               initializeBranch(connection, childBranchShortName, childBranchName, parentBranchId, authorId, timestamp,
                     creationComment, associatedArtifactId);
         int newTransactionNumber = Query.getNextSeqVal(null, SkynetDatabase.TRANSACTION_ID_SEQ);
         ConnectionHandler.runPreparedUpdate(connection, INSERT_TX_DETAILS, SQL3DataType.INTEGER, branchId,
               SQL3DataType.INTEGER, newTransactionNumber, SQL3DataType.VARCHAR, creationComment,
               SQL3DataType.TIMESTAMP, timestamp, SQL3DataType.INTEGER, authorId, SQL3DataType.INTEGER, 1);

         specializedBranchOperations(branchId, newTransactionNumber, connection);
         success = true;

      }

      private int initializeBranch(Connection connection, String branchShortName, String branchName, int parentBranchId, int authorId, Timestamp creationDate, String creationComment, int associatedArtifactId) throws SQLException {
         if (checkAlreadyHasBranchName(branchName)) {
            throw new IllegalArgumentException("A branch with the name " + branchName + " already exists");
         }
         int branchId = Query.getNextSeqVal(null, SkynetDatabase.BRANCH_ID_SEQ);
         ConnectionHandler.runPreparedUpdate(connection, BRANCH_TABLE_INSERT, SQL3DataType.INTEGER, branchId,
               SQL3DataType.VARCHAR, branchShortName, SQL3DataType.VARCHAR, branchName, SQL3DataType.INTEGER,
               parentBranchId, SQL3DataType.INTEGER, 0, SQL3DataType.INTEGER, associatedArtifactId);

         return branchId;
      }

      private boolean checkAlreadyHasBranchName(String branchName) throws SQLException {
         Connection connection = null;
         boolean alreadyHasName = false;
         try {
            connection = OseeDbConnection.getConnection();
            ConnectionHandlerStatement stmt = null;
            try {
               stmt =
                     org.eclipse.osee.framework.db.connection.ConnectionHandler.runPreparedQuery(connection,
                           SELECT_BRANCH_BY_NAME, SQL3DataType.VARCHAR, branchName);
               if (stmt.getRset().next()) {
                  alreadyHasName = true;
               }
            } finally {
               DbUtil.close(stmt);
            }

         } finally {
            if (connection != null) {
               connection.close();
            }
         }
         return alreadyHasName;
      }

      public abstract void specializedBranchOperations(int newBranchId, int newTransactionNumber, Connection connection) throws SQLException;

   }

   private final class CreateRootBranchTx extends CreateBranchTx {

      private String staticBranchName;

      public CreateRootBranchTx(int parentBranchId, String childBranchShortName, String childBranchName, String creationComment, int associatedArtifactId, int authorId, String staticBranchName) {
         super(parentBranchId, childBranchShortName, childBranchName, creationComment, associatedArtifactId, authorId);
         this.staticBranchName = staticBranchName;
      }

      public CreateRootBranchTx(int parentBranchId, String childBranchShortName, String childBranchName, String creationComment, int associatedArtifactId, int authorId) {
         this(parentBranchId, childBranchShortName, childBranchName, creationComment, associatedArtifactId, authorId,
               null);
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.db.connection.core.transaction.AbstractDbTxTemplate#handleTxWork()
       */
      @Override
      public void specializedBranchOperations(int newBranchId, int newTransactionNumber, Connection connection) throws SQLException {
         if (staticBranchName != null) ConnectionHandler.runPreparedUpdate(INSERT_DEFAULT_BRANCH_NAMES,
               SQL3DataType.VARCHAR, staticBranchName, SQL3DataType.INTEGER, branchId);

      }
   }

   private final class CreateChildBranchTx extends CreateBranchTx {

      public CreateChildBranchTx(int parentBranchId, String childBranchShortName, String childBranchName, String creationComment, int associatedArtifactId, int authorId) {
         super(parentBranchId, childBranchShortName, childBranchName, creationComment, associatedArtifactId, authorId);
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.db.connection.core.transaction.AbstractDbTxTemplate#handleTxWork()
       */
      @Override
      public void specializedBranchOperations(int newBranchId, int newTransactionNumber, Connection connection) throws SQLException {
         int updates =
               ConnectionHandler.runPreparedUpdate(connection, COPY_BRANCH_ADDRESSING, SQL3DataType.INTEGER,
                     newTransactionNumber, SQL3DataType.INTEGER, parentBranchId);
         System.out.println(String.format("Create child branch - updated [%d] records", updates));
      }
   }
}
