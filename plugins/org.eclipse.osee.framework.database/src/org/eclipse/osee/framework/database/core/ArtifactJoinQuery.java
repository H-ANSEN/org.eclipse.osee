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
package org.eclipse.osee.framework.database.core;

import org.eclipse.osee.framework.database.core.DatabaseJoinAccessor.JoinItem;

/**
 * @author Roberto E. Escobar
 */
public final class ArtifactJoinQuery extends AbstractJoinQuery {

   private final class Entry implements IJoinRow {
      private final int artId;
      private final int branchId;
      private final int transactionId;

      private Entry(Integer artId, Integer branchId, Integer transactionId) {
         this.artId = artId;
         this.branchId = branchId;
         this.transactionId = transactionId;
      }

      @Override
      public Object[] toArray() {
         return new Object[] {getQueryId(), getInsertTime(), artId, branchId, transactionId};
      }

      @Override
      public String toString() {
         return String.format("art_id=%s, branch_id=%s, transaction_id=%s", artId, branchId, transactionId);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         }
         if (!(obj instanceof Entry)) {
            return false;
         }
         Entry other = (Entry) obj;
         return other.artId == this.artId && other.branchId == this.branchId && other.transactionId == this.transactionId;
      }

      @Override
      public int hashCode() {
         return 37 * artId * branchId * transactionId;
      }
   }

   protected ArtifactJoinQuery(IJoinAccessor joinAccessor, int queryId) {
      super(joinAccessor, JoinItem.ARTIFACT, queryId);
   }

   public void add(Integer art_id, Integer branchId, Integer transactionId) {
      entries.add(new Entry(art_id, branchId, transactionId));
   }

}