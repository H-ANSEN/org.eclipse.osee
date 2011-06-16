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
package org.eclipse.osee.ats.artifact;

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.column.ActionableItemsColumn;
import org.eclipse.osee.ats.core.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionManagerCore;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.workflow.ActionArtifact;
import org.eclipse.osee.ats.core.workflow.ActionArtifactRollup;
import org.eclipse.osee.ats.core.workflow.ActionableItemManagerCore;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.AICheckTreeDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.swt.Displays;

public class ActionableItemManager {

   public static Set<ActionableItemArtifact> getAIsFromItemAndChildren(ActionableItemArtifact aia) throws OseeCoreException {
      Set<ActionableItemArtifact> aias = new HashSet<ActionableItemArtifact>();
      aias.add(aia);
      for (Artifact art : aia.getChildren()) {
         if (art instanceof ActionableItemArtifact) {
            aias.addAll(getAIsFromItemAndChildren((ActionableItemArtifact) art));
         }
      }
      return aias;
   }

   public static Result editActionableItems(ActionArtifact actionArt) throws OseeCoreException {
      final AICheckTreeDialog diag =
         new AICheckTreeDialog(
            "Add Impacted Actionable Items",
            "Select New Impacted Actionable Items\n\n" + "Note: Un-selecting existing items will NOT remove the impact.\n" + "Team Workflow with no impact should be transitioned to Cancelled.",
            Active.Active);

      diag.setInitialAias(actionArt.getActionableItems());
      if (diag.open() != 0) {
         return Result.FalseResult;
      }

      // ensure that at least one actionable item exists for each team after aias added/removed
      for (TeamWorkFlowArtifact team : ActionManager.getTeams(actionArt)) {
         Set<ActionableItemArtifact> currentAias = team.getActionableItemsDam().getActionableItems();
         Collection<ActionableItemArtifact> checkedAias = diag.getChecked();
         for (ActionableItemArtifact aia : new CopyOnWriteArrayList<ActionableItemArtifact>(currentAias)) {
            if (!checkedAias.contains(aia)) {
               currentAias.remove(aia);
            }
         }
         if (currentAias.isEmpty()) {
            return new Result("Can not remove all actionable items for a team.\n\nActionable Items will go to 0 for [" +
            //
            team.getTeamName() + "][" + team.getHumanReadableId() + "]\n\nCancel team workflow instead.");
         }
      }

      final StringBuffer sb = new StringBuffer();
      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "Edit Actionable Items");
      Date createdDate = new Date();
      User createdBy = UserManager.getUser();

      // Add new aias
      for (ActionableItemArtifact aia : diag.getChecked()) {
         Result result = addActionableItemToTeamsOrAddTeams(actionArt, aia, createdDate, createdBy, transaction);
         sb.append(result.getText());
      }
      // Remove unchecked aias
      for (TeamWorkFlowArtifact team : ActionManager.getTeams(actionArt)) {
         for (ActionableItemArtifact aia : team.getActionableItemsDam().getActionableItems()) {
            if (!diag.getChecked().contains(aia)) {
               team.getActionableItemsDam().removeActionableItem(aia);
            }
         }
         team.persist(transaction);
      }

