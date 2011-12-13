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

package org.eclipse.osee.framework.jdk.core.util.io.xml;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Ryan D. Brooks
 */
public abstract class AbstractSheetWriter implements ISheetWriter {
   private boolean startRow;
   private int defaultCellIndex;

   public AbstractSheetWriter() {
      startRow = true;
      defaultCellIndex = 0;
   }

   /**
    * must be called by subclasses in their implementations of writeCell(String data, int cellIndex)
    */
   protected void startRowIfNecessary() throws IOException {
      if (startRow) {
         startRow();
         startRow = false;
      }
   }

   @Override
   public void writeRow(Collection<Object> row) throws IOException {
      writeRow(row.toArray(new Object[row.size()]));
   }

   @Override
   public void writeRow(Object... row) throws IOException {
      for (int i = 0; i < row.length; i++) {
         writeCell(row[i]);
      }

      endRow();
   }

   @Override
   public void writeCell(Object data, int cellIndex) throws IOException {
      startRowIfNecessary();
      defaultCellIndex = cellIndex + 1;
      writeCellText(data, cellIndex);
   }

   @Override
   public void endRow() throws IOException {
      startRowIfNecessary();
      startRow = true;
      defaultCellIndex = 0;
      writeEndRow();
   }

   @Override
   public void writeCell(Object cellData) throws IOException {
      writeCell(cellData, defaultCellIndex);
   }

   protected abstract void startRow() throws IOException;

   protected abstract void writeEndRow() throws IOException;

   protected abstract void writeCellText(Object data, int cellIndex) throws IOException;
}