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
package org.eclipse.osee.ats;

import org.eclipse.osee.ats.config.copy.ConfigDataTest;
import org.eclipse.osee.ats.config.copy.CopyAtsConfigurationOperationTest;
import org.eclipse.osee.ats.config.copy.CopyAtsUtilTest;
import org.eclipse.osee.ats.config.copy.CopyAtsValidationTest;
import org.eclipse.osee.ats.core.client.config.AtsCoreClient_Config_Demo_PT_Suite;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConfigDataTest.class,
   CopyAtsUtilTest.class,
   CopyAtsValidationTest.class,
   CopyAtsConfigurationOperationTest.class,
   AtsCoreClient_Config_Demo_PT_Suite.class})
/**
 * This test suite contains test that can be run against any production db
 * 
 * @author Donald G. Dunne
 */
public class AtsTest_Demo_Config_Suite {
   @BeforeClass
   public static void setUp() throws Exception {
      OseeProperties.setIsInTest(true);
      System.out.println("\n\nBegin " + AtsTest_Demo_Config_Suite.class.getSimpleName());
   }

   @AfterClass
   public static void tearDown() throws Exception {
      System.out.println("End " + AtsTest_Demo_Config_Suite.class.getSimpleName());
   }
}
