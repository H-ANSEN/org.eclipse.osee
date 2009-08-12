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
package org.eclipse.osee.ats.editor.widget;

import java.util.Collections;
import java.util.logging.Level;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.editor.SMAManager;
import org.eclipse.osee.ats.editor.SMAPromptChangeStatus;
import org.eclipse.osee.ats.workflow.AtsWorkPage;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.widgets.XHyperlinkLabelValueSelection;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;

/**
 * @author Donald G. Dunne
 */
public class StateHoursSpentXWidget extends XHyperlinkLabelValueSelection {

   private final SMAManager smaMgr;
   private final AtsWorkPage page;

   public StateHoursSpentXWidget(IManagedForm managedForm, AtsWorkPage page, final SMAManager smaMgr, Composite composite, int horizontalSpan, XModifiedListener xModListener) {
      super("\"" + page.getName() + "\"" + " State Hours Spent");
      this.page = page;
      this.smaMgr = smaMgr;
      if (xModListener != null) {
         addXModifiedListener(xModListener);
      }
      setEditable(!smaMgr.getSma().isReadOnly());
      setFillHorizontally(true);
      setToolTip(TOOLTIP);
      super.createWidgets(managedForm, composite, horizontalSpan);
   }

   @Override
   public boolean handleSelection() {
      try {
         SMAPromptChangeStatus.promptChangeStatus(Collections.singleton(smaMgr.getSma()), false);
         smaMgr.getEditor().onDirtied();
         return true;
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return false;
   }

   public static String TOOLTIP = "Calculation: \n     State Hours Spent: amount entered by user\n" +
   //
   "     Task Hours Spent: total hours spent of all tasks related to state\n" +
   //
   "     Review Hours Spent: total hours spent of all reviews related to state\n" +
   //
   "Total State Hours Spent: state hours + all task hours + all review hours";

   @Override
   public void refresh() {
      super.refresh();
      if (getControl().isDisposed()) {
         setValueLabel("State Percent Error: page == null");
         return;
      } else if (page == null) {
         setValueLabel("page == null");
         return;
      }

      try {
         StringBuffer sb =
               new StringBuffer(String.format("        State Hours: %5.2f", smaMgr.getStateMgr().getHoursSpent(
                     page.getName())));
         boolean breakoutNeeded = false;
         if (smaMgr.getTaskMgr().hasTaskArtifacts()) {
            sb.append(String.format("\n        Task  Hours: %5.2f", smaMgr.getTaskMgr().getHoursSpent(page.getName())));
            breakoutNeeded = true;
         }
         if (smaMgr.getReviewManager().hasReviews()) {
            sb.append(String.format("\n     Review Hours: %5.2f", smaMgr.getReviewManager().getHoursSpent(
                  page.getName())));
            breakoutNeeded = true;
         }
         if (breakoutNeeded) {
            setValueLabel(String.format("%5.2f", smaMgr.getSma().getHoursSpentSMAStateTotal(page.getName())));
            setToolTip(sb.toString());
         } else {
            setValueLabel(String.format("%5.2f", smaMgr.getStateMgr().getHoursSpent(page.getName())));
         }

      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

}
