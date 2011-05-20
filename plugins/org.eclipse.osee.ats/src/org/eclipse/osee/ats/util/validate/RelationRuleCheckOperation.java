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
package org.eclipse.osee.ats.util.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

/**
 * @author Donald G. Dunne
 */
final class RelationRuleCheckOperation extends AbstractOperation {
   private final Collection<Artifact> itemsToCheck;
   private final XResultData rd;
   private final Set<RelationSetRule> relationSetRules;

   public RelationRuleCheckOperation(Collection<Artifact> itemsToCheck, XResultData rd, Set<RelationSetRule> relationSetRules) {
      super("Relation Check", SkynetGuiPlugin.PLUGIN_ID);
      this.itemsToCheck = itemsToCheck;
      this.rd = rd;
      this.relationSetRules = relationSetRules;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      rd.log("\n" + getName());
      Collection<String> warnings = new ArrayList<String>();
      double total = itemsToCheck.size() + relationSetRules.size();
      if (total > 0) {
         int workAmount = calculateWork(1 / total);
         for (Artifact art : itemsToCheck) {
            // Validate relations set
            for (RelationSetRule relationSetRule : relationSetRules) {
               checkForCancelledStatus(monitor);
               if (relationSetRule.hasArtifactType(art.getArtifactType())) {
                  // validate that artifact has one "Requirement Trace" relation to a Subsystem Requirement
                  Collection<Artifact> arts = art.getRelatedArtifacts(relationSetRule.getRelationEnum());
                  if (arts.size() < relationSetRule.getMinimumRelations()) {
                     if (art.isOfType(CoreArtifactTypes.DirectSoftwareRequirement)) {
                        rd.logError(ValidateReqChangeReport.getRequirementHyperlink(art) + " (" + art.getGammaId() + ") has less than minimum " + relationSetRule.getMinimumRelations() + " relation for type \"" + relationSetRule.getRelationEnum() + "\"");
                     } else {
                        warnings.add(ValidateReqChangeReport.getRequirementHyperlink(art) + " (" + art.getGammaId() + ") has less than minimum " + relationSetRule.getMinimumRelations() + " relation for type \"" + relationSetRule.getRelationEnum() + "\"");
                     }
                  }
               }
               monitor.worked(workAmount);
            }
         }
         // flag warnings
         for (String warning : warnings) {
            rd.logWarning(warning);
         }
      }
   }
}