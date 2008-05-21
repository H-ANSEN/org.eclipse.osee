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
package org.eclipse.osee.framework.ui.skynet.group;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.LocalTransactionEvent;
import org.eclipse.osee.framework.skynet.core.event.RemoteTransactionEvent;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.event.TransactionEvent;
import org.eclipse.osee.framework.skynet.core.event.TransactionEvent.EventData;
import org.eclipse.osee.framework.skynet.core.relation.CoreRelationEnumeration;
import org.eclipse.osee.framework.ui.plugin.event.Event;
import org.eclipse.osee.framework.ui.plugin.event.IEventReceiver;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

/**
 * @author Donald G. Dunne
 */
public class GroupExplorerItem implements IEventReceiver {

   private Artifact artifact;
   private final TreeViewer treeViewer;
   private SkynetEventManager eventManager;
   private GroupExplorerItem parentItem;
   private List<GroupExplorerItem> groupItems;
   private final GroupExplorer groupExplorer;

   public GroupExplorerItem(TreeViewer treeViewer, Artifact artifact, GroupExplorerItem parentItem, GroupExplorer groupExplorer) {
      this.treeViewer = treeViewer;
      this.artifact = artifact;
      this.parentItem = parentItem;
      this.groupExplorer = groupExplorer;
      eventManager = SkynetEventManager.getInstance();
      eventManager.register(RemoteTransactionEvent.class, this);
      eventManager.register(LocalTransactionEvent.class, this);
   }

   public boolean contains(Artifact artifact) {
      for (GroupExplorerItem item : getGroupItems()) {
         if (item.getArtifact() != null && item.getArtifact().equals(artifact)) return true;
      }
      return false;
   }

   /**
    * @param artifact to match with
    * @return UGI that contains artifact
    */
   public GroupExplorerItem getItem(Artifact artifact) {
      if (this.artifact != null && this.artifact.equals(artifact)) return this;
      for (GroupExplorerItem item : getGroupItems()) {
         GroupExplorerItem ugi = item.getItem(artifact);
         if (ugi != null) return ugi;
      }
      return null;
   }

   public void dispose() {
      eventManager.unRegisterAll(this);
      if (groupItems != null) for (GroupExplorerItem item : groupItems)
         item.dispose();
   }

   public boolean isUniversalGroup() {
      if (artifact == null || artifact.isDeleted()) return false;
      return artifact.getArtifactTypeNameSuppressException().equals("Universal Group");
   }

   public String getTableArtifactType() {
      return artifact.getArtifactTypeNameSuppressException();
   }

   public String getTableArtifactName() {
      return artifact.getDescriptiveName();
   }

   public String getTableArtifactDescription() {
      return null;
   }

   public Artifact getArtifact() {
      return artifact;
   }

   public List<GroupExplorerItem> getGroupItems() {
      // Light loading; load the first time getChildren is called
      if (groupItems == null) {
         groupItems = new ArrayList<GroupExplorerItem>();
         populateUpdateCategory();
      }
      List<GroupExplorerItem> items = new ArrayList<GroupExplorerItem>();
      if (groupItems != null) items.addAll(groupItems);
      return items;
   }

   /**
    * Populate/Update this category with it's necessary children items
    */
   public void populateUpdateCategory() {
      try {
         for (GroupExplorerItem item : getGroupItems()) {
            removeGroupItem(item);
         }
         for (Artifact art : artifact.getRelatedArtifacts(CoreRelationEnumeration.UNIVERSAL_GROUPING__MEMBERS)) {
            addGroupItem(new GroupExplorerItem(treeViewer, art, this, groupExplorer));
         }
      } catch (SQLException ex) {
         SkynetGuiPlugin.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
   }

   public void addGroupItem(GroupExplorerItem item) {
      groupItems.add(item);
   }

   public void removeGroupItem(GroupExplorerItem item) {
      item.dispose();
      groupItems.remove(item);
   }

   public void onEvent(final Event event) {
      if (treeViewer == null || treeViewer.getTree().isDisposed() || (artifact != null && artifact.isDeleted())) {
         dispose();
         return;
      }
      final GroupExplorerItem tai = this;

      if (event instanceof TransactionEvent) {
         EventData ed = ((TransactionEvent) event).getEventData(artifact);
         if (ed.isRemoved()) {
            treeViewer.refresh();
            groupExplorer.restoreSelection();
         } else if (ed.getAvie() != null && ed.getAvie().getOldVersion().equals(artifact)) {
            if (artifact == ed.getAvie().getOldVersion()) {
               artifact = ed.getAvie().getNewVersion();
               treeViewer.refresh(tai);
               groupExplorer.restoreSelection();
            }
         } else if (ed.isModified()) {
            treeViewer.update(tai, null);
         } else if (ed.isRelChange()) {
            populateUpdateCategory();
            treeViewer.refresh(tai);
            groupExplorer.restoreSelection();
         }
      } else
         SkynetGuiPlugin.getLogger().log(Level.SEVERE, "Unexpected event => " + event);
   }

   public boolean runOnEventInDisplayThread() {
      return true;
   }

   public GroupExplorerItem getParentItem() {
      return parentItem;
   }

}
