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
package org.eclipse.osee.framework.ui.skynet.artifact.editor;

import java.util.logging.Level;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactModType;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.event.AccessControlEventType;
import org.eclipse.osee.framework.skynet.core.event.BranchEventType;
import org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData;
import org.eclipse.osee.framework.skynet.core.event.IAccessControlEventListener;
import org.eclipse.osee.framework.skynet.core.event.IArtifactModifiedEventListener;
import org.eclipse.osee.framework.skynet.core.event.IArtifactsChangeTypeEventListener;
import org.eclipse.osee.framework.skynet.core.event.IArtifactsPurgedEventListener;
import org.eclipse.osee.framework.skynet.core.event.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.IFrameworkTransactionEventListener;
import org.eclipse.osee.framework.skynet.core.event.IRelationModifiedEventListener;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.Sender;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationModType;
import org.eclipse.osee.framework.skynet.core.utility.LoadedArtifacts;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

/**
 * @author Jeff C. Phillips
 */
public abstract class AbstractEventArtifactEditor extends AbstractArtifactEditor {

   private InternalEventHandler internalEventHandler;

   public AbstractEventArtifactEditor() {
      super();
      internalEventHandler = new InternalEventHandler();
      OseeEventManager.addListener(internalEventHandler);
   }

   /* (non-Javadoc)
    * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
    */
   @Override
   protected void addPages() {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see org.eclipse.ui.forms.editor.FormEditor#isDirty()
    */
   @Override
   public boolean isDirty() {
      boolean wasDirty = false;
      Artifact artifact = getArtifactFromEditorInput();
      if (artifact != null) {
         if (!artifact.isDeleted() && !artifact.isReadOnly()) {
            wasDirty = super.isDirty() || artifact.isDirty(true);
         }
      }
      return wasDirty;
   }

   protected abstract void checkEnabledTooltems();

   protected abstract void refreshDirtyArtifact();

   protected abstract void closeEditor();

   protected abstract void refreshRelations();

   /* (non-Javadoc)
    * @see org.eclipse.ui.forms.editor.FormEditor#dispose()
    */
   @Override
   public void dispose() {
      super.dispose();
      OseeEventManager.removeListener(internalEventHandler);
   }

   private final class InternalEventHandler implements IArtifactsPurgedEventListener, IBranchEventListener, IAccessControlEventListener, IArtifactModifiedEventListener, IArtifactsChangeTypeEventListener, IRelationModifiedEventListener, IFrameworkTransactionEventListener {
      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.skynet.core.event.IArtifactModifiedEventListener#handleArtifactModifiedEvent(org.eclipse.osee.framework.skynet.core.event.Sender, org.eclipse.osee.framework.skynet.core.artifact.ArtifactModType, org.eclipse.osee.framework.skynet.core.artifact.Artifact)
       */
      @Override
      public void handleArtifactModifiedEvent(Sender sender, ArtifactModType artifactModType, Artifact artifact) {
         if (getArtifactFromEditorInput() == null || !getArtifactFromEditorInput().equals(artifact)) return;
         if (artifactModType == ArtifactModType.Added || artifactModType == ArtifactModType.Changed || artifactModType == ArtifactModType.Reverted) {
            refreshDirtyArtifact();
         }
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.skynet.core.event.IArtifactsChangeTypeEventListener#handleArtifactsChangeTypeEvent(org.eclipse.osee.framework.skynet.core.event.Sender, int, org.eclipse.osee.framework.skynet.core.utility.LoadedArtifacts)
       */
      @Override
      public void handleArtifactsChangeTypeEvent(Sender sender, int toArtifactTypeId, LoadedArtifacts loadedArtifacts) {
         try {
            Artifact localArtifact = getArtifactFromEditorInput();
            if (loadedArtifacts.getLoadedArtifacts().contains(localArtifact)) {
               closeEditor();
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.skynet.core.event.IRelationModifiedEventListener#handleRelationModifiedEvent(org.eclipse.osee.framework.skynet.core.event.Sender, org.eclipse.osee.framework.skynet.core.relation.RelationModType, org.eclipse.osee.framework.skynet.core.relation.RelationLink, org.eclipse.osee.framework.skynet.core.artifact.Branch, java.lang.String)
       */
      @Override
      public void handleRelationModifiedEvent(Sender sender, RelationModType relationModType, RelationLink link, Branch branch, String relationType) {
         try {
            Artifact localArtifact = getArtifactFromEditorInput();
            if (link.getArtifactA().equals(localArtifact) || link.getArtifactB().equals(localArtifact)) {
               refreshRelations();
               onDirtied();
            }
         } catch (Exception ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.skynet.core.event.IFrameworkTransactionEventListener#handleFrameworkTransactionEvent(org.eclipse.osee.framework.skynet.core.event.Sender, org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData)
       */
      @Override
      public void handleFrameworkTransactionEvent(Sender sender, FrameworkTransactionData transData) throws OseeCoreException {
         Artifact localArtifact = getArtifactFromEditorInput();
         if (!transData.isHasEvent(localArtifact)) {
            return;
         }
         if (transData.isDeleted(localArtifact)) {
            closeEditor();
         }
         if (transData.isRelAddedChangedDeleted(localArtifact)) {
            refreshRelations();
         }
         if (transData.isChanged(localArtifact)) {
            refreshDirtyArtifact();
         }
         onDirtied();
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.skynet.core.event.IArtifactsPurgedEventListener#handleArtifactsPurgedEvent(org.eclipse.osee.framework.skynet.core.event.Sender, org.eclipse.osee.framework.skynet.core.utility.LoadedArtifacts)
       */
      @Override
      public void handleArtifactsPurgedEvent(Sender sender, LoadedArtifacts loadedArtifacts) throws OseeCoreException {
         try {
            if (loadedArtifacts.getLoadedArtifacts().contains(getArtifactFromEditorInput())) {
               closeEditor();
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.skynet.core.event.IBranchEventListener#handleBranchEvent(org.eclipse.osee.framework.skynet.core.event.Sender, org.eclipse.osee.framework.skynet.core.event.BranchEventType, int)
       */
      @Override
      public void handleBranchEvent(Sender sender, BranchEventType branchModType, int branchId) {
         if (branchModType == BranchEventType.Committed) {
            if (getArtifactFromEditorInput().getBranch().getBranchId() == branchId) {
               closeEditor();
            }
         }
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.skynet.core.event.IBranchEventListener#handleLocalBranchToArtifactCacheUpdateEvent(org.eclipse.osee.framework.skynet.core.event.Sender)
       */
      @Override
      public void handleLocalBranchToArtifactCacheUpdateEvent(Sender sender) {
         // TODO Auto-generated method stub
      }

      /* (non-Javadoc)
       * @see org.eclipse.osee.framework.skynet.core.event.IAccessControlEventListener#handleAccessControlArtifactsEvent(org.eclipse.osee.framework.skynet.core.event.Sender, org.eclipse.osee.framework.skynet.core.event.AccessControlEventType, org.eclipse.osee.framework.skynet.core.utility.LoadedArtifacts)
       */
      @Override
      public void handleAccessControlArtifactsEvent(Sender sender, AccessControlEventType accessControlEventType, LoadedArtifacts loadedArtifacts) {
         try {
            if (accessControlEventType == AccessControlEventType.ArtifactsLocked || accessControlEventType == AccessControlEventType.ArtifactsUnlocked) {
               if (loadedArtifacts.getLoadedArtifacts().contains(getArtifactFromEditorInput())) {
                  Displays.ensureInDisplayThread(new Runnable() {
                     /* (non-Javadoc)
                      * @see java.lang.Runnable#run()
                      */
                     @Override
                     public void run() {
                        setTitleImage(getArtifactFromEditorInput().getImage());
                     }
                  });
               }
            }
         } catch (Exception ex) {
            // do nothing
         }
      }
   }
}
