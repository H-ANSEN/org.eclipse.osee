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
package org.eclipse.osee.coverage.vcast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * Represents a single RESULTS block found in the <dir>.wrk/vcast.vcp file.
 * 
 * @author Donald G. Dunne
 */
public class VcpResultsFile {

   private final Map<ResultsValue, String> resultsValues = new HashMap<ResultsValue, String>(20);
   Pattern valuePattern = Pattern.compile("(.*?):(.*?)$");
   private VcpResultsDatFile vcpResultsDatFile;
   private final String vcastDirectory;

   public static enum ResultsValue {
      FILENAME,
      DIRECTORY,
      DISPLAY_NAME,
      RESULT_TYPE,
      ADDITION_TIME,
      IS_SELECTED,
      HAD_COVERAGE_REMOVED
   };

   public VcpResultsFile(String vcastDirectory) {
      this.vcastDirectory = vcastDirectory;
   }

   public String getValue(ResultsValue resultsValue) {
      return resultsValues.get(resultsValue);
   }

   public void addLine(String line) {
      Matcher m = valuePattern.matcher(line);
      if (m.find()) {
         ResultsValue resultsValue = ResultsValue.valueOf(m.group(1));
         if (resultsValue == null) {
            OseeLog.log(Activator.class, Level.SEVERE, String.format("Unhandled VcpResultsFile value [%s]", m.group(1)));
         } else {
            resultsValues.put(resultsValue, m.group(2));
         }
      } else {
         OseeLog.log(Activator.class, Level.SEVERE, String.format("Unhandled VcpResultsFile line [%s]", line));
      }
   }

   public VcpResultsDatFile getVcpResultsDatFile() throws OseeCoreException, IOException {
      if (vcpResultsDatFile == null) {
         vcpResultsDatFile = new VcpResultsDatFile(vcastDirectory, this);
      }
      return vcpResultsDatFile;
   }

   @Override
   public String toString() {
      return getValue(ResultsValue.FILENAME);
   }
}
