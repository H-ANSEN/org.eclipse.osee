/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.editor.log.column;

import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerValueColumn;
import org.eclipse.osee.ats.core.client.workflow.log.LogItem;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.swt.SWT;

public class LogDateColumn extends XViewerValueColumn {

   private static LogDateColumn instance = new LogDateColumn();

   public static LogDateColumn getInstance() {
      return instance;
   }

   public LogDateColumn() {
      super("ats.log.Date", "Date", 120, SWT.LEFT, true, SortDataType.Date, false, "");
   }

   /**
    * XViewer uses copies of column definitions so originals that are registered are not corrupted. Classes extending
    * XViewerValueColumn MUST extend this constructor so the correct sub-class is created
    */
   @Override
   public LogDateColumn copy() {
      LogDateColumn newXCol = new LogDateColumn();
      copy(this, newXCol);
      return newXCol;
   }

   @Override
   public String getColumnText(Object element, XViewerColumn column, int columnIndex) {
      if (element instanceof LogItem) {
         return DateUtil.getMMDDYYHHMM(((LogItem) element).getDate());
      }

      return "";
   }
}
