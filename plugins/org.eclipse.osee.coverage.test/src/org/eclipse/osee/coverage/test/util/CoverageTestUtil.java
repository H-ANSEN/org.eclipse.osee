/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.coverage.test.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageOption;
import org.eclipse.osee.coverage.model.CoveragePackageBase;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.store.CoverageArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.PurgeArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.StaticIdManager;
import org.eclipse.osee.support.test.util.DemoSawBuilds;

/**
 * @author Donald G. Dunne
 */
public class CoverageTestUtil {
   private static String COVERAGE_STATIC_ID = "coverage.artifact";

   public static void cleanupCoverageTests() throws OseeCoreException {
      try {
         new PurgeArtifacts(getAllCoverageArtifacts()).execute();
      } catch (ArtifactDoesNotExist ex) {
         // do nothing
      }
   }

   /**
    * Adds the static id to the artifact to ensure that test cleans (purges) this artifact after completion.
    */
   public static void registerAsTestArtifact(Artifact artifact) throws OseeCoreException {
      registerAsTestArtifact(artifact, false);
   }

   /**
    * Adds the static id to the artifact to ensure that test cleans (purges) this artifact after completion.
    */
   public static void registerAsTestArtifact(Artifact artifact, boolean recurse) throws OseeCoreException {
      StaticIdManager.setSingletonAttributeValue(artifact, CoverageTestUtil.COVERAGE_STATIC_ID);
      if (recurse) {
         for (Artifact childArt : artifact.getChildren()) {
            if (childArt.isOfType(CoverageArtifactTypes.CoveragePackage, CoverageArtifactTypes.CoveragePackage)) {
               registerAsTestArtifact(childArt, recurse);
            }
         }
      }
   }

   public static ICoverage getFirstCoverageByName(CoveragePackageBase coveragePackageBase, String name) {
      for (ICoverage coverage : coveragePackageBase.getChildren(true)) {
         if (coverage.getName().equals(name)) {
            return coverage;
         }
      }
      return null;
   }

   public static Collection<Artifact> getAllCoverageArtifacts() throws OseeCoreException {
      List<Artifact> artifacts = new ArrayList<Artifact>();
      artifacts.addAll(getCoveragePackageArtifacts());
      artifacts.addAll(getCoverageUnitArtifacts());
      artifacts.addAll(getCoverageRecordArtifacts());
      return artifacts;
   }

   public static Collection<Artifact> getCoverageUnitArtifacts() throws OseeCoreException {
      return StaticIdManager.getArtifactsFromArtifactQuery(CoverageArtifactTypes.CoverageUnit, COVERAGE_STATIC_ID,
         CoverageTestUtil.getTestBranch());
   }

   public static Collection<Artifact> getCoveragePackageArtifacts() throws OseeCoreException {
      return StaticIdManager.getArtifactsFromArtifactQuery(CoverageArtifactTypes.CoveragePackage, COVERAGE_STATIC_ID,
         CoverageTestUtil.getTestBranch());
   }

   public static Collection<Artifact> getCoverageRecordArtifacts() throws OseeCoreException {
      return StaticIdManager.getArtifactsFromArtifactQuery(CoreArtifactTypes.GeneralDocument, COVERAGE_STATIC_ID,
         CoverageTestUtil.getTestBranch());
   }

   public static void setAllCoverageMethod(CoverageUnit coverageUnit, CoverageOption CoverageOption, boolean recurse) {
      for (CoverageItem item : coverageUnit.getCoverageItems(recurse)) {
         item.setCoverageMethod(CoverageOption);
      }
   }

   public static Branch getTestBranch() {
      try {
         return BranchManager.getBranch(DemoSawBuilds.SAW_Bld_1);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return null;
   }
}
