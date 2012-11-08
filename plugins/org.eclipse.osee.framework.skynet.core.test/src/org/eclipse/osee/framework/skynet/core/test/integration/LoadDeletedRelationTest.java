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
package org.eclipse.osee.framework.skynet.core.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.test.integration.utils.FrameworkTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Ryan Schmitt
 */
public class LoadDeletedRelationTest {
   Branch branch;
   Artifact left, right;
   IRelationType type;

   @Before
   public void setUp() throws OseeCoreException {
      branch = BranchManager.getBranch("SAW_Bld_2");
      left = FrameworkTestUtil.createSimpleArtifact(CoreArtifactTypes.Requirement, "Left", branch);
      right = FrameworkTestUtil.createSimpleArtifact(CoreArtifactTypes.Requirement, "Right", branch);
      left.persist(getClass().getSimpleName());
      right.persist(getClass().getSimpleName());
      type = CoreRelationTypes.Requirement_Trace__Higher_Level;
   }

   @Ignore
   //not implemented  in the code
   @Test
   public void loadDeletedRelationTest() throws OseeCoreException {
      RelationManager.addRelation(type, left, right, "");
      left.persist(getClass().getSimpleName());
      RelationLink loaded = RelationManager.getLoadedRelation(type, left.getArtId(), right.getArtId(), branch);
      int oldGammaId = loaded.getGammaId();
      RelationManager.deleteRelation(type, left, right);
      left.persist(getClass().getSimpleName());
      RelationManager.addRelation(type, left, right, "");
      left.persist(getClass().getSimpleName());

      List<RelationLink> links = RelationManager.getRelationsAll(left, DeletionFlag.INCLUDE_DELETED);
      int linkCount = 0;
      for (RelationLink link : links) {
         if (link.getRelationType().getName().equals("Requirement Trace")) {
            linkCount++;
         }
      }

      int newGammaId = loaded.getGammaId();
      assertEquals("Deleted relation was not re-used by addRelation; see L3778", 1, linkCount);
      assertFalse(loaded.isDeleted());
      assertEquals("Gamma ID was changed;", oldGammaId, newGammaId);
   }

   @After
   public void tearDown() throws OseeCoreException {
      left.purgeFromBranch();
      right.purgeFromBranch();
   }
}
