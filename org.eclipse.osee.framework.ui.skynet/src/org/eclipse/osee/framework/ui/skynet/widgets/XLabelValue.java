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
package org.eclipse.osee.framework.ui.skynet.widgets;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Donald G. Dunne
 */
public abstract class XLabelValue extends XWidget {

   Label valueLabel;

   public XLabelValue(String label) {
      super(label);
   }

   public String getText() {
      return "";
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {

      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(ALayout.getZeroMarginLayout(5, false));
      if (isFillHorizontally()) {
         comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      } else {
         comp.setLayoutData(new GridData());
      }

      // Create Text Widgets
      if (isDisplayLabel() && !getLabel().equals("")) {
         labelWidget = new Label(comp, SWT.NONE);
         labelWidget.setText(getLabel() + ":");
         if (getToolTip() != null) {
            labelWidget.setToolTipText(getToolTip());
         }
      }

      if (toolkit == null) {
         valueLabel = new Label(comp, SWT.NONE);
      } else {
         valueLabel = toolkit.createLabel(comp, "", SWT.NONE);
      }
      if (Strings.isValid(getToolTip())) {
         valueLabel.setToolTipText(getToolTip());
      }
      if (isFillHorizontally()) {
         if (isFillHorizontally()) {
            valueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         } else {
            valueLabel.setLayoutData(new GridData());
         }
      }
      refresh();
   }

   @Override
   public void refresh() {
      valueLabel.setText(getText());
      valueLabel.update();
      valueLabel.getParent().update();
      validate();
   }

   @Override
   public Control getControl() {
      return valueLabel;
   }

   @Override
   public void adaptControls(FormToolkit toolkit) {
      super.adaptControls(toolkit);
      toolkit.adapt(valueLabel, true, true);
      valueLabel.update();
   }

   @Override
   public String toHTML(String labelFont) {
      return AHTML.getLabelValueStr(AHTML.LABEL_FONT, getLabel(), getText());
   }

   @Override
   public void dispose() {
   }

   @Override
   public Object getData() {
      return null;
   }

   @Override
   public String getReportData() {
      return null;
   }

   @Override
   public String getXmlData() {
      return null;
   }

   @Override
   public IStatus isValid() {
      return Status.OK_STATUS;
   }

   @Override
   public void setFocus() {
   }

   @Override
   public void setXmlData(String str) {
   }

   public void setText(String str) {
      valueLabel.setText(str);
   }
}
