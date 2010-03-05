/*
 * Created on Sep 23, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.coverage.vcast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;

/**
 * @author Donald G. Dunne
 */
public class VCastAggregateReport {

   private final String vcastDirectory;
   Pattern coverageUnitPattern = Pattern.compile("Code&nbsp;Coverage&nbsp;for&nbsp;Unit:&nbsp;(.*?)<");
   Pattern resultsPattern = Pattern.compile("&nbsp;([0-9]+)&nbsp;of&nbsp;([0-9]+)&nbsp;Lines&nbsp;Covered&nbsp;");

   public VCastAggregateReport(String vcastDirectory) throws OseeCoreException {
      this.vcastDirectory = vcastDirectory;
   }

   public List<AggregateCoverageUnitResult> getResults() throws OseeCoreException {
      File reportHtmlFile = getFile();
      if (!reportHtmlFile.exists()) {
         throw new OseeArgumentException(String.format(
               "VectorCast vcast_aggregate_coverage_report.html file doesn't exist [%s]", vcastDirectory));
      }
      List<AggregateCoverageUnitResult> results = new ArrayList<AggregateCoverageUnitResult>();
      try {
         Reader inStream = new InputStreamReader(new FileInputStream(reportHtmlFile), "UTF-8");
         BufferedReader bufferedReader = new BufferedReader(inStream);
         String line;

         AggregateCoverageUnitResult result = null;
         while ((line = bufferedReader.readLine()) != null) {
            Matcher m = coverageUnitPattern.matcher(line);
            if (m.find()) {
               if (result != null) throw new OseeStateException("Last result not closed");
               result = new AggregateCoverageUnitResult(m.group(1));
               results.add(result);
               //               System.out.println("Found name " + m.group(1));
            }
            m = resultsPattern.matcher(line);
            if (m.find()) {
               if (result == null) throw new OseeStateException("Result end before result begin");
               result.setNumCovered(new Integer(m.group(1)));
               result.setNumLines(new Integer(m.group(2)));
               //               System.out.println("Found covered " + result.getNumCovered() + " of " + result.getNumLines());
               result = null;
            }
         }
         bufferedReader.close();
      } catch (Exception ex) {
         throw new OseeWrappedException("Error parsing aggregate report", ex);
      }

      return results;
   }

   public File getFile() {
      return new File(vcastDirectory + "/vcast/vcast_aggregate_coverage_report.html");
   }

}
