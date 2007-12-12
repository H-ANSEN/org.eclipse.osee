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
package org.eclipse.osee.ats.util.widgets.defect;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import org.eclipse.osee.ats.util.widgets.defect.DefectItem.Severity;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.AXml;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.XDate;

/**
 * @author Donald G. Dunne
 */
public class DefectManager {

   private final Artifact artifact;
   private boolean enabled = true;
   private static final SkynetAuthentication skynetAuth = SkynetAuthentication.getInstance();
   private static String ATS_DEFECT_TAG = "AtsDefect";
   private static String DEFECT_ITEM_TAG = "Item";
   private static String REVIEW_DEFECT_ATTRIBUTE_NAME = "ats.Review Defect";

   public DefectManager(Artifact artifact) {
      this.artifact = artifact;
   }

   public String getHtml() {
      if (getDefectItems().size() == 0) return "";
      StringBuffer sb = new StringBuffer();
      sb.append(AHTML.addSpace(1) + AHTML.getLabelStr(AHTML.LABEL_FONT, "Defects"));
      sb.append(getTable());
      return sb.toString();
   }

   public Set<DefectItem> getDefectItems() {
      Set<DefectItem> defectItems = new HashSet<DefectItem>();
      String xml = artifact.getSoleAttributeValue(REVIEW_DEFECT_ATTRIBUTE_NAME);
      Matcher m =
            java.util.regex.Pattern.compile("<" + DEFECT_ITEM_TAG + ">(.*?)</" + DEFECT_ITEM_TAG + ">").matcher(xml);
      while (m.find()) {
         DefectItem item = new DefectItem(m.group());
         defectItems.add(item);
      }
      return defectItems;
   }

   public int getNumMajor() {
      int x = 0;
      for (DefectItem dItem : getDefectItems())
         if (dItem.getSeverity() == Severity.Major) x++;
      return x;
   }

   public int getNumMinor() {
      int x = 0;
      for (DefectItem dItem : getDefectItems())
         if (dItem.getSeverity() == Severity.Minor) x++;
      return x;
   }

   public int getNumIssues() {
      int x = 0;
      for (DefectItem dItem : getDefectItems())
         if (dItem.getSeverity() == Severity.Issue) x++;
      return x;
   }

   private void saveDefectItems(Set<DefectItem> defectItems, boolean persist) {
      try {
         StringBuffer sb = new StringBuffer("<" + ATS_DEFECT_TAG + ">");
         for (DefectItem item : defectItems)
            sb.append(AXml.addTagData(DEFECT_ITEM_TAG, item.toXml()));
         sb.append("</" + ATS_DEFECT_TAG + ">");
         artifact.setSoleAttributeValue(REVIEW_DEFECT_ATTRIBUTE_NAME, sb.toString());
         if (persist) artifact.persist();
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, "Can't create ats review defect document", ex, true);
      }
   }

   public void addOrUpdateDefectItem(DefectItem defectItem, boolean persist) {
      Set<DefectItem> defectItems = getDefectItems();
      boolean found = false;
      for (DefectItem dItem : defectItems) {
         if (defectItem.equals(dItem)) {
            dItem.update(defectItem);
            found = true;
         }
      }
      if (!found) defectItems.add(defectItem);
      saveDefectItems(defectItems, persist);
   }

   public void removeDefectItem(DefectItem defectItem, boolean persist) {
      Set<DefectItem> defectItems = getDefectItems();
      defectItems.remove(defectItem);
      saveDefectItems(defectItems, persist);
   }

   public void addDefectItem(String description, boolean persist) {
      DefectItem item = new DefectItem();
      item.setDescription(description);
      addOrUpdateDefectItem(item, persist);
   }

   public void clearLog(boolean persist) {
      saveDefectItems(new HashSet<DefectItem>(), persist);
   }

   public String getTable() {
      StringBuilder builder = new StringBuilder();
      builder.append("<TABLE BORDER=\"1\" cellspacing=\"1\" cellpadding=\"3%\" width=\"100%\"><THEAD><TR><TH>Severity</TH>" + "<TH>Disposition</TH><TH>Injection</TH><TH>User</TH><TH>Date</TH><TH>Description</TH><TH>Location</TH>" + "<TH>Resolution</TH><TH>Guid</TH><TH>Completed</TH></THEAD></TR>");
      for (DefectItem item : getDefectItems()) {
         User user = item.getUser();
         String name = "";
         if (user != null) {
            name = user.getName();
            if (name == null || name.equals("")) {
               name = user.getName();
            }
         }
         builder.append("<TR>");
         builder.append("<TD>" + item.getSeverity() + "</TD>");
         builder.append("<TD>" + item.getDisposition() + "</TD>");
         builder.append("<TD>" + item.getInjectionActivity() + "</TD>");
         if (user.equals(skynetAuth.getAuthenticatedUser()))
            builder.append("<TD bgcolor=\"#CCCCCC\">" + name + "</TD>");
         else
            builder.append("<TD>" + name + "</TD>");
         builder.append("<TD>" + item.getCreatedDate(XDate.MMDDYYHHMM) + "</TD>");
         builder.append("<TD>" + item.getDescription() + "</TD>");
         builder.append("<TD>" + item.getLocation() + "</TD>");
         builder.append("<TD>" + item.getResolution() + "</TD>");
         builder.append("<TD>" + item.getGuid() + "</TD>");
         builder.append("<TD>" + item.isClosed() + "</TD>");
         builder.append("</TR>");
      }
      builder.append("</TABLE>");
      return builder.toString();
   }

   public boolean isEnabled() {
      return enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

}