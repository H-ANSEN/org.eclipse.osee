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
package org.eclipse.osee.framework.skynet.core.artifact.search;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import static org.eclipse.osee.framework.core.enums.DeletionFlag.INCLUDE_DELETED;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.junit.Ignore;

/**
 * @author Andrew M. Finkbeiner
 */
public class ArtifactQueryPerformanceTests {

   /*
    * constructors to test no good way to get id's for test so we son't test these two. public ArtifactQueryBuilder(int
    * artId, Branch branch, boolean allowDeleted, ArtifactLoad loadLevel) { this(null, artId, null, null, null, branch,
    * allowDeleted, loadLevel, true); } public ArtifactQueryBuilder(Collection<Integer> artifactIds, Branch branch,
    * boolean allowDeleted, ArtifactLoad loadLevel) { this(artifactIds, 0, null, null, null, branch, allowDeleted,
    * loadLevel, true); emptyCriteria = artifactIds.isEmpty(); } public ArtifactQueryBuilder(Branch branch, ArtifactLoad
    * loadLevel, boolean allowDeleted, AbstractArtifactSearchCriteria... criteria) { this(null, 0, null, null, null,
    * branch, allowDeleted, loadLevel, true, criteria); emptyCriteria = criteria.length == 0; } public
    * ArtifactQueryBuilder(Branch branch, ArtifactLoad loadLevel, List<AbstractArtifactSearchCriteria> criteria) {
    * this(null, 0, null, null, null, branch, false, loadLevel, true, toArray(criteria)); emptyCriteria =
    * criteria.isEmpty(); } public ArtifactQueryBuilder(ArtifactType artifactType, Branch branch, ArtifactLoad
    * loadLevel, AbstractArtifactSearchCriteria... criteria) { this(null, 0, null, null, Arrays.asList(artifactType),
    * branch, false, loadLevel, true, criteria); emptyCriteria = criteria.length == 0; } public
    * ArtifactQueryBuilder(ArtifactType artifactType, Branch branch, ArtifactLoad loadLevel,
    * List<AbstractArtifactSearchCriteria> criteria) { this(null, 0, null, null, Arrays.asList(artifactType), branch,
    * false, loadLevel, true, toArray(criteria)); emptyCriteria = criteria.isEmpty(); }
    */

   @org.junit.Test
   public void testGetArtifactByHRID() throws OseeCoreException {
      Branch common = BranchManager.getCommonBranch();
      Artifact art = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(common);
      long startTime = System.currentTimeMillis();
      Artifact result = ArtifactQuery.getArtifactFromId(art.getHumanReadableId(), common, INCLUDE_DELETED);
      long elapsedTime = System.currentTimeMillis() - startTime;
      System.out.println(String.format("testGetArtifactByHRID took %dms", elapsedTime));
      assertNotNull("No artifact found", result);
      assertTrue(String.format("Elapsed time for artifact by hrid query took %dms.  It should take less than 100ms.",
         elapsedTime), elapsedTime < 100);
   }

   @org.junit.Test
   public void testGetArtifactsByHRID() throws OseeCoreException {
      Branch common = BranchManager.getCommonBranch();
      Artifact art = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(common);
      List<String> hrids = new ArrayList<String>();
      List<Artifact> children = art.getChildren();
      for (Artifact child : children) {
         hrids.add(child.getHumanReadableId());
      }
      long startTime = System.currentTimeMillis();
      List<Artifact> result = ArtifactQuery.getArtifactListFromIds(hrids, common, INCLUDE_DELETED);
      long elapsedTime = System.currentTimeMillis() - startTime;
      System.out.println(String.format("testGetArtifactsByHRID took %dms for %d artifacts", elapsedTime, result.size()));
      assertTrue("No artifacts found", result.size() > 0);
      assertTrue(String.format("Elapsed time for artifact by hrid query took %dms.  It should take less than 180ms.",
         elapsedTime), elapsedTime < 180);
   }

