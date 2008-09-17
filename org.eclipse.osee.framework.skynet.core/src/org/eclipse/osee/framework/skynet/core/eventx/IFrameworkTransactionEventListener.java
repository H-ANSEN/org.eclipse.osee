/*
 * Created on Sep 14, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.eventx;

import org.eclipse.osee.framework.ui.plugin.event.Sender.Source;

/**
 * @author Donald G. Dunne
 */
public interface IFrameworkTransactionEventListener extends IXEventListener {

   public void handleFrameworkTransactionEvent(Source source, TransactionData transData);

}
