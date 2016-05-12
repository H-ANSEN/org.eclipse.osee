/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.rest.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.data.BranchReadable;
import org.eclipse.osee.orcs.data.TransactionReadable;
import org.eclipse.osee.orcs.rest.model.Branch;
import org.eclipse.osee.orcs.rest.model.Transaction;

/**
 * @author Roberto E. Escobar
 */
public final class OrcsRestUtil {

   private OrcsRestUtil() {
      // Utility class
   }

   public static <T> T executeCallable(Callable<T> callable) {
      try {
         return callable.call();
      } catch (Exception ex) {
         throw OseeCoreException.wrap(ex);
      }
   }

   public static Response asResponse(boolean modified) {
      ResponseBuilder builder;
      if (modified) {
         builder = Response.ok();
      } else {
         builder = Response.notModified();
      }
      return builder.build();
   }

   public static List<Transaction> asTransactions(ResultSet<? extends TransactionReadable> results) {
      List<Transaction> toReturn = new ArrayList<>(results.size());
      for (TransactionReadable data : results) {
         toReturn.add(asTransaction(data));
      }
      return toReturn;
   }

   public static List<Branch> asBranches(ResultSet<? extends BranchReadable> results) {
      List<Branch> toReturn = new ArrayList<>(results.size());
      for (BranchReadable data : results) {
         toReturn.add(asBranch(data));
      }
      return toReturn;
   }

   public static Branch asBranch(BranchReadable src) {
      Branch data = new Branch();
      data.setArchiveState(src.getArchiveState());
      data.setAssociatedArtifactId(src.getAssociatedArtifactId());
      data.setBaseTransactionId(src.getBaseTransaction());
      data.setBranchState(src.getBranchState());
      data.setBranchType(src.getBranchType());
      data.setInheritAccessControl(src.isInheritAccessControl());
      data.setName(src.getName());
      data.setParentBranch(src.getParentBranch());
      data.setSourceTransactionId(src.getSourceTransaction());
      data.setBranchUuid(src.getGuid());
      return data;
   }

   public static Transaction asTransaction(TransactionReadable tx) {
      Transaction data = new Transaction();
      data.setTxId(tx);
      data.setAuthorId(tx.getAuthorId());
      data.setBranchUuid(tx.getBranchId());
      data.setComment(tx.getComment());
      data.setCommitArtId(tx.getCommit());
      data.setTimeStamp(tx.getDate());
      data.setTxType(tx.getTxType());
      return data;
   }

   public static List<Long> asLongList(String rawValue) {
      List<Long> toReturn;
      if (Strings.isValid(rawValue)) {
         String[] entries = rawValue.split(",");
         toReturn = new ArrayList<>();
         for (String entry : entries) {
            Long value = Long.parseLong(entry.trim());
            toReturn.add(value);
         }
      } else {
         toReturn = Collections.emptyList();
      }
      return toReturn;
   }
}
