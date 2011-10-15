/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.db.internal.accessor;

import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.model.IOseeStorable;
import org.eclipse.osee.framework.core.model.cache.IOseeDataAccessor;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.IOseeSequence;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractDatabaseAccessor<K, T extends IOseeStorable> implements IOseeDataAccessor<K, T> {

   private final IOseeDatabaseService databaseService;

   protected AbstractDatabaseAccessor(IOseeDatabaseService databaseService) {
      this.databaseService = databaseService;
   }

   protected IOseeDatabaseService getDatabaseService() {
      return databaseService;
   }

   protected IOseeSequence getSequence() throws OseeDataStoreException {
      return getDatabaseService().getSequence();
   }
}
