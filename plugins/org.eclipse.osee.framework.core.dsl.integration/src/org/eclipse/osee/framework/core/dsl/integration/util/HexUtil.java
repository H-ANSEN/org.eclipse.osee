/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.dsl.integration.util;

import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public final class HexUtil {

   private HexUtil() {
      // Utility class
   }

   public static long toLong(String hexString) throws OseeCoreException {
      Long toReturn = -1L;
      try {
         String hex = hexString;
         if (hexString.startsWith("0x")) {
            hex = hexString.substring(2);
         }
         toReturn = Long.parseLong(hex, 16);
      } catch (Exception ex) {
         throw new OseeCoreException(String.format("Error converting [%s] to java.util.Long", hexString), ex);
      }
      return toReturn;

   }

   public static String toString(long value) throws OseeCoreException {
      //      String hexString = Long.toHexString(value).toUpperCase();
      return String.format("0x%016X", value);
   }

}
