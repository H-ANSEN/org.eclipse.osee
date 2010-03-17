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

package org.eclipse.osee.framework.ui.skynet.widgets.dialog;

import java.util.Calendar;
import java.util.Date;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.calendarcombo.CalendarCombo;
import org.eclipse.nebula.widgets.calendarcombo.CalendarListenerAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class DateSelectionDialog extends MessageDialog {

   private Date initialDate, selectedDate;

   private final String dialogMessage;

   public DateSelectionDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, Date selectedDate) {
      super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
            defaultIndex);
      this.initialDate = selectedDate;
      this.dialogMessage = dialogMessage;
   }

   public DateSelectionDialog(String dialogTitle, String dialogMessage, Date selectedDate) {
      this(Display.getCurrent().getActiveShell(), dialogTitle, null, dialogMessage, MessageDialog.NONE, new String[] {
            "Ok", "Cancel"}, 0, selectedDate);
   }

   @Override
   protected Control createDialogArea(Composite container) {

      Composite filterComp = new Composite(container, SWT.NONE);

      filterComp.setLayout(new GridLayout(1, false));
      filterComp.setLayoutData(new GridData(GridData.FILL_BOTH));

      (new Label(filterComp, SWT.None)).setText(dialogMessage);

      final CalendarCombo dp = new CalendarCombo(filterComp, SWT.SINGLE | SWT.FLAT);
      if (initialDate != null) dp.setDate(initialDate);
      dp.addCalendarListener(new CalendarListenerAdapter() {
         public void dateChanged(Calendar date) {
            if (date != null) {
               selectedDate = dp.getDate().getTime();
            }
         }
      });

      // set selected date if != null
      return filterComp;
   }

   /**
    * @return the selectedDate
    */
   public Date getSelectedDate() {
      return selectedDate;
   }

   /**
    * @param selectedDate the selectedDate to set
    */
   public void setSelectedDate(Date initialDate) {
      this.initialDate = initialDate;
   }

   /**
    * @return the noneSelected
    */
   public boolean isNoneSelected() {
      return selectedDate == null;
   }

}
