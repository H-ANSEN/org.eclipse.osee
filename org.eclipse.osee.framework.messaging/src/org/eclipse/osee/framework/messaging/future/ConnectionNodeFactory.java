/*
 * Created on Jan 15, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.messaging.future;

import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author b1122182
 */
public interface ConnectionNodeFactory {

   ConnectionNode create(NodeInfo nodeInfo) throws OseeCoreException;
   
}
