/*
 * Created on Mar 24, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.artifact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.artifact.log.LogItem;
import org.eclipse.osee.ats.column.CancelledDateColumn;
import org.eclipse.osee.ats.column.ChangeTypeColumn;
import org.eclipse.osee.ats.column.CompletedDateColumn;
import org.eclipse.osee.ats.column.CreatedDateColumn;
import org.eclipse.osee.ats.column.HoursSpentTotalColumn;
import org.eclipse.osee.ats.column.PriorityColumn;
import org.eclipse.osee.ats.column.StateColumn;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.StateManager;
import org.eclipse.osee.ats.workdef.RuleDefinitionOption;
import org.eclipse.osee.ats.workdef.StateDefinition;
import org.eclipse.osee.ats.workdef.WidgetDefinition;
import org.eclipse.osee.ats.workdef.WidgetOption;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.validation.OseeXWidgetValidateManager;
import org.eclipse.osee.framework.ui.skynet.util.ChangeType;

public class WorkflowManager {

   public static enum ValidType {
      RequiredForCompletion,
      RequiredForTransition;
   }
   public static class ValidResult {
      public ValidType type;
      public WidgetDefinition widgetDef;

      public ValidResult(ValidType type, WidgetDefinition widgetDef) {
         super();
         this.type = type;
         this.widgetDef = widgetDef;
      }
   }

   /**
    * Return collection of problems with state widgets and artifact model storage
    */
   public static Collection<ValidResult> isStateValid(AbstractWorkflowArtifact awa, StateDefinition stateDef) throws OseeCoreException {
      List<ValidResult> results = new ArrayList<WorkflowManager.ValidResult>();
      for (WidgetDefinition widgetDef : stateDef.getWidgets()) {
         ValidResult result = isWidgetValid(awa, widgetDef);
         if (result != null) {
            results.add(result);
         }
      }
      return results;
   }

   /**
    * Return result of validity between widget and artifact model storage
    */
   public static ValidResult isWidgetValid(AbstractWorkflowArtifact awa, WidgetDefinition widgetDef) throws OseeCoreException {
      // validate first with providers of validation
      if (Strings.isValid(widgetDef.getXWidgetName())) {
         OseeXWidgetValidateManager.instance.validate(awa, widgetDef.getXWidgetName(), widgetDef.getName());
      }

      // else fallback on attribute validation if this is an artifact stored widget
      if (Strings.isValid(widgetDef.getAtrributeName())) {
         if (widgetDef.getOptions().contains(WidgetOption.REQUIRED_FOR_COMPLETION)) {
            if (awa.getAttributesToStringList(AttributeTypeManager.getType(widgetDef.getAtrributeName())).isEmpty()) {
               return new ValidResult(ValidType.RequiredForCompletion, widgetDef);
            }
         } else if (widgetDef.getOptions().contains(WidgetOption.REQUIRED_FOR_TRANSITION)) {
            if (awa.getAttributesToStringList(AttributeTypeManager.getType(widgetDef.getAtrributeName())).isEmpty()) {
               return new ValidResult(ValidType.RequiredForTransition, widgetDef);
            }
         }
      }
      return null;
   }

   public static boolean isEditable(AbstractWorkflowArtifact sma, StateDefinition stateDef, boolean priviledgedEditEnabled) throws OseeCoreException {
      // must be writeable
      return !sma.isReadOnly() &&
      // and access control writeable
      sma.isAccessControlWrite() &&
      // and current state
      (stateDef == null || sma.isInState(stateDef)) &&
      // and one of these
      //
      // page is define to allow anyone to edit
      (sma.getStateDefinition().hasRule(RuleDefinitionOption.AllowEditToAll) ||
      // team definition has allowed anyone to edit
      sma.teamDefHasRule(RuleDefinitionOption.AllowEditToAll) ||
      // priviledged edit mode is on
      priviledgedEditEnabled ||
      // current user is assigned
      sma.isAssigneeMe() ||
      // current user is ats admin
      AtsUtil.isAtsAdmin());
   }

   public static boolean isAssigneeEditable(AbstractWorkflowArtifact awa, boolean priviledgedEditEnabled) throws OseeCoreException {
      return !awa.isCompletedOrCancelled() && !awa.isReadOnly() &&
      // and access control writeable
      awa.isAccessControlWrite() && //

      (WorkflowManager.isEditable(awa, awa.getStateDefinition(), priviledgedEditEnabled) || //
      // page is define to allow anyone to edit
      awa.getStateDefinition().hasRule(RuleDefinitionOption.AllowAssigneeToAll) ||
      // team definition has allowed anyone to edit
      awa.teamDefHasRule(RuleDefinitionOption.AllowAssigneeToAll));
   }

   public static List<TeamWorkFlowArtifact> getAllTeamWorkflowArtifacts() throws OseeCoreException {
      List<TeamWorkFlowArtifact> result = new ArrayList<TeamWorkFlowArtifact>();
      for (IArtifactType artType : TeamWorkflowProviders.getAllTeamWorkflowArtifactTypes()) {
         List<TeamWorkFlowArtifact> teamArts =
            org.eclipse.osee.framework.jdk.core.util.Collections.castAll(ArtifactQuery.getArtifactListFromType(artType,
               AtsUtil.getAtsBranch()));
         result.addAll(teamArts);
      }
      return result;
   }

   public static Collection<AbstractWorkflowArtifact> getCompletedCancelled(Collection<AbstractWorkflowArtifact> awas) throws OseeCoreException {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         if (awa.isCompletedOrCancelled()) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> getInWork(Collection<AbstractWorkflowArtifact> awas) throws OseeCoreException {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         if (!awa.isCompletedOrCancelled()) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> filterOutState(Collection<AbstractWorkflowArtifact> awas, Collection<String> stateNames) {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         if (!stateNames.contains(awa.getStateMgr().getCurrentStateName())) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> filterOutCompleted(Collection<AbstractWorkflowArtifact> awas) throws OseeCoreException {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         if (!awa.isCompleted()) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> filterOutCancelled(Collection<AbstractWorkflowArtifact> awas) throws OseeCoreException {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         if (!awa.isCancelled()) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<Artifact> filterState(String selectedState, Collection<? extends Artifact> awas) {
      List<Artifact> artifactsToReturn = new ArrayList<Artifact>(awas.size());
      if (!Strings.isValid(selectedState)) {
         artifactsToReturn.addAll(awas);
      } else {
         for (Artifact awa : awas) {
            if (StateColumn.getInstance().getColumnText(awa, null, 0).equals(selectedState)) {
               artifactsToReturn.add(awa);
            }
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> filterOutTypes(Collection<AbstractWorkflowArtifact> awas, Collection<Class<?>> classes) {
      List<AbstractWorkflowArtifact> artifactsToReturn = new ArrayList<AbstractWorkflowArtifact>(awas.size());
      for (AbstractWorkflowArtifact awa : awas) {
         boolean found = false;
         for (Class<?> clazz : classes) {
            if (clazz.isInstance(awa)) {
               found = true;
            }
         }
         if (!found) {
            artifactsToReturn.add(awa);
         }
      }
      return artifactsToReturn;
   }

   public static Collection<AbstractWorkflowArtifact> getOpenAtDate(Date date, Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> awas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : artifacts) {
         Date createDate = CreatedDateColumn.getDate(awa);
         Date completedCancelDate = null;
         if (awa.isCompletedOrCancelled()) {
            if (awa.isCancelled()) {
               completedCancelDate = CancelledDateColumn.getDate(awa);
            } else {
               completedCancelDate = CompletedDateColumn.getDate(awa);
            }
         }
         if (createDate.before(date) && (completedCancelDate == null || completedCancelDate.after(date))) {
            awas.add(awa);
         }
      }
      return awas;
   }

   public static Collection<AbstractWorkflowArtifact> getCompletedCancelledBetweenDate(Date startDate, Date endDate, Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> awas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : artifacts) {
         Date completedCancelDate = null;
         if (awa.isCompletedOrCancelled()) {
            if (awa.isCancelled()) {
               completedCancelDate = CancelledDateColumn.getDate(awa);
            } else {
               completedCancelDate = CompletedDateColumn.getDate(awa);
            }
         }
         if (completedCancelDate == null) {
            continue;
         }
         if (completedCancelDate.after(startDate) && completedCancelDate.before(endDate)) {
            awas.add(awa);
         }
      }
      return awas;
   }

   public static Double getHoursSpent(Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      Double hoursSpent = 0.0;
      for (AbstractWorkflowArtifact awa : artifacts) {
         hoursSpent += HoursSpentTotalColumn.getHoursSpentTotal(awa);
      }
      return hoursSpent;
   }

   public static Collection<AbstractWorkflowArtifact> getStateAtDate(Date date, Collection<String> states, Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> awas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : artifacts) {
         Date createDate = CreatedDateColumn.getDate(awa);
         if (createDate.after(date)) {
            continue;
         }
         // Find state at date
         String currentState = awa.getStateMgr().getCurrentStateName();
         for (LogItem item : awa.getLog().getLogItems()) {
            if (item.getDate().before(date)) {
               currentState = item.getState();
            }
         }
         if (states.contains(currentState)) {
            awas.add(awa);
         }
      }
      return awas;
   }

   /**
    * Returns awa if change type, or parent team workflow's change type is in specified set
    */
   public static Collection<AbstractWorkflowArtifact> getChangeType(Collection<ChangeType> changeTypes, Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> awas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : artifacts) {
         TeamWorkFlowArtifact teamArt = awa.getParentTeamWorkflow();
         if (changeTypes.contains(ChangeTypeColumn.getChangeType(teamArt))) {
            awas.add(awa);
         }
      }
      return awas;

   }

   /**
    * Returns awa if priority type, or parent team workflow's priority type is in specified set
    */
   public static Collection<AbstractWorkflowArtifact> getPriorityType(Collection<String> priorityTypes, Collection<AbstractWorkflowArtifact> artifacts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> awas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : artifacts) {
         TeamWorkFlowArtifact teamArt = awa.getParentTeamWorkflow();
         if (priorityTypes.contains(PriorityColumn.getPriorityStr(teamArt))) {
            awas.add(awa);
         }
      }
      return awas;

   }

   public static Collection<AbstractWorkflowArtifact> getTeamDefinitionWorkflows(Collection<? extends Artifact> artifacts, Collection<TeamDefinitionArtifact> teamDefs) throws OseeCoreException {
      List<AbstractWorkflowArtifact> returnawas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : getAwas(artifacts)) {
         if (awa.getParentTeamWorkflow() == null) {
            continue;
         }
         if (teamDefs.contains(awa.getParentTeamWorkflow().getTeamDefinition())) {
            returnawas.add(awa);
         }
      }
      return returnawas;
   }

   public static Collection<AbstractWorkflowArtifact> getVersionWorkflows(Collection<? extends Artifact> artifacts, Collection<Artifact> versionArts) throws OseeCoreException {
      List<AbstractWorkflowArtifact> returnawas = new ArrayList<AbstractWorkflowArtifact>();
      for (AbstractWorkflowArtifact awa : getAwas(artifacts)) {
         if (awa.getParentTeamWorkflow() == null) {
            continue;
         }
         if (awa.getTargetedVersion() == null) {
            continue;
         }
         if (versionArts.contains(awa.getTargetedVersion())) {
            returnawas.add(awa);
         }
      }
      return returnawas;
   }

   public static Collection<AbstractWorkflowArtifact> getAwas(Collection<? extends Artifact> artifacts) {
      return Collections.castMatching(AbstractWorkflowArtifact.class, artifacts);
   }

   public static StateManager getStateManager(Artifact artifact) {
      return cast(artifact).getStateMgr();
   }

   public static AbstractWorkflowArtifact cast(Artifact artifact) {
      if (artifact instanceof AbstractWorkflowArtifact) {
         return (AbstractWorkflowArtifact) artifact;
      }
      return null;
   }

}
