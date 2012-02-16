/*
 * Created on Oct 24, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.osee.ats.core.client.AtsTestUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public class OpenWorkflowByIdActionTest extends AbstractAtsActionRunTest {

   @Override
   public Action createAction() throws OseeCoreException {
      OpenWorkflowByIdAction action = new OpenWorkflowByIdAction();
      action.setOverrideId(AtsTestUtil.getTeamWf().getHumanReadableId());
      return action;
   }

}
