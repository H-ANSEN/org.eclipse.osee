/*
 * Created on Apr 20, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.messaging;

/**
 * @author Andrew M. Finkbeiner
 *
 */
public interface ReceiveListener {
   void handle(Message cmd);
}
