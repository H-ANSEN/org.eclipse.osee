/*********************************************************************
 * Copyright (c) 2010 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.ats.ide.column;

import org.eclipse.osee.ats.api.column.AtsColumnTokens;
import org.eclipse.osee.ats.ide.util.xviewer.column.XViewerAtsColumnIdColumn;

/**
 * @author Jeremy A. Midvidy
 */
public class CancelledReasonDetailsColumnUI extends XViewerAtsColumnIdColumn {

   public static CancelledReasonDetailsColumnUI instance = new CancelledReasonDetailsColumnUI();

   public static CancelledReasonDetailsColumnUI getInstance() {
      return instance;
   }

   public CancelledReasonDetailsColumnUI() {
      super(AtsColumnTokens.CancelledReasonDetails);
   }

}
