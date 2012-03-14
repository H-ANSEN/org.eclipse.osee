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
package org.eclipse.osee.framework.ui.skynet.test.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.ui.skynet.dbHealth.DatabaseHealthOperation;
import org.eclipse.osee.framework.ui.skynet.dbHealth.DatabaseHealthOpsExtensionManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Roberto E. Escobar
 */
@RunWith(Parameterized.class)
public class DatabaseIntegrityTest {

   private final String operationId;

   public DatabaseIntegrityTest(String operationId) {
      this.operationId = operationId;
   }

   @Test
   @Ignore
   public void testDatabaseIntegrity() {
      DatabaseHealthOperation operation = DatabaseHealthOpsExtensionManager.getVerifyOperationByName(operationId);

      assertNotNull(operation);

      operation.setFixOperationEnabled(false);
      testOperation(operation, IStatus.OK);

      int totalItemsToFix = operation.getItemsToFixCount();
      assertEquals(String.format("Error [%s]: found [%s] items", operation.getName(), totalItemsToFix), 0,
         totalItemsToFix);
   }

   @Parameters
   public static Collection<Object[]> data() {
      Collection<Object[]> data = new ArrayList<Object[]>();

      for (String verifyOpId : DatabaseHealthOpsExtensionManager.getVerifyOperationNames()) {
         if (Strings.isValid(verifyOpId)) {
            data.add(new Object[] {verifyOpId});
         }
      }
      return data;
   }

   public static IStatus testOperation(IOperation operation, int expectedSeverity) {
      IStatus status = Operations.executeWork(operation);
      Assert.assertEquals(status.toString(), expectedSeverity, status.getSeverity());
      return status;
   }

}
