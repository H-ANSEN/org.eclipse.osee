/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http:www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.test;

import static org.junit.Assert.assertTrue;
import junit.framework.Assert;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.framework.plugin.core.util.OseeData;
import org.eclipse.osee.framework.ui.skynet.test.artifact.ArtifactPromptChangeTest;
import org.eclipse.osee.framework.ui.skynet.test.blam.BlamXWidgetTest;
import org.eclipse.osee.framework.ui.skynet.test.blam.operation.EmailGroupsBlamTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.ArtifactPasteConfigurationTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.ArtifactPasteOperationTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.AttributeTypeEditPresenterTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.InterArtifactDropTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.PreviewAndMultiPreviewTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.RelationOrderRendererTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.ResultsEditorConverterTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.ViewWordChangeAndDiffTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.WordArtifactElementExtractorTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.WordEditTest;
import org.eclipse.osee.framework.ui.skynet.test.cases.WordTrackedChangesTest;
import org.eclipse.osee.framework.ui.skynet.test.dbHealth.RelationIntegrityCheckTest;
import org.eclipse.osee.framework.ui.skynet.test.importer.ImportTestSuite;
import org.eclipse.osee.framework.ui.skynet.test.renderer.RendererTestSuite;
import org.eclipse.osee.framework.ui.skynet.test.renderer.imageDetection.WordImageCompareTest;
import org.eclipse.osee.framework.ui.skynet.test.util.enumeration.AbstractEnumerationTest;
import org.eclipse.osee.framework.ui.skynet.test.widgets.workflow.WorkPageAdapterTest;
import org.eclipse.osee.framework.ui.skynet.test.render.word.WordTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   WordImageCompareTest.class,
BlamXWidgetTest.class,
   EmailGroupsBlamTest.class,
   ResultsEditorConverterTest.class,
   ArtifactPasteConfigurationTest.class,
   ArtifactPasteOperationTest.class,
   RelationOrderRendererTest.class,
   InterArtifactDropTest.class,
   WordEditTest.class,
   WordTrackedChangesTest.class,
   PreviewAndMultiPreviewTest.class,
   ViewWordChangeAndDiffTest.class,
   WordArtifactElementExtractorTest.class,
   AttributeTypeEditPresenterTest.class,
   ArtifactPromptChangeTest.class,
   RendererTestSuite.class,
   WorkPageAdapterTest.class,
   AbstractEnumerationTest.class,
   ImportTestSuite.class,
   WordTestSuite.class,
   RelationIntegrityCheckTest.class})
/**
 * @author Donald G. Dunne
 */
public class FrameworkUi_Demo_Suite {
   @BeforeClass
   public static void setUp() throws Exception {
      assertTrue("Demo Application Server must be running.",
         ClientSessionManager.getAuthenticationProtocols().contains("demo"));
      assertTrue("Client must authenticate using demo protocol",
         ClientSessionManager.getSession().getAuthenticationProtocol().equals("demo"));
      OseeProperties.setIsInTest(true);
      System.out.println("\n\nBegin " + FrameworkUi_Demo_Suite.class.getSimpleName());
      Assert.assertTrue("osee.data project should be open", OseeData.isProjectOpen());
   }

   @AfterClass
   public static void tearDown() throws Exception {
      Assert.assertTrue("osee.data project should be open", OseeData.isProjectOpen());
      System.out.println("End " + FrameworkUi_Demo_Suite.class.getSimpleName());
   }
}