      transaction.execute();
      return new Result(true, sb.toString());
   }

   public static Result addActionableItemToTeamsOrAddTeams(Artifact actionArt, ActionableItemArtifact aia, Date createdDate, User createdBy, SkynetTransaction transaction) throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      for (TeamDefinitionArtifact tda : TeamDefinitionManagerCore.getImpactedTeamDefs(Arrays.asList(aia))) {
         boolean teamExists = false;
         // Look for team workflow that is associated with this tda
         for (TeamWorkFlowArtifact teamArt : ActionManager.getTeams(actionArt)) {
            // If found
            if (teamArt.getTeamDefinition().equals(tda)) {
               // And workflow doesn't already have this actionable item,
               // ADD it
               if (!teamArt.getActionableItemsDam().getActionableItems().contains(aia)) {
                  teamArt.getActionableItemsDam().addActionableItem(aia);
                  teamArt.saveSMA(transaction);
                  sb.append(aia.getName() + " => added to existing team workflow \"" + tda.getName() + "\"\n");
                  teamExists = true;
               } else {
                  sb.append(aia.getName() + " => already exists in team workflow \"" + tda.getName() + "\"\n");
                  teamExists = true;
               }
            }
         }
         if (!teamExists) {
            TeamWorkFlowArtifact teamArt =
               ActionManager.createTeamWorkflow(actionArt, tda, Arrays.asList(aia), tda.getLeads(), transaction,
                  createdDate, createdBy, null);
            teamArt.persist(transaction);
            sb.append(aia.getName() + " => added team workflow \"" + tda.getName() + "\"\n");
         }
      }
      return new Result(true, sb.toString());
   }

   public static Result editActionableItems(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      return ActionableItemManager.editActionableItems(teamArt.getParentActionArtifact());
   }

   public static Result convertActionableItems(TeamWorkFlowArtifact teamArt) throws OseeCoreException {
      Result toReturn = Result.FalseResult;
      AICheckTreeDialog diag =
         new AICheckTreeDialog(
            "Convert Impacted Actionable Items",
            "NOTE: This should NOT be the normal path to changing actionable items.\n\nIf a team has " +
            //
            "determined " + "that there is NO impact and that another actionable items IS impacted:\n" +
            //
            "   1) Cancel this operation\n" + "   2) Select \"Edit Actionable Items\" to add/remove " +
            //
            "impacted items \n" + "      which will create new teams as needed.\n" +
            //
            "   3) Then cancel the team that has no impacts.\n   Doing this will show that the original " +
            //
            "team analyzed the impact\n" + "   and determined that there was no change.\n\n" + "However, " +
            //
            "there are some cases where an impacted item was incorrectly chosen\n" + "and the original team " +
            //
            "does not need to do anything, this dialog will purge the\n" + "team from the DB as if it was " +
            //
            "never chosen.\n\n" + "Current Actionable Item(s): " + ActionableItemsColumn.getActionableItemsStr(teamArt) + "\n" +
            //
            "Current Team: " + teamArt.getTeamDefinition().getName() + "\n" +
            //
            "Select SINGLE Actionable Item below to convert this workflow to.\n\n" +
            //
            "You will be prompted to confirm this conversion.", Active.Both);

      diag.setInput(ActionableItemManagerCore.getTopLevelActionableItems(Active.Both));
      if (diag.open() != 0) {
         return Result.FalseResult;
      }
      if (diag.getChecked().isEmpty()) {
         return new Result("At least one actionable item must must be selected.");
      }
      if (diag.getChecked().size() > 1) {
         return new Result("Only ONE actionable item can be selected for converts");
      }
      ActionableItemArtifact selectedAia = diag.getChecked().iterator().next();
      Collection<TeamDefinitionArtifact> teamDefs =
         ActionableItemManagerCore.getImpactedTeamDefs(Arrays.asList(selectedAia));
      if (teamDefs.size() == 1) {
         TeamDefinitionArtifact newTeamDef = teamDefs.iterator().next();
         if (newTeamDef.equals(teamArt.getTeamDefinition())) {
            toReturn =
               new Result(
                  "Actionable Item selected belongs to same team as currently selected team.\n" + "Use \"Edit Actionable Items\" instaed.");
         } else {
            StringBuffer sb = new StringBuffer("Converting...\nActionable Item(s): ");
            sb.append(ActionableItemsColumn.getActionableItemsStr(teamArt));
            sb.append("\nTeam: ");
            sb.append(teamArt.getTeamDefinition().getName());
            sb.append("\nto\nActionable Item(s): ");
            sb.append(selectedAia);
            sb.append("\nTeam: ");
            sb.append(newTeamDef.getName());
            if (MessageDialog.openConfirm(Displays.getActiveShell(), "Confirm Convert", sb.toString())) {
               Set<ActionableItemArtifact> toProcess = new HashSet<ActionableItemArtifact>();
               toProcess.add(selectedAia);
               toReturn = actionableItemsTx(teamArt, AtsUtil.getAtsBranch(), toProcess, newTeamDef);
            }
         }
      } else {
         toReturn = new Result("Single team can not retrieved for " + selectedAia.getName());
      }
      return toReturn;
   }

   private static Result actionableItemsTx(TeamWorkFlowArtifact teamArt, Branch branch, Set<ActionableItemArtifact> selectedAlias, TeamDefinitionArtifact teamDefinition) throws OseeCoreException {
      Result workResult = teamArt.getActionableItemsDam().setActionableItems(selectedAlias);
      if (workResult.isTrue()) {
         if (teamDefinition != null) {
            teamArt.setTeamDefinition(teamDefinition);
         }
         SkynetTransaction transaction = new SkynetTransaction(branch, "Converate Actionable Item");
         ActionArtifactRollup rollup = new ActionArtifactRollup(teamArt.getParentActionArtifact(), transaction);
         rollup.resetAttributesOffChildren();
         teamArt.persist(transaction);
         transaction.execute();
      }
      return workResult;
   }

   public static Set<ActionableItemArtifact> getActionableItemsNameStartsWith(String prefix) throws OseeCoreException {
      Set<ActionableItemArtifact> artifacts = new HashSet<ActionableItemArtifact>();
      for (Artifact art : ArtifactQuery.getArtifactListFromName(prefix + "%", AtsUtil.getAtsBranch(), EXCLUDE_DELETED)) {
         if (art instanceof ActionableItemArtifact) {
            artifacts.add((ActionableItemArtifact) art);
         }
      }
      return artifacts;
   }

}
