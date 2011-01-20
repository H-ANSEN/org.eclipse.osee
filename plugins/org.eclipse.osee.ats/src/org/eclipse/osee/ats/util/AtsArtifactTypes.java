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
package org.eclipse.osee.ats.util;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.NamedIdentity;

public class AtsArtifactTypes extends NamedIdentity implements IArtifactType {

   // @formatter:off
   public static final IArtifactType Action = new AtsArtifactTypes("AAMFDhY_rns71KvX14QA", "Action");
   public static final IArtifactType ActionableItem = new AtsArtifactTypes("AAMFDhW2LmhtRFsVyzwA", "Actionable Item");
   public static final IArtifactType DecisionReview = new AtsArtifactTypes("AAMFDhfrdR7BGTL7H_wA", "Decision Review");
   public static final IArtifactType PeerToPeerReview = new AtsArtifactTypes("AAMFDhh_300dpgmNtRAA", "PeerToPeer Review");
   public static final IArtifactType Task = new AtsArtifactTypes("AAMFDhbTAAB6h+06fuAA", "Task");
   public static final IArtifactType StateMachineArtifact = new AtsArtifactTypes("ABMfXC+LFBn31ZZbvjAA", "Abstract State Machine Artifact");
   public static final IArtifactType ReviewArtifact = new AtsArtifactTypes("ABMa6P4TwzXA1b8K3RAA", "Abstract Review Artifact");
   public static final IArtifactType TeamDefinition = new AtsArtifactTypes("AAMFDhUrlytusKbaQGAA", "Team Definition");
   public static final IArtifactType TeamWorkflow = new AtsArtifactTypes("AAMFDhSiF2OD+wiUqugA", "Team Workflow");
   public static final IArtifactType Version = new AtsArtifactTypes("AAMFDhder0oETnv14xQA", "Version");
   public static final IArtifactType Goal = new AtsArtifactTypes("ABMgU119UjI_Q23Yu+gA", "Goal");
   public static final IArtifactType AtsArtifact = new AtsArtifactTypes("ABMaLS0jvw92SE+4ZJQA", "ats.Ats Artifact");
   public static final IArtifactType WorkDefinition = new AtsArtifactTypes("AGrU8fWa3AJ6uoWYP7wA", "Work Definition");
   // @formatter:on

   private AtsArtifactTypes(String guid, String name) {
      super(guid, name);
   }
}