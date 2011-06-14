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

import static org.eclipse.osee.framework.core.enums.DeletionFlag.EXCLUDE_DELETED;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.core.team.TeamState;
import org.eclipse.osee.ats.core.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.workflow.transition.TransitionHelper;
import org.eclipse.osee.ats.core.workflow.transition.TransitionManager;
import org.eclipse.osee.ats.core.workflow.transition.TransitionOption;
import org.eclipse.osee.ats.core.workflow.transition.TransitionResults;
import org.eclipse.osee.ats.util.AtsNotifyUsers.NotifyType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.skynet.notify.OseeNotificationEvent;
import org.eclipse.osee.support.test.util.DemoUsers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class AtsNotifyUsersTest {

   @BeforeClass
   @AfterClass
   public static void cleanup() throws OseeCoreException {
      Collection<Artifact> artifacts =
         ArtifactQuery.getArtifactListFromName(AtsNotifyUsersTest.class.getSimpleName(), AtsUtil.getAtsBranch(),
            EXCLUDE_DELETED);
      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "AtsNotifyUsersTest Clean-up");
      for (Artifact artifact : artifacts) {
         artifact.deleteAndPersist(transaction);
      }
      transaction.execute();
   }

   @Test
   public void testNotify() throws OseeCoreException {
      User jason_ValidEmail = UserManager.getUser(DemoUsers.Jason_Michael);
      jason_ValidEmail.setEmail("this@boeing.com");
      User alex_NoValidEmail = UserManager.getUser(DemoUsers.Alex_Kay);
      User kay_ValidEmail = UserManager.getUser(DemoUsers.Kay_Jones);
      kay_ValidEmail.setEmail("this@boeing.com");
      User joeSmith_CurrentUser = UserManager.getUser(DemoUsers.Joe_Smith);
      joeSmith_CurrentUser.setEmail("this@boeing.com");
      User inactiveSteve = UserManager.getUser(DemoUsers.Inactive_Steve);
      inactiveSteve.setEmail("goodEmail@boeing.com");

      TestNotificationManager notifyManager = new TestNotificationManager();
      AtsNotifyUsers atsNotifyUsers = AtsNotifyUsers.getInstance();
      atsNotifyUsers.setNotificationManager(notifyManager);
      atsNotifyUsers.setInTest(true);

      SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), getClass().getSimpleName());
      TeamWorkFlowArtifact teamArt =
         DemoTestUtil.createSimpleAction(AtsNotifyUsersTest.class.getSimpleName(), transaction);
      teamArt.internalSetCreatedBy(kay_ValidEmail);
      List<IBasicUser> assignees = new ArrayList<IBasicUser>();
      assignees.addAll(Arrays.asList(inactiveSteve, alex_NoValidEmail, jason_ValidEmail, kay_ValidEmail,
         joeSmith_CurrentUser));
      teamArt.getStateMgr().setAssignees(assignees);
      teamArt.persist(transaction);
      transaction.execute();

      notifyManager.clear();
      AtsNotifyUsers.getInstance().notify(teamArt, NotifyType.Originator);
      Assert.assertEquals(1, notifyManager.getNotificationEvents().size());
      OseeNotificationEvent event = notifyManager.getNotificationEvents().get(0);
      Assert.assertEquals(NotifyType.Originator.name(), event.getType());
      Assert.assertEquals(kay_ValidEmail, event.getUsers().iterator().next());
      Assert.assertEquals(
         "You have been set as the originator of [Demo Code Team Workflow] state [Endorse] titled [AtsNotifyUsersTest]",
         event.getDescription());

      notifyManager.clear();
      teamArt.internalSetCreatedBy(inactiveSteve);
      teamArt.persist(getClass().getSimpleName());
      AtsNotifyUsers.getInstance().notify(teamArt, NotifyType.Originator);
      Assert.assertEquals(0, notifyManager.getNotificationEvents().size());
      teamArt.internalSetCreatedBy(kay_ValidEmail);
      teamArt.persist(getClass().getSimpleName());

      notifyManager.clear();
      AtsNotifyUsers.getInstance().notify(teamArt, NotifyType.Assigned);
      Assert.assertEquals(1, notifyManager.getNotificationEvents().size());
      event = notifyManager.getNotificationEvents().get(0);
      Assert.assertEquals(NotifyType.Assigned.name(), event.getType());
      // joe smith should be removed from list cause it's current user
      // alex should be removed cause not valid email
      List<IBasicUser> expected = new ArrayList<IBasicUser>();
      expected.add(jason_ValidEmail);
      expected.add(kay_ValidEmail);
      List<IBasicUser> users = new ArrayList<IBasicUser>();
      users.addAll(event.getUsers());
      Assert.assertTrue(org.eclipse.osee.framework.jdk.core.util.Collections.isEqual(expected, users));
      Assert.assertEquals(
         "You have been set as the assignee of [Demo Code Team Workflow] in state [Endorse] titled [AtsNotifyUsersTest]",
         event.getDescription());

      notifyManager.clear();
      AtsNotifyUsers.getInstance().notify(teamArt, Collections.singleton((IBasicUser) jason_ValidEmail),
         NotifyType.Assigned);
      Assert.assertEquals(1, notifyManager.getNotificationEvents().size());
      event = notifyManager.getNotificationEvents().get(0);
      Assert.assertEquals(NotifyType.Assigned.name(), event.getType());
      // only alex should be emailed cause sent in list
      Assert.assertEquals(Collections.singleton(jason_ValidEmail), event.getUsers());
      Assert.assertEquals(
         "You have been set as the assignee of [Demo Code Team Workflow] in state [Endorse] titled [AtsNotifyUsersTest]",
         event.getDescription());

      notifyManager.clear();
      new SubscribeManager(teamArt).toggleSubscribe(false);
      transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "AtsNotifyUsersTests.toggle.subscribed");
      SubscribeManager.addSubscribed(teamArt, inactiveSteve, transaction);
      transaction.execute();
      AtsNotifyUsers.getInstance().notify(teamArt, NotifyType.Subscribed);
      Assert.assertEquals(1, notifyManager.getNotificationEvents().size());
      event = notifyManager.getNotificationEvents().get(0);
      Assert.assertEquals(NotifyType.Subscribed.name(), event.getType());
      Assert.assertEquals(UserManager.getUser(), event.getUsers().iterator().next());
      Assert.assertEquals(
         "[Demo Code Team Workflow] titled [AtsNotifyUsersTest] transitioned to [Endorse] and you subscribed for notification.",
         event.getDescription());
      new SubscribeManager(teamArt).toggleSubscribe(false);

      notifyManager.clear();
      AtsNotifyUsers.getInstance().notify(teamArt, NotifyType.Completed);
      Assert.assertEquals(0, notifyManager.getNotificationEvents().size());

      notifyManager.clear();
      teamArt.getStateMgr().initializeStateMachine(TeamState.Completed);
      AtsNotifyUsers.getInstance().notify(teamArt, NotifyType.Completed);
      event = notifyManager.getNotificationEvents().get(0);
      Assert.assertEquals(NotifyType.Completed.name(), event.getType());
      Assert.assertEquals(kay_ValidEmail, event.getUsers().iterator().next());
      Assert.assertEquals("[Demo Code Team Workflow] titled [AtsNotifyUsersTest] is [Completed]",
         event.getDescription());

      notifyManager.clear();
      teamArt.internalSetCreatedBy(inactiveSteve);
      teamArt.persist(getClass().getSimpleName());
      teamArt.getStateMgr().initializeStateMachine(TeamState.Completed);
      AtsNotifyUsers.getInstance().notify(teamArt, NotifyType.Completed);
      Assert.assertEquals(0, notifyManager.getNotificationEvents().size());
      teamArt.internalSetCreatedBy(kay_ValidEmail);
      teamArt.persist(getClass().getSimpleName());

      notifyManager.clear();
      teamArt.getStateMgr().initializeStateMachine(TeamState.Endorse);
      TransitionHelper helper =
         new TransitionHelper(getClass().getSimpleName(), Arrays.asList(teamArt), TeamState.Cancelled.getPageName(),
            null, "this is the reason", TransitionOption.OverrideTransitionValidityCheck);
      transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), getClass().getSimpleName());
      TransitionManager transitionMgr = new TransitionManager(helper, transaction);
      TransitionResults results = transitionMgr.handleAll();
      transaction.execute();
      Assert.assertTrue("Transition should have no errors", results.isEmpty());

      Assert.assertEquals(1, notifyManager.getNotificationEvents().size());
      event = notifyManager.getNotificationEvents().get(0);
      Assert.assertEquals(NotifyType.Cancelled.name(), event.getType());
      Assert.assertEquals(kay_ValidEmail, event.getUsers().iterator().next());
      Assert.assertTrue(event.getDescription().startsWith(
         "[Demo Code Team Workflow] titled [AtsNotifyUsersTest] was [Cancelled] from the [Endorse] state on"));
      Assert.assertTrue(event.getDescription().endsWith(".<br>Reason: [this is the reason]"));

   }
}
