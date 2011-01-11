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
package org.eclipse.osee.ats.test;

import static org.junit.Assert.assertTrue;
import org.eclipse.osee.ats.test.artifact.log.AtsLogTest;
import org.eclipse.osee.ats.test.artifact.log.LogItemTest;
import org.eclipse.osee.ats.test.artifact.note.AtsNoteTest;
import org.eclipse.osee.ats.test.artifact.note.NoteItemTest;
import org.eclipse.osee.ats.test.config.AtsBranchConfigurationTest;
import org.eclipse.osee.ats.test.editor.SMAPrintTest;
import org.eclipse.osee.ats.test.health.AtsValidateAtsDatabaseTest;
import org.eclipse.osee.ats.test.navigate.AtsNavigateItemsToMassEditorTest;
import org.eclipse.osee.ats.test.navigate.AtsNavigateItemsToTaskEditorTest;
import org.eclipse.osee.ats.test.navigate.AtsNavigateItemsToWorldViewTest;
import org.eclipse.osee.ats.test.render.RendererManagerTest;
import org.eclipse.osee.ats.test.util.AtsDeleteManagerTest;
import org.eclipse.osee.ats.test.util.AtsImageTest;
import org.eclipse.osee.ats.test.util.AtsNotifyUsersTest;
import org.eclipse.osee.ats.test.util.AtsPurgeTest;
import org.eclipse.osee.ats.test.util.AtsXWidgetsExampleBlamTest;
import org.eclipse.osee.ats.test.util.ImportActionsViaSpreadsheetTest;
import org.eclipse.osee.ats.test.workflow.SMAPromptChangeStatusTest;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   AtsNavigateItemsToMassEditorTest.class,
   AtsNavigateItemsToTaskEditorTest.class,
   AtsNavigateItemsToWorldViewTest.class,
   ImportActionsViaSpreadsheetTest.class,
   AtsTest_Demo_Column_Suite.class,
   LogItemTest.class,
   AtsLogTest.class,
   NoteItemTest.class,
   AtsNoteTest.class,
   RendererManagerTest.class,
   SMAPrintTest.class,
   AtsImageTest.class,
   SMAPromptChangeStatusTest.class,
   AtsDeleteManagerTest.class,
   AtsPurgeTest.class,
   AtsNotifyUsersTest.class,
   AtsBranchConfigurationTest.class,
   AtsValidateAtsDatabaseTest.class,
   AtsXWidgetsExampleBlamTest.class})
/**
 * @author Donald G. Dunne
 */
public class AtsTest_Demo_Suite {
   @BeforeClass
   public static void setUp() throws Exception {
      assertTrue("Demo Application Server must be running.",
         ClientSessionManager.getAuthenticationProtocols().contains("demo"));
      assertTrue("Client must authenticate using demo protocol",
         ClientSessionManager.getSession().getAuthenticationProtocol().equals("demo"));
   }
}
