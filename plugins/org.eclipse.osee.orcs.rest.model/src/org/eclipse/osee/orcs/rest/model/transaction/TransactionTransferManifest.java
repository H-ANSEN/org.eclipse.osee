package org.eclipse.osee.orcs.rest.model.transaction;

import static org.eclipse.osee.orcs.rest.model.transaction.TransferTupleTypes.ExportedBranch;
import static org.eclipse.osee.orcs.rest.model.transaction.TransferTupleTypes.TransferFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.jdk.core.result.XResultData;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.search.TupleQuery;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;

/**
 * @author Huy A. Tran
 */

public class TransactionTransferManifest {
   public TransactionId exportId = null;
   public TransactionId buildId = null;
   private List<TransferBranch> transferBranchList;
   public String path;
   private final XResultData results;
   private final int PURGE_TXS_LIMIT = 100;

   public TransactionTransferManifest() {
      this.results = new XResultData();
   }

   public List<TransferBranch> getTransferBranches() {
      if (transferBranchList == null) {
         transferBranchList = new ArrayList<>();
      }
      return transferBranchList;
   }

   public void addTransferBranch(TransferBranch tb) {
      if (transferBranchList == null) {
         transferBranchList = new ArrayList<>();
      }
      transferBranchList.add(tb);
   }

   public XResultData parse(String dirName) {
      transferBranchList = new ArrayList<>();

      this.path = dirName;
      File manifestFile = new File(String.format("%s%s%s", path, File.separator, "manifest.md"));
      try {
         String manifest = Lib.fileToString(manifestFile);
         String[] mdLines = manifest.split("\n");
         if (mdLines[0].toLowerCase().contains("buildid")) {
            this.buildId = TransactionId.valueOf(mdLines[0].split(":")[1].trim());
         } else {
            results.errorf("BuildId not found in: %s", mdLines[0]);
            return results;
         }
         if (mdLines[1].toLowerCase().contains("exportid")) {
            this.exportId = TransactionId.valueOf(mdLines[1].split(":")[1].trim());
         } else {
            results.errorf("ExportId not found in: %s", mdLines[1]);
            return results;
         }
         // skip lines 3,4 since they should be the table header
         boolean done = false;
         int i = 4;
         TransferBranch transBranch = null;
         while (!done) {
            if (mdLines[i].toLowerCase().contains("prev")) {
               if (transBranch != null) {
                  // finish off previous branch
                  this.transferBranchList.add(transBranch);
               }
               transBranch = createTransferBranchFromRow(mdLines[i]);
            } else if (mdLines[i].toLowerCase().contains("cur")) {
               results.errorf("not accepting this type %s", mdLines[i]);
            } else if (mdLines[i].toLowerCase().contains("add")) {
               addRowToTransferBranch(transBranch, mdLines[i], TransferOpType.ADD);
            } else if (mdLines[i].toLowerCase().contains("empty")) {
               addRowToTransferBranch(transBranch, mdLines[i], TransferOpType.EMPTY);
            } else if (mdLines[i].length() < 3) {
               done = true;
               this.transferBranchList.add(transBranch);
            } else {
               results.errorf("unhandled type in row %s", mdLines);
               return results;
            }
            ++i;
         }
         // TODO possibly handle directories/contents
      } catch (Exception e) {
         results.errorf("%s",
            String.format("IO Exception while verifying manifest and transaction files. %s", e.getMessage()));
      }

      return results;
   }

   private TransferBranch createTransferBranchFromRow(String row) {
      String[] mdCols = row.split("\\|");
      TransferBranch transBranch = new TransferBranch(BranchId.valueOf(mdCols[1].trim()));
      transBranch.setPrevTx(TransactionId.valueOf(mdCols[2].trim()));

      TransferTransaction trans = new TransferTransaction(transBranch.getBranchId(), transBranch.getPrevTx(),
         TransactionId.valueOf(mdCols[3].trim()), TransferOpType.PREV_TX);
      transBranch.addTransferTransaction(trans);
      return transBranch;
   }

