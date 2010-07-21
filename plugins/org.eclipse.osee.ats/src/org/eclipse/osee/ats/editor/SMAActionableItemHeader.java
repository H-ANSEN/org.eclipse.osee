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
package org.eclipse.osee.ats.editor;

import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.XFormToolkit;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * <REM2> handled through SMAEditorEventManager
 * 
 * @author Donald G. Dunne
 */
public class SMAActionableItemHeader extends Composite {

   private static String ACTION_ACTIONABLE_ITEMS = "Actionable Items: ";
   private Hyperlink link;
   private Label label;
   private final StateMachineArtifact sma;

   public SMAActionableItemHeader(Composite parent, XFormToolkit toolkit, StateMachineArtifact sma) throws OseeCoreException {
      super(parent, SWT.NONE);
      this.sma = sma;
      try {
         final TeamWorkFlowArtifact teamWf = (TeamWorkFlowArtifact) sma;

         toolkit.adapt(this);
         setLayout(ALayout.getZeroMarginLayout(2, false));
         GridData gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.horizontalSpan = 4;
         setLayoutData(gd);

         link = toolkit.createHyperlink(this, ACTION_ACTIONABLE_ITEMS, SWT.NONE);
         link.setToolTipText("Edit Actionable Items for the parent Action (this may add Team Workflows)");
         link.addHyperlinkListener(new IHyperlinkListener() {

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }

            public void linkActivated(HyperlinkEvent e) {
               try {
                  AtsUtil.editActionableItems(teamWf.getParentActionArtifact());
               } catch (Exception ex) {
                  OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         });

         label = toolkit.createLabel(this, " ");
         refresh();

      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

   private void refresh() throws OseeCoreException {
      if (label.isDisposed()) {
         return;
      }
      final TeamWorkFlowArtifact teamWf = (TeamWorkFlowArtifact) sma;
      if (!sma.isCancelled() && !sma.isCompleted()) {
         if (teamWf.getParentActionArtifact().getActionableItems().isEmpty()) {
            label.setText(" " + "Error: No Actionable Items identified.");
            label.setForeground(Displays.getSystemColor(SWT.COLOR_RED));
         } else {
            StringBuffer sb = new StringBuffer(teamWf.getActionableItemsDam().getActionableItemsStr());
            if (teamWf.getParentActionArtifact().getTeamWorkFlowArtifacts().size() > 1) {
               sb.append("         Other: ");
               for (TeamWorkFlowArtifact workflow : teamWf.getParentActionArtifact().getTeamWorkFlowArtifacts()) {
                  if (!workflow.equals(teamWf)) {
                     sb.append(workflow.getActionableItemsDam().getActionableItemsStr() + ", ");
                  }
               }
            }
            label.setText(sb.toString().replaceFirst(", $", ""));
            label.setForeground(Displays.getSystemColor(SWT.COLOR_BLACK));
         }
         label.update();
         layout();
      } else {
         if (teamWf.getParentActionArtifact().getActionableItems().isEmpty()) {
            label.setText(" " + "Error: No Actionable Items identified.");
            label.setForeground(Displays.getSystemColor(SWT.COLOR_RED));
         } else {
            label.setText(" " + teamWf.getParentActionArtifact().getWorldViewActionableItems());
            label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            label.setForeground(Displays.getSystemColor(SWT.COLOR_BLACK));
         }
         label.update();
         layout();
      }
   }

}
