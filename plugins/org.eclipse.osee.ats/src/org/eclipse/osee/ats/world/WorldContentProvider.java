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

package org.eclipse.osee.ats.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.ats.artifact.AbstractReviewArtifact;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.GoalArtifact;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.ActionManager;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.ats.util.AtsRelationTypes;
import org.eclipse.osee.ats.util.widgets.ReviewManager;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.swt.Displays;

public class WorldContentProvider implements ITreeContentProvider {

   // Store off relatedArts as they are discovered so they're not garbage collected
   protected Set<Artifact> relatedArts = new HashSet<Artifact>();
   private final WorldXViewer xViewer;

   public WorldContentProvider(WorldXViewer WorldXViewer) {
      super();
      this.xViewer = WorldXViewer;
   }

   @Override
   public String toString() {
      return "WorldContentProvider";
   }

   public void clear(boolean forcePend) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            xViewer.setInput(Collections.emptyList());
            xViewer.refresh();
         };
      }, forcePend);
   }

   @Override
   public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof Collection<?>) {
         return ((Collection<?>) parentElement).toArray();
      }
      if (parentElement instanceof Artifact) {
         try {
            Artifact artifact = (Artifact) parentElement;
            if (artifact.isDeleted()) {
               return new Object[] {};
            }
            if (artifact.isOfType(AtsArtifactTypes.Action)) {
               relatedArts.addAll(ActionManager.getTeams(artifact));
               return ActionManager.getTeams((artifact)).toArray();
            }
            if (artifact instanceof GoalArtifact) {
               List<Artifact> arts =
                  artifact.getRelatedArtifacts(AtsRelationTypes.Goal_Member, DeletionFlag.EXCLUDE_DELETED);
               relatedArts.addAll(arts);
               return arts.toArray(new Artifact[artifact.getRelatedArtifactsCount(AtsRelationTypes.Goal_Member)]);
            }
            if (artifact instanceof TeamWorkFlowArtifact) {
               TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) artifact;
               List<Artifact> arts = new ArrayList<Artifact>();
               // Convert artifacts to WorldArtifactItems
               arts.addAll(ReviewManager.getReviews(teamArt));
               arts.addAll(teamArt.getTaskArtifactsSorted());
               relatedArts.addAll(arts);
               return arts.toArray();
            }
            if (artifact instanceof AbstractReviewArtifact) {
               AbstractReviewArtifact reviewArt = (AbstractReviewArtifact) artifact;
               List<Artifact> arts = new ArrayList<Artifact>();
               arts.addAll(reviewArt.getTaskArtifactsSorted());
               relatedArts.addAll(arts);
               return arts.toArray();
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         }
      }
      return org.eclipse.osee.framework.jdk.core.util.Collections.EMPTY_ARRAY;
   }

   @Override
   public Object getParent(Object element) {
      if (element instanceof Artifact) {
         try {
            Artifact artifact = (Artifact) element;
            if (artifact.isDeleted()) {
               return null;
            }
            if (artifact instanceof TeamWorkFlowArtifact) {
               return ((TeamWorkFlowArtifact) artifact).getParentActionArtifact();
            }
            if (artifact instanceof TaskArtifact) {
               return ((TaskArtifact) artifact).getParentSMA();
            }
            if (artifact instanceof AbstractReviewArtifact) {
               return ((AbstractReviewArtifact) artifact).getParentSMA();
            }
            if (artifact instanceof GoalArtifact) {
               return ((GoalArtifact) artifact).getParentSMA();
            }
         } catch (Exception ex) {
            // do nothing
         }
      }
      return null;
   }

   @Override
   public boolean hasChildren(Object element) {
      if (element instanceof Collection<?>) {
         return true;
      }
      if (element instanceof String) {
         return false;
      }
      if (((Artifact) element).isDeleted()) {
         return false;
      }
      if (ActionManager.isOfTypeAction(element)) {
         return true;
      }
      if (element instanceof AbstractWorkflowArtifact) {
         return hasAtsWorldChildren((AbstractWorkflowArtifact) element);
      }
      return true;
   }

   private boolean hasAtsWorldChildren(AbstractWorkflowArtifact workflow) {
      if (workflow instanceof TaskArtifact) {
         return false;
      }
      for (IRelationTypeSide iRelationEnumeration : workflow.getAtsWorldRelations()) {
         if (workflow.getRelatedArtifactsCount(iRelationEnumeration) > 0) {
            return true;
         }
      }
      return false;
   }

   @Override
   public Object[] getElements(Object inputElement) {
      if (inputElement instanceof String) {
         return new Object[] {inputElement};
      }
      return getChildren(inputElement);
   }

   @Override
   public void dispose() {
      // do nothing
   }

   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // do nothing
   }

}
