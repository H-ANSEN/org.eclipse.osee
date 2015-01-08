/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.disposition.rest.importer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.eclipse.osee.disposition.model.Discrepancy;
import org.eclipse.osee.disposition.model.DispoItem;
import org.eclipse.osee.disposition.rest.internal.DispoDataFactory;
import org.eclipse.osee.disposition.rest.internal.importer.TmzImporter;
import org.eclipse.osee.disposition.rest.internal.report.OperationReport;
import org.eclipse.osee.disposition.rest.util.DispoUtil;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author John Misinco
 */
public class TmzImporterTest {

   @Rule
   public TemporaryFolder folder = new TemporaryFolder();

   private final DispoDataFactory factory = new DispoDataFactory();

   @Test
   @SuppressWarnings("unchecked")
   public void testImportWithCheckGroups() throws IOException, JSONException {
      File tmzFile = folder.newFile("CheckGroup.tmz");
      Lib.inputStreamToFile(getClass().getResourceAsStream("CheckGroup.tmz"), tmzFile);
      TmzImporter importer = new TmzImporter(null, factory);
      OperationReport report = new OperationReport();
      List<DispoItem> results = importer.importDirectory(new HashMap<String, DispoItem>(), folder.getRoot(), report);
      Assert.assertEquals(1, results.size());
      DispoItem result = results.get(0);
      Assert.assertEquals("CheckGroup", result.getName());
      Assert.assertEquals(2, result.getDiscrepanciesList().length());
      Iterator<String> keys = result.getDiscrepanciesList().keys();
      Assert.assertEquals("113054", result.getVersion());
      boolean firstFound = false, thirdFound = false;
      while (keys.hasNext()) {
         JSONObject jsonObject = result.getDiscrepanciesList().getJSONObject(keys.next());
         Discrepancy discrepancy = DispoUtil.jsonObjToDiscrepancy(jsonObject);
         if (discrepancy.getLocation() == 1) {
            firstFound = true;
            Assert.assertEquals(
               "Failure at Test Point 1. Check Group with Checkpoint Failures: Check Point: CODE. Expected: 1500. Actual: NULL. Check Point: STATE. Expected: TRUE. Actual: NULL. Check Point: IBOT. Expected: FALSE. Actual: NULL. Check Point: BOT_CODE. Expected: 0. Actual: NULL. Check Point: FILTER_TIME. Expected: 10000. Actual: NULL. ",
               discrepancy.getText());
         } else if (discrepancy.getLocation() == 3) {
            thirdFound = true;
            Assert.assertEquals(
               "Failure at Test Point 3. Check Point: CheckPoint_BOT. Expected: ORANGE = FALSE. Actual: ORANGE = NULL. ",
               discrepancy.getText());
         }
      }
      Assert.assertTrue(firstFound);
      Assert.assertTrue(thirdFound);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testImportNoCheckGroups() throws IOException, JSONException {
      File tmzFile = folder.newFile("NoCheckGroup.tmz");
      Lib.inputStreamToFile(getClass().getResourceAsStream("NoCheckGroup.tmz"), tmzFile);
      TmzImporter importer = new TmzImporter(null, factory);
      OperationReport report = new OperationReport();
      List<DispoItem> results = importer.importDirectory(new HashMap<String, DispoItem>(), folder.getRoot(), report);
      Assert.assertEquals(1, results.size());
      DispoItem result = results.get(0);
      Assert.assertEquals("NoCheckGroup", result.getName());
      Assert.assertEquals(1, result.getDiscrepanciesList().length());
      Iterator<String> keys = result.getDiscrepanciesList().keys();
      Assert.assertEquals("113054", result.getVersion());
      boolean secondFound = false;
      while (keys.hasNext()) {
         JSONObject jsonObject = result.getDiscrepanciesList().getJSONObject(keys.next());
         Discrepancy discrepancy = DispoUtil.jsonObjToDiscrepancy(jsonObject);
         if (discrepancy.getLocation() == 2) {
            secondFound = true;
            Assert.assertEquals(
               "Failure at Test Point 2. Check Point: CheckPoint_BOT. Expected: ORANGE = FALSE. Actual: ORANGE = NULL. ",
               discrepancy.getText());
         }
      }
      Assert.assertTrue(secondFound);
   }

}