   @org.junit.Test
   public void testGetArtifactsByHRIDNoDeleted() throws OseeCoreException {
      Branch common = BranchManager.getCommonBranch();
      Artifact art = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(common);
      List<String> hrids = new ArrayList<String>();
      List<Artifact> children = art.getChildren();
      for (Artifact child : children) {
         hrids.add(child.getHumanReadableId());
      }
      long startTime = System.currentTimeMillis();
      List<Artifact> result = ArtifactQuery.getArtifactListFromIds(hrids, common, EXCLUDE_DELETED);
      long elapsedTime = System.currentTimeMillis() - startTime;
      System.out.println(String.format("testGetArtifactsByHRIDNoDeleted took %dms for %d artifacts", elapsedTime,
         result.size()));
      assertTrue("No artifacts found", result.size() > 0);
      assertTrue(String.format("Elapsed time for artifact by hrid query took %dms.  It should take less than 130ms.",
         elapsedTime), elapsedTime < 130);
   }

   @org.junit.Test
   public void testGetArtifactsByArtType() throws OseeCoreException {
      long startTime = System.currentTimeMillis();
      List<Artifact> result =
         ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.WorkFlowDefinition, BranchManager.getCommonBranch());
      long elapsedTime = System.currentTimeMillis() - startTime;
      System.out.println(String.format("testGetArtifactsByArtType took %dms for %d artifacts", elapsedTime,
         result.size()));
      assertTrue("No artifacts found", result.size() > 0);
      assertTrue(String.format(
         "Elapsed time for testGetArtifactsByArtType took %dms.  It should take less than 750ms.", elapsedTime),
         elapsedTime < 750);
   }

   @org.junit.Test
   public void testGetArtifactsByArtTypes() throws OseeCoreException {
      internalTestGetArtifactsByArtTypes(false, 8000);
   }

   private void internalTestGetArtifactsByArtTypes(boolean allowDeleted, long expectedElapseTime) throws OseeCoreException {
      Branch common = BranchManager.getCommonBranch();
      List<? extends IArtifactType> artTypes =
         Arrays.asList(CoreArtifactTypes.GeneralDocument, CoreArtifactTypes.Folder,
            CoreArtifactTypes.WorkFlowDefinition, CoreArtifactTypes.User, CoreArtifactTypes.WorkPageDefinition,
            CoreArtifactTypes.WorkRuleDefinition);

      long startTime = System.currentTimeMillis();
      List<Artifact> result = ArtifactQuery.getArtifactListFromTypes(artTypes, common, EXCLUDE_DELETED);
      long elapsedTime = System.currentTimeMillis() - startTime;

      System.out.println(String.format("testGetArtifactsByArtTypes took %dms for %d artifacts", elapsedTime,
         result.size()));
      assertTrue("No artifacts found", result.size() > 0);
      assertTrue(String.format(
         "Elapsed time for testGetArtifactsByArtTypes took %dms to load %d artifacts.  It should take less than %dms.",
         elapsedTime, result.size(), expectedElapseTime), elapsedTime < expectedElapseTime);
   }

   @org.junit.Test
   public void testGetArtifactsByArtTypesAllowDeleted() throws OseeCoreException {
      internalTestGetArtifactsByArtTypes(true, 5000);
   }

   @org.junit.Test
   @Ignore
   public void testLoadAllBranch() throws OseeCoreException {
      Branch common = BranchManager.getCommonBranch();
      long startTime = System.currentTimeMillis();
      List<Artifact> result = ArtifactQuery.getArtifactListFromBranch(common, EXCLUDE_DELETED);
      long elapsedTime = System.currentTimeMillis() - startTime;
      System.out.println(String.format("loadAllBranch took %dms for %d artifacts", elapsedTime, result.size()));
      assertTrue("No artifacts found", result.size() > 0);
      assertTrue(
         String.format("Elapsed time for loadAllBranch took %dms.  It should take less than 700000ms.", elapsedTime),
         elapsedTime < 700000);

      // check for exceptions
      assertTrue(result.size() > 0);
      for (Artifact artifact : result) {
         assertTrue(artifact.getName().length() > 0);
         artifact.isOrphan();
      }
   }
}
