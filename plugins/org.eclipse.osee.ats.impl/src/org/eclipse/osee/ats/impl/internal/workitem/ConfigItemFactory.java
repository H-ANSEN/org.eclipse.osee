/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.impl.internal.workitem;

import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.agile.IAgileFeatureGroup;
import org.eclipse.osee.ats.api.agile.IAgileTeam;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.ats.api.team.IAtsConfigItemFactory;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.impl.IAtsServer;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Donald G. Dunne
 */
public class ConfigItemFactory implements IAtsConfigItemFactory {

   private final Log logger;
   private final IAtsServer atsServer;

   public ConfigItemFactory(Log logger, IAtsServer atsServer) {
      this.logger = logger;
      this.atsServer = atsServer;
   }

   @Override
   public IAtsConfigObject getConfigObject(Object artifact) throws OseeCoreException {
      IAtsConfigObject configObject = null;
      try {
         if (artifact instanceof ArtifactReadable) {
            ArtifactReadable artRead = (ArtifactReadable) artifact;
            if (artRead.isOfType(AtsArtifactTypes.Version)) {
               configObject = getVersion(artifact);
            } else if (artRead.isOfType(AtsArtifactTypes.TeamDefinition)) {
               configObject = getTeamDef(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.ActionableItem)) {
               configObject = getActionableItem(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.Program)) {
               configObject = getProgram(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.AgileTeam)) {
               configObject = getAgileTeam(artRead);
            } else if (artRead.isOfType(AtsArtifactTypes.AgileFeatureGroup)) {
               configObject = getAgileFeatureGroup(artRead);
            }
         }
      } catch (OseeCoreException ex) {
         logger.error(ex, "Error getting config object for [%s]", artifact);
      }
      return configObject;
   }

   @Override
   public IAtsVersion getVersion(Object artifact) {
      IAtsVersion version = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.Version)) {
            version = new Version(logger, atsServer, artRead);
         }
      }
      return version;
   }

   @Override
   public IAtsTeamDefinition getTeamDef(Object artifact) throws OseeCoreException {
      IAtsTeamDefinition teamDef = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.TeamDefinition)) {
            teamDef = new TeamDefinition(logger, atsServer, artRead);
         }
      }
      return teamDef;
   }

   @Override
   public IAtsActionableItem getActionableItem(Object artifact) throws OseeCoreException {
      IAtsActionableItem ai = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.ActionableItem)) {
            ai = new ActionableItem(logger, atsServer, artRead);
         }
      }
      return ai;
   }

   @Override
   public IAtsProgram getProgram(Object artifact) {
      IAtsProgram program = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.Program)) {
            program = new Program(logger, atsServer, artRead);
         }
      }
      return program;
   }

   @Override
   public IAgileTeam getAgileTeam(Object artifact) {
      IAgileTeam agileTeam = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.AgileTeam)) {
            agileTeam = atsServer.getAgileService().getAgileTeam(artRead);
         }
      }
      return agileTeam;
   }

   @Override
   public IAgileFeatureGroup getAgileFeatureGroup(Object artifact) {
      IAgileFeatureGroup agileTeam = null;
      if (artifact instanceof ArtifactReadable) {
         ArtifactReadable artRead = (ArtifactReadable) artifact;
         if (artRead.isOfType(AtsArtifactTypes.AgileFeatureGroup)) {
            agileTeam = atsServer.getAgileService().getAgileFeatureGroup(artRead);
         }
      }
      return agileTeam;
   }

}
