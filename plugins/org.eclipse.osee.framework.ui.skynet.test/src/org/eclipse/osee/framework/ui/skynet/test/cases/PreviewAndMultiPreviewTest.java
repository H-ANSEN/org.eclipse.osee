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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.IHealthStatus;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.util.FrameworkTestUtil;
import org.eclipse.osee.framework.ui.skynet.render.ITemplateRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.render.RenderingUtil;
import org.eclipse.osee.framework.ui.skynet.render.WholeWordRenderer;
import org.eclipse.osee.framework.ui.skynet.render.WordTemplateRenderer;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Megumi Telles
 */
public class PreviewAndMultiPreviewTest {

   private static final IOseeBranch BRANCH = DemoSawBuilds.SAW_Bld_1;

   @BeforeClass
   public static void testSetUp() throws OseeCoreException {
      Assert.assertFalse("Not to be run on production datbase.", TestUtil.isProductionDb());
      RenderingUtil.setPopupsAllowed(false);
   }

   @Before
   public void setUp() throws Exception {
      cleanup();
   }

   @After
   public void tearDown() throws Exception {
      cleanup();
   }

   private void cleanup() throws Exception {
      SevereLoggingMonitor monitorLog = TestUtil.severeLoggingStart();
      FrameworkTestUtil.cleanupSimpleTest(BRANCH, PreviewAndMultiPreviewTest.class.getSimpleName());
      TestUtil.severeLoggingEnd(monitorLog);
   }

   private static Artifact createArtifact(IArtifactType type, IOseeBranch branch, String name) throws OseeCoreException {
      Artifact artifact = ArtifactTypeManager.addArtifact(type, branch, name);
      Assert.assertNotNull(artifact);
      return artifact;
   }

