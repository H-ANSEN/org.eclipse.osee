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
package org.eclipse.osee.ats.core.config;

import java.util.Collections;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.ats.core.internal.Activator;
import org.eclipse.osee.ats.core.type.AtsRelationTypes;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;

/**
 * @author Donald G. Dunne
 */
public class AtsLoadConfigArtifactsOperation extends AbstractOperation {
   private static boolean loaded = false;

   public AtsLoadConfigArtifactsOperation() {
      super("ATS Loading Configuration", Activator.PLUGIN_ID);
   }

   public void forceReload() throws OseeCoreException {
      loaded = false;
      ensureLoaded();
   }

   public synchronized void ensureLoaded() throws OseeCoreException {
      if (!loaded) {
         loaded = true;
         OseeLog.log(Activator.class, Level.INFO, "Loading ATS Configuration");
         Artifact headingArt = AtsUtilCore.getFromToken(AtsArtifactToken.HeadingFolder);
         // Loading artifacts will cache them in ArtifactCache
         RelationManager.getRelatedArtifacts(Collections.singleton(headingArt), 8,
            CoreRelationTypes.Default_Hierarchical__Child, AtsRelationTypes.TeamDefinitionToVersion_Version);
         // Load Work Definitions
         // TODO not doing anymore
         //         WorkItemDefinitionFactory.loadDefinitions();
         loaded = true;
      }
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      ensureLoaded();
   }

   public static boolean isLoaded() {
      return loaded;
   }

}
