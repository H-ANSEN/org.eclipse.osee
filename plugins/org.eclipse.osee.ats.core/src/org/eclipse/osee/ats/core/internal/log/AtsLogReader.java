/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.internal.log;

import java.util.logging.Level;
import java.util.regex.Matcher;
import org.eclipse.osee.ats.api.workflow.log.IAtsLog;
import org.eclipse.osee.ats.api.workflow.log.IAtsLogItem;
import org.eclipse.osee.ats.api.workflow.log.ILogStorageProvider;
import org.eclipse.osee.ats.core.AtsCore;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.AXml;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class AtsLogReader {

   private final ILogStorageProvider storeProvider;
   private final IAtsLog atsLog;

   public AtsLogReader(IAtsLog atsLog, ILogStorageProvider storeProvider) {
      this.atsLog = atsLog;
      this.storeProvider = storeProvider;
   }

   public void load() throws OseeCoreException {
      atsLog.clearLog();
      atsLog.setLogId(storeProvider.getLogId());
      String xml = storeProvider.getLogXml();
      if (!xml.isEmpty()) {
         Matcher m = AtsLogWriter.LOG_ITEM_PATTERN.matcher(xml);
         while (m.find()) {
            IAtsLogItem item =
               new LogItem(m.group(4), m.group(1), Strings.intern(m.group(5)), Strings.intern(m.group(3)),
                  AXml.xmlToText(m.group(2)));
            atsLog.addLogItem(item);
         }

         Matcher m2 = AtsLogWriter.LOG_ITEM_TAG_PATTERN.matcher(xml);
         int openTagsFound = 0;
         while (m2.find()) {
            openTagsFound++;
         }
         int size = atsLog.getLogItems().size();
         if (size != openTagsFound) {
            OseeLog.logf(AtsCore.class, Level.SEVERE,
               "ATS Log: open tags found %d doesn't match log items parsed %d for %s", openTagsFound, size,
               storeProvider.getLogId());
         }
      }
      atsLog.setDirty(false);
   }

}
