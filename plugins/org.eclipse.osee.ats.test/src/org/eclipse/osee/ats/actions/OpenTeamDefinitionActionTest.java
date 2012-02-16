/*
 * Created on Oct 23, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.actions;

import org.eclipse.osee.ats.core.client.AtsTestUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public class OpenTeamDefinitionActionTest extends AbstractAtsActionRunTest {

   @Override
   public OpenTeamDefinitionAction createAction() throws OseeCoreException {
      return new OpenTeamDefinitionAction(AtsTestUtil.getTeamWf());
   }

}