   private void addRowToTransferBranch(TransferBranch transBranch, String row, TransferOpType type) {
      String[] mdCols = row.split("\\|");
      TransactionId sourceTx = TransactionId.valueOf(mdCols[2].trim());
      TransactionId uniqueTx = TransactionId.valueOf(mdCols[3].trim());
      TransferTransaction trans = new TransferTransaction(transBranch.getBranchId(), sourceTx, uniqueTx, type);
      transBranch.addTransferTransaction(trans);
   }

   /*
    * verify all add transactions having the json files verify all prevTx are matched with from db.
    */

   public XResultData Validate(TupleQuery tupleQuery) {
      ArrayList<String> dirs = Lib.readListFromDir(path, null);
      try {
         for (TransferBranch tb : transferBranchList) {
            BranchId branchId = tb.getBranchId();
            //looking for directory
            int index = -1;
            for (int j = 0; j < dirs.size(); j++) {
               if (branchId.toString().equals(dirs.get(j))) {
                  index = j;
                  break;
               }
            }
            if (index == -1) {
               results.error(String.format("Missing %s directory", branchId.toString()));
               return results;
            }
            //looking for json files
            ArrayList<String> tempfiles =
               Lib.readListFromDir(String.format("%s%s%s", path, File.separator, dirs.get(index)), null);
            for (TransferTransaction transTx : tb.getTxList()) {
               String fileString = transTx.getSourceTransId().toString();
               if (!tempfiles.contains(fileString) && transTx.getTransferOp().equals(TransferOpType.ADD)) {
                  results.errorf("Missing %s.json under %s", fileString, tb.getBranchId().toString());
                  return results;
               }

            }
            //Verify prevTX, ImportId,
            List<TransactionId> txIds = new ArrayList<>();
            //tupleQuery.getTuple4E3E4FromE1E2(TupleType, branchId, e1, e2, consumer);
            tupleQuery.getTuple4E3E4FromE1E2(TransferFile, CoreBranches.COMMON, branchId, TransferOpType.PREV_TX,
               (E3, E4) -> {
                  txIds.add(E3);
               });
            if (txIds.isEmpty()) {
               //if there is no preTx add the new prevTx so that it can pass the validation
               //txIds.add(TransationId.valueOf(strManBaseTxId));
               results.errorf("%s", String.format("Can not get Prev_TX of %s from database.", branchId.toString()));
               return results;
            }
            // get max tx from tuple table and check to see if it matches the previous tx from the manifest
            // still TODO

            List<GammaId> tuples = new ArrayList<>();
            //tupleQuery.getTuple4GammaFromE1E2(TransferFile, CoreBranches.COMMON, branchId, TransferOpType.PREV_TX, tuples::add);
            tupleQuery.getTuple4GammaFromE1E2(TransferFile, branchId, branchId, TransferOpType.PREV_TX, tuples::add);

            if (tuples.isEmpty()) {
               //results.errorf("%s", String.format("Can not get gamma Id from %s and Prev_TX.", branchId.toString()));
               //return results; must add it back
               results.log(String.format("Can not get gamma Id from %s.", branchId.toString()));
            } else {
               //transferBranchList.get(i).setGammaID(tuples.get(0));
            }

            //verify export id
            //get export id should be queried by system level and return only one not for every branch ???
            txIds.clear();
            tupleQuery.getTuple4E3E4FromE1E2(ExportedBranch, branchId, exportId, branchId, (E3, E4) -> {
               txIds.add(E3);
            });

            if (txIds.isEmpty()) {
               results.log(String.format("Can not get exportId from database of branch %s.", branchId.toString()));
               //return results; must add it back
            }

         }
      } catch (Exception e) {
         results.errorf("%s",
            String.format("IO Exception while verifying manifest and transaction files. %s", e.getMessage()));
      }

      return results;
   }

   public ArrayList<String> GetAllImportedTransIds() {
      ArrayList<String> ids = new ArrayList<>();
      for (TransferBranch tb : transferBranchList) {
         for (TransferTransaction transTx : tb.getTxList()) {
            TransactionId tx = transTx.getImportedTransId();
            if (tx != null && tx.isValid()) {
               ids.add(tx.getIdString());
            }
         }
      }
      return ids;
   }

