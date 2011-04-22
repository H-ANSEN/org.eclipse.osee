/*
 * Created on Nov 10, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.column;

import java.util.Date;
import org.junit.Assert;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.column.AssigneeColumn;
import org.eclipse.osee.ats.column.CancelledDateColumn;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.DemoTestUtil;
import org.eclipse.osee.ats.util.TeamState;
import org.eclipse.osee.ats.util.TransitionOption;
import org.eclipse.osee.ats.workflow.TransitionManager;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.logging.SevereLoggingMonitor;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @tests CancelledDateColumn
 * @author Donald G. Dunne
 */
public class CancelledDateColumnTest {

   @AfterClass
   @BeforeClass
   public static void cleanup() throws Exception {
      DemoTestUtil.cleanupSimpleTest(CancelledDateColumnTest.class.getSimpleName());
   }

   @org.junit.Test
   public void testGetDateAndStrAndColumnText() throws Exception {
      SevereLoggingMonitor loggingMonitor = TestUtil.severeLoggingStart();

      SkynetTransaction transaction =
         new SkynetTransaction(AtsUtil.getAtsBranch(), CancelledDateColumnTest.class.getSimpleName());
      TeamWorkFlowArtifact teamArt =
         DemoTestUtil.createSimpleAction(CancelledDateColumnTest.class.getSimpleName(), transaction);
      transaction.execute();

      Assert.assertEquals("", CancelledDateColumn.getInstance().getColumnText(teamArt, AssigneeColumn.getInstance(), 0));
      Date date = CancelledDateColumn.getDate(teamArt);
      Assert.assertNull(date);
      Assert.assertEquals("", CancelledDateColumn.getDateStr(teamArt));

      TransitionManager transitionMgr = new TransitionManager(teamArt);
      transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), CancelledDateColumnTest.class.getSimpleName());
      transitionMgr.transitionToCancelled("reason", transaction, TransitionOption.OverrideTransitionValidityCheck);
      transaction.execute();

      date = CancelledDateColumn.getDate(teamArt);
      Assert.assertNotNull(date);
      Assert.assertEquals(DateUtil.getMMDDYYHHMM(date), CancelledDateColumn.getDateStr(teamArt));
      Assert.assertEquals(DateUtil.getMMDDYYHHMM(date),
         CancelledDateColumn.getInstance().getColumnText(teamArt, AssigneeColumn.getInstance(), 0));

      transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), CancelledDateColumnTest.class.getSimpleName());
      transitionMgr.transition(TeamState.Endorse, UserManager.getUser(), transaction,
         TransitionOption.OverrideTransitionValidityCheck);
      transaction.execute();

      Assert.assertEquals("Cancelled date should be blank again", "",
         CancelledDateColumn.getInstance().getColumnText(teamArt, AssigneeColumn.getInstance(), 0));
      date = CancelledDateColumn.getDate(teamArt);
      Assert.assertNull(date);

      TestUtil.severeLoggingEnd(loggingMonitor);
   }
}