   /*
    * Preview Requirements Artifact (includes a child artifact == general document but should not invoke a warning since
    * only previewing (no recurse)).
    */
   @org.junit.Test
   public void testPreview() throws Exception {
      Template template = new Template() {

         @Override
         protected void testBody() throws OseeCoreException {
            Artifact parentArtifact =
               createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH,
                  PreviewAndMultiPreviewTest.class.getSimpleName());

            Artifact childArt = createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH, addPrefix("1a"));
            parentArtifact.addChild(childArt);

            parentArtifact.persist(getClass().getSimpleName());

            WordTemplateRenderer renderer = new WordTemplateRenderer();
            renderer.open(Arrays.asList(parentArtifact), PresentationType.PREVIEW);
         }
      };
      template.test();
   }

   @org.junit.Test
   public void testPreviewUsingRendererManager() throws Exception {
      Template template = new Template() {

         @Override
         protected void testBody() throws OseeCoreException {
            Artifact parentArtifact =
               createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH,
                  PreviewAndMultiPreviewTest.class.getSimpleName());

            Artifact childArt = createArtifact(CoreArtifactTypes.GeneralDocument, BRANCH, addPrefix("1b"));
            parentArtifact.addChild(childArt);

            parentArtifact.persist(getClass().getSimpleName());

            RendererManager.openInJob(Arrays.asList(parentArtifact), PresentationType.PREVIEW);
         }
      };
      template.test();
   }

   /*
    * Preview Requirements Artifact with valid children.
    */
   @org.junit.Test
   public void testPreviewWithChildren() throws Exception {
      Template template = new Template() {

         @Override
         protected void testBody() throws OseeCoreException {
            Artifact parentArtifact =
               createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH,
                  PreviewAndMultiPreviewTest.class.getSimpleName());

            Artifact childArt = createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH, addPrefix("1c"));
            parentArtifact.addChild(childArt);

            parentArtifact.persist(getClass().getSimpleName());

            RendererManager.open(parentArtifact, PresentationType.PREVIEW,
               ITemplateRenderer.PREVIEW_WITH_RECURSE_OPTION_PAIR);
         }
      };
      template.test();
   }

   @org.junit.Test
   public void testPreviewWithChildrenUsingRendererManager() throws Exception {
      Template template = new Template() {

         @Override
         protected void testBody() throws OseeCoreException {
            Artifact parentArtifact =
               createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH,
                  PreviewAndMultiPreviewTest.class.getSimpleName());

            Artifact childArt = createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH, addPrefix("1d"));
            parentArtifact.addChild(childArt);

            parentArtifact.persist(getClass().getSimpleName());

            RendererManager.openInJob(Arrays.asList(parentArtifact), PresentationType.PREVIEW);
         }
      };
      template.test();
   }

   @org.junit.Test
   public void testPreviewWithChildrenFault() throws Exception {
      SevereLoggingMonitor monitorLog = TestUtil.severeLoggingStart();

      Artifact parentArtifact =
         createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH, PreviewAndMultiPreviewTest.class.getSimpleName());

      Artifact childArt =
         createArtifact(CoreArtifactTypes.GeneralDocument, BRANCH,
            PreviewAndMultiPreviewTest.class.getSimpleName() + ".1e");
      parentArtifact.addChild(childArt);

      parentArtifact.persist(getClass().getSimpleName());

      RendererManager.open(parentArtifact, PresentationType.PREVIEW, ITemplateRenderer.PREVIEW_WITH_RECURSE_OPTION_PAIR);

      // should get one warning since the child is a general document
      Collection<IHealthStatus> warnings = monitorLog.getLogsAtLevel(Level.WARNING);
      Assert.assertEquals(Collections.toString(", ", warnings), 1, warnings.size());

      IHealthStatus status = warnings.iterator().next();
      String warningMsg = status.getMessage();
      Assert.assertTrue(warningMsg.contains("You chose to preview/edit artifacts that could not be handled"));
      Assert.assertEquals(0, TestUtil.getNumberOfLogsAtLevel(monitorLog, Level.SEVERE));
   }

   /*
    * No warning expected in this fault case because the Renderer Manager resolves which render to use to preview.
    */
   @org.junit.Test
   public void testPreviewWithChildrenUsingRendererManagerFault() throws Exception {
      Template template = new Template() {

         @Override
         protected void testBody() throws OseeCoreException {
            Artifact parentArtifact =
               createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH,
                  PreviewAndMultiPreviewTest.class.getSimpleName());

            Artifact childArt = createArtifact(CoreArtifactTypes.GeneralDocument, BRANCH, addPrefix("1f"));
            parentArtifact.addChild(childArt);

            parentArtifact.persist(getClass().getSimpleName());

            RendererManager.openInJob(Arrays.asList(parentArtifact), PresentationType.PREVIEW);
         }
      };
      template.test();
   }

   /*
    * Preview multiple Requirement Artifacts
    */
   @org.junit.Test
   public void testMultiPreview() throws Exception {
      Template template = new Template() {

         @Override
         protected void testBody() throws OseeCoreException {
            Artifact multiArt1 = createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH, addPrefix("3z"));
            multiArt1.persist(getClass().getSimpleName());
            Artifact multiArt2 = createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH, addPrefix("2y"));
            multiArt2.persist(getClass().getSimpleName());
            Artifact multiArt3 = createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH, addPrefix("1x"));
            multiArt3.persist(getClass().getSimpleName());

            WordTemplateRenderer renderer = new WordTemplateRenderer();
            List<Artifact> newMultiArts = Arrays.asList(multiArt1, multiArt2, multiArt3);
            renderer.open(newMultiArts, PresentationType.PREVIEW);
         }
      };
      template.test();
   }

   @org.junit.Test
   public void testMultiPreviewUsingRendererManager() throws Exception {
      Template template = new Template() {

         @Override
         protected void testBody() throws OseeCoreException {
            Artifact parentArtifact =
               createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH,
                  PreviewAndMultiPreviewTest.class.getSimpleName());

            Artifact multiArt1 = createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH, addPrefix("3o"));
            parentArtifact.addChild(multiArt1);

            Artifact multiArt2 = createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH, addPrefix("2n"));
            parentArtifact.addChild(multiArt2);

            Artifact multiArt3 = createArtifact(CoreArtifactTypes.SoftwareRequirement, BRANCH, addPrefix("1m"));
            parentArtifact.addChild(multiArt3);

            parentArtifact.persist(getClass().getSimpleName());

            RendererManager.open(parentArtifact, PresentationType.PREVIEW);
         }
      };
      template.test();
   }

   /*
    * Preview a whole word doc
    */
   @org.junit.Test
   public void testWholeWordPreview() throws Exception {
      Template template = new Template() {

         @Override
         protected void testBody() throws OseeCoreException {
            Artifact art = createArtifact(CoreArtifactTypes.TestProcedureWML, BRANCH, addPrefix("4g"));
            art.persist(getClass().getSimpleName());
            WholeWordRenderer renderer = new WholeWordRenderer();
            renderer.open(Arrays.asList(art), PresentationType.PREVIEW);
         }

      };
      template.test();
   }

   @org.junit.Test
   public void testWholeWordPreviewUsingRendererManager() throws Exception {
      Template template = new Template() {

         @Override
         protected void testBody() throws OseeCoreException {
            Artifact art = createArtifact(CoreArtifactTypes.TestProcedureWML, BRANCH, addPrefix("4h"));
            art.persist(getClass().getSimpleName());

            RendererManager.openInJob(Arrays.asList(art), PresentationType.PREVIEW);
         }

      };
      template.test();
   }

   private static abstract class Template {

      public void test() throws Exception {
         SevereLoggingMonitor monitorLog = TestUtil.severeLoggingStart();
         testBody();
         Assert.assertTrue(monitorLog.getLogsAtLevel(Level.WARNING).isEmpty());
         Assert.assertTrue(monitorLog.getLogsAtLevel(Level.SEVERE).isEmpty());
         TestUtil.severeLoggingStop(monitorLog);
      }

      public String addPrefix(String name) {
         return String.format("%s.%s", Template.class.getName(), name);
      }

      protected abstract void testBody() throws OseeCoreException;
   }
}