   public XResultData PurgeAllImportedTransaction(OrcsApi orcsApi) {
      ArrayList<String> transIdList = GetAllImportedTransIds();
      StringBuilder transIds = new StringBuilder("");
      //Purge up to PURGE_TXS_LIMIT
      try {
         for (int i = 0; i < transIdList.size(); i++) {
            if (i % PURGE_TXS_LIMIT == 0 && transIds.length() != 0) {
               orcsApi.getTransactionFactory().purgeTxs(transIds.toString());
               results.log(String.format("Purged succesfully the transaction ids: %s.", transIds.toString()));
               //transIds.setLength(0);
               transIds = new StringBuilder("");
            }
            if (transIds.length() == 0) {
               transIds.append(transIdList.get(i));
            } else {
               transIds.append(",").append(transIdList.get(i));
            }
         }
         if (transIds.length() != 0) {
            orcsApi.getTransactionFactory().purgeTxs(transIds.toString()); //for test must be removed
            results.log(String.format("Purged succesfully the transaction ids: %s.", transIds.toString()));
         }
      } catch (Exception e) {
         results.errorf("%s", String.format("Roll back failed while purging transaction ids %s.", e.getMessage()));
      }

      return results;
   }

   public XResultData ImportAllTransactions(OrcsApi orcsApi, IResourceManager resourceManager) {
      //import transactions to branchIds
      try {
         for (TransferBranch tb : transferBranchList) {
            BranchId branchId = tb.getBranchId();

            String current = "";
            try {
               for (TransferTransaction transTx : tb.getTxList()) {
                  TransferOpType op = transTx.getTransferOp();
                  if (TransferOpType.PREV_TX.equals(op) || TransferOpType.EMPTY.equals(op)) {
                     continue;
                  }
                  File transFile = new File(String.format("%s%s%s%s%s.json", this.path, File.separator,
                     branchId.toString(), File.separator, transTx.getSourceTransId().toString()));
                  if (transFile.exists()) {
                     current = transFile.getName();
                     String transStr = Lib.fileToString(transFile);
                     TransactionBuilderDataFactory txBdf = new TransactionBuilderDataFactory(orcsApi, resourceManager);
                     TransactionBuilder trans = txBdf.loadFromJson(transStr);
                     TransactionToken token = trans.commit();
                     if (token.isInvalid()) {
                        results.errorf("Failed at %s - %s", branchId.toString(), current);
                        break;
                     }
                     transTx.setImportedTransId(token);
                  }
               }
            } catch (Exception e) {
               results.errorf("Failed at %s - %s.json: %s.", branchId.toString(), current, e.getMessage());
            }

            if (results.isFailed()) {
               break;
            }
         }

      } catch (Exception e) {
         results.error(e.getMessage());
      }
      return results;
   }

   public XResultData UpdatePrevTXs(OrcsApi orcsApi) {
      try {
         TransactionBuilder txDel =
            orcsApi.getTransactionFactory().createTransaction(CoreBranches.COMMON, "Delete Tuple4 Prev TX");
         for (TransferBranch tb : transferBranchList) {
            BranchId branchId = tb.getBranchId();
            GammaId id = tb.getGammaId();
            if (id != null) {
               txDel.deleteTuple4(id);
            }
         }
         txDel.commit();
         //         TransferTransaction transTx = null;
         //         TransactionBuilder txUpdt =
         //            orcsApi.getTransactionFactory().createTransaction(CoreBranches.COMMON, "Update Tuple4 Prev TX");
         //         txUpdt.commit();
      } catch (Exception e) {
         results.error(String.format("Error while Updating PrevTX to database %s", e.getMessage()));
      }
      return results;
   }

   public void setExportID(TransactionId exportId) {
      this.exportId = exportId;
   }

   public void setBuildID(TransactionId buildId) {
      this.buildId = buildId;
   }
}
