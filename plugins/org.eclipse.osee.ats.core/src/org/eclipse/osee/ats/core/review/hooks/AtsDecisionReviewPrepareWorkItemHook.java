/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.ats.core.review.hooks;

import java.util.Collection;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.review.DecisionReviewOptions;
import org.eclipse.osee.ats.api.review.DecisionReviewState;
import org.eclipse.osee.ats.api.review.IAtsDecisionReview;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.workdef.IStateToken;
import org.eclipse.osee.ats.api.workflow.hooks.IAtsTransitionHook;
import org.eclipse.osee.ats.api.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.core.internal.AtsApiService;

/**
 * Contributed via AtsWorkItemServiceImpl
 *
 * @author Donald G. Dunne
 */
public class AtsDecisionReviewPrepareWorkItemHook implements IAtsTransitionHook {

   public String getName() {
      return AtsDecisionReviewPrepareWorkItemHook.class.getSimpleName();
   }

   @Override
   public String getDescription() {
      return "Add validation of Decision Review options prior to transitioning from Prepare to Decision.";
   }

   @Override
   public void transitioning(TransitionResults results, IAtsWorkItem workItem, IStateToken fromState, IStateToken toState, Collection<? extends AtsUser> toAssignees, AtsUser asUser) {
      if (workItem instanceof IAtsDecisionReview && fromState.getName().equals(
         DecisionReviewState.Prepare.getName()) && toState.getName().equals(DecisionReviewState.Decision.getName())) {
         DecisionReviewOptions decOptions =
            new DecisionReviewOptions((IAtsDecisionReview) workItem, AtsApiService.get());
         decOptions.validateDecisionOptions(results);
      }
   }

}
