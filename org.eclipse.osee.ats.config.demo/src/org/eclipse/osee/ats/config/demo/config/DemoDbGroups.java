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
package org.eclipse.osee.ats.config.demo.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.config.demo.artifact.DemoCodeTeamWorkflowArtifact;
import org.eclipse.osee.ats.config.demo.internal.OseeAtsConfigDemoActivator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.enums.CoreRelationEnumeration;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.UniversalGroup;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;

/**
 * @author Donald G. Dunne
 */
public class DemoDbGroups {
   public static String TEST_GROUP_NAME = "Test Group";

   public static List<TeamWorkFlowArtifact> createGroups() throws Exception {

      // Create group of all resulting objects
      List<TeamWorkFlowArtifact> codeWorkflows = new ArrayList<TeamWorkFlowArtifact>();
      OseeLog.log(OseeAtsConfigDemoActivator.class, Level.INFO,  "Create Groups and add objects");
      Artifact groupArt = UniversalGroup.addGroup(TEST_GROUP_NAME, AtsUtil.getAtsBranch());
      for (DemoCodeTeamWorkflowArtifact codeArt : DemoDbUtil.getSampleCodeWorkflows()) {

         // Add Action to Universal Group
         groupArt.addRelation(CoreRelationEnumeration.Universal_Grouping__Members, codeArt.getParentActionArtifact());

         // Add All Team Workflows to Universal Group
         for (Artifact teamWorkflow : codeArt.getParentActionArtifact().getTeamWorkFlowArtifacts()) {
            groupArt.addRelation(CoreRelationEnumeration.Universal_Grouping__Members, teamWorkflow);
         }

         codeArt.persist();
      }

      // Add all Tasks to Group
      for (Artifact task : ArtifactQuery.getArtifactListFromType(TaskArtifact.ARTIFACT_NAME, AtsUtil.getAtsBranch())) {
         groupArt.addRelation(CoreRelationEnumeration.Universal_Grouping__Members, task);
      }
      groupArt.persist();

      return codeWorkflows;
   }
}
