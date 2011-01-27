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

package org.eclipse.osee.framework.ui.skynet.widgets.xmerge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.framework.access.AccessControlManager;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.help.ui.OseeHelpContext;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.Jobs;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.conflict.ArtifactConflict;
import org.eclipse.osee.framework.skynet.core.conflict.AttributeConflict;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IArtifactEventListener;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.model.EventModType;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.AbstractSelectionEnabledHandler;
import org.eclipse.osee.framework.ui.plugin.util.Commands;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.skynet.ArtifactExplorer;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.OseeStatusContributionItemFactory;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.revert.RevertWizard;
import org.eclipse.osee.framework.ui.skynet.util.SkynetViews;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericViewPart;
import org.eclipse.osee.framework.ui.skynet.widgets.xHistory.HistoryView;
import org.eclipse.osee.framework.ui.skynet.widgets.xmerge.ConflictHandlingOperation.ConflictOperationEnum;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.NonmodalWizardDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;

/**
 * @author Donald G. Dunne
 */
public class MergeView extends GenericViewPart implements IBranchEventListener, IArtifactEventListener {

   public static final String VIEW_ID = "org.eclipse.osee.framework.ui.skynet.widgets.xmerge.MergeView";

   private MergeXWidget mergeXWidget;
   private IHandlerService handlerService;
   private Branch sourceBranch;
   private Branch destBranch;
   private TransactionRecord transactionId;
   private TransactionRecord commitTrans;
   private boolean showConflicts;

   private final class MergeManagerConflictHandler extends AbstractSelectionEnabledHandler {
      private final String dialogString;
      private final ConflictOperationEnum kindOfOperation;
      private List<Conflict> conflicts;

      public MergeManagerConflictHandler(MenuManager menuManager, String dialogString, ConflictOperationEnum kindOfOperation) {
         super(menuManager);
         this.dialogString = dialogString;
         this.kindOfOperation = kindOfOperation;
      }

      @Override
      public Object executeWithException(ExecutionEvent event) {
         boolean confirm =
            MessageDialog.openConfirm(Displays.getActiveShell().getShell(), "Confirm",
               String.format(dialogString, conflicts.size()));
         if (confirm) {
            IOperation operation = new ConflictHandlingOperation(kindOfOperation, conflicts);
            Operations.executeAsJob(operation, true, Job.SHORT, new JobChangeAdapter() {
               @Override
               public void done(IJobChangeEvent event) {
                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        mergeXWidget.refresh();
                     }
                  });
               }
            });
         }
         return null;
      }

      @Override
      public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
         conflicts = mergeXWidget.getSelectedConflicts();
         return !conflicts.isEmpty();
      }
   }

   public static void openView(final Branch sourceBranch, final Branch destBranch, final TransactionRecord tranId) {
      if (Conditions.allNull(sourceBranch, destBranch, tranId)) {
         throw new IllegalArgumentException("Branch's and Transaction ID can't be null");
      }
      openViewUpon(sourceBranch, destBranch, tranId, null, true);
   }

   public static void openView(final TransactionRecord commitTrans) throws OseeCoreException {
      Conditions.checkNotNull(commitTrans, "Commit Transaction ID");
      openViewUpon(null, null, null, commitTrans, true);
   }

   private static void openViewUpon(final Branch sourceBranch, final Branch destBranch, final TransactionRecord tranId, final TransactionRecord commitTrans, final boolean showConflicts) {
      Job job = new Job("Open Merge View") {

         @Override
         protected IStatus run(final IProgressMonitor monitor) {
            Displays.ensureInDisplayThread(new Runnable() {
               @Override
               public void run() {
                  try {
                     IWorkbenchPage page = AWorkbench.getActivePage();
                     IViewPart viewPart =
                        page.showView(
                           MergeView.VIEW_ID,
                           String.valueOf(sourceBranch != null ? sourceBranch.getId() * 100000 + destBranch.getId() : commitTrans.getId()),
                           IWorkbenchPage.VIEW_ACTIVATE);
                     if (viewPart instanceof MergeView) {
                        MergeView mergeView = (MergeView) viewPart;
                        mergeView.showConflicts = showConflicts;
                        mergeView.explore(sourceBranch, destBranch, tranId, commitTrans, showConflicts);
                     }
                  } catch (Exception ex) {
                     OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
                  }
               }
            });

            monitor.done();
            return Status.OK_STATUS;
         }
      };

      Jobs.startJob(job);
   }

   private Conflict[] getConflicts() {
      return mergeXWidget != null ? mergeXWidget.getConflicts() : MergeXViewer.EMPTY_CONFLICTS;
   }

   @Override
   public void dispose() {
      super.dispose();
      OseeEventManager.removeListener(this);
      OseeEventManager.removeListener(this);
   }

   @Override
   public void createPartControl(Composite parent) {
      PlatformUI.getWorkbench().getService(IHandlerService.class);
      handlerService = (IHandlerService) getSite().getService(IHandlerService.class);

      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.verticalSpacing = 0;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      parent.setLayout(layout);
      parent.setLayoutData(new GridData(GridData.FILL_BOTH));
      mergeXWidget = new MergeXWidget();
      mergeXWidget.setDisplayLabel(false);
      mergeXWidget.createWidgets(parent, 1);

      MenuManager menuManager = new MenuManager();
      menuManager.setRemoveAllWhenShown(true);
      menuManager.addMenuListener(new IMenuListener() {
         @Override
         public void menuAboutToShow(IMenuManager manager) {
            MenuManager menuManager = (MenuManager) manager;
            fillPopupMenu(menuManager);
         }

         private void fillPopupMenu(MenuManager menuManager) {
            addEditArtifactMenuItem(menuManager);
            addMergeMenuItem(menuManager);
            menuManager.add(new Separator());
            addMarkAsResolvedMenuItem(menuManager);
            addMarkAsUnResolvedMenuItem(menuManager);
            menuManager.add(new Separator());
            addSourceAsMergeValueMenuItem(menuManager);
            addDestinationAsMergeValueMenuItem(menuManager);
            menuManager.add(new Separator());
            addResetConflictMenuItem(menuManager);
            menuManager.add(new Separator());
            addPreviewMenuItem(menuManager);
            addDiffMenuItem(menuManager);
            menuManager.add(new Separator());
            addSourceResourceHistoryMenuItem(menuManager);
            addSourceRevealMenuItem(menuManager);
            menuManager.add(new Separator());
            addDestResourceHistoryMenuItem(menuManager);
            addDestRevealMenuItem(menuManager);
            menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            addRevertUnresolvableConflictsMenuItem(menuManager);
         }
      });

      mergeXWidget.getXViewer().getTree().setMenu(menuManager.createContextMenu(mergeXWidget.getXViewer().getTree()));

      createEditArtifactMenuItem(menuManager);
      createMergeMenuItem(menuManager);
      menuManager.add(new Separator());
      createMarkResolvedMenuItem(menuManager);
      createMarkUnResolvedMenuItem(menuManager);
      menuManager.add(new Separator());
      createSourceAsMergeMenuItem(menuManager);
      createDestinationAsMergeMenuItem(menuManager);
      menuManager.add(new Separator());
      createResetConflictMenuItem(menuManager);
      menuManager.add(new Separator());
      createPreviewMenuItem(menuManager);
      createDiffMenuItem(menuManager);
      menuManager.add(new Separator());
      createSourceResourceHistoryMenuItem(menuManager);
      createSourceRevealMenuItem(menuManager);
      menuManager.add(new Separator());
      createDestinationResourceHistoryMenuItem(menuManager);
      createDestinationRevealMenuItem(menuManager);
      menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      createRevertUnresolvableConflictsMenuItem(menuManager);

      OseeStatusContributionItemFactory.addTo(this, true);
      getSite().registerContextMenu("org.eclipse.osee.framework.ui.skynet.widgets.xmerge.MergeView", menuManager,
         mergeXWidget.getXViewer());

      getSite().setSelectionProvider(mergeXWidget.getXViewer());
      HelpUtil.setHelp(parent, OseeHelpContext.MERGE_MANAGER);

      OseeEventManager.addListener(this);
      OseeEventManager.addListener(this);

      setFocusWidget(mergeXWidget.getControl());
   }

   private void addPreviewMenuItem(MenuManager menuManager) {
      MenuManager subMenuManager = new MenuManager("Preview", "previewTransaction");
      menuManager.add(subMenuManager);
      addPreviewItems(subMenuManager, "Preview Source Artifact");
      addPreviewItems(subMenuManager, "Preview Destination Artifact");
      addPreviewItems(subMenuManager, "Preview Merge Artifact");
   }

   private void createPreviewMenuItem(MenuManager menuManager) {
      MenuManager subMenuManager = new MenuManager("Preview", "previewTransaction");
      menuManager.add(subMenuManager);
      createPreviewItems(subMenuManager, new PreviewHandler(menuManager, 1), "Preview Source Artifact");
      createPreviewItems(subMenuManager, new PreviewHandler(menuManager, 2), "Preview Destination Artifact");
      createPreviewItems(subMenuManager, new PreviewHandler(menuManager, 3), "Preview Merge Artifact");
   }

   private String addPreviewItems(MenuManager subMenuManager, String command) {
      CommandContributionItem previewCommand =
         Commands.getLocalCommandContribution(getSite(), subMenuManager.getId() + command, command, null, null,
            ImageManager.getImageDescriptor(FrameworkImage.PREVIEW_ARTIFACT), null, null, null);
      subMenuManager.add(previewCommand);
      return previewCommand.getId();
   }

   private void createPreviewItems(MenuManager subMenuManager, PreviewHandler handler, String command) {
      handlerService.activateHandler(addPreviewItems(subMenuManager, command), handler);
   }

   private void addDiffMenuItem(MenuManager menuManager) {
      MenuManager subMenuManager = new MenuManager("Differences", "diffTransaction");
      menuManager.add(subMenuManager);
      addDiffItems(subMenuManager, "Show Source Branch Differences");
      addDiffItems(subMenuManager, "Show Destination Branch Differences");
      addDiffItems(subMenuManager, "Show Source/Destination Differences");
      addDiffItems(subMenuManager, "Show Source/Merge Differences");
      addDiffItems(subMenuManager, "Show Destination/Merge Differences");
   }

   private void createDiffMenuItem(MenuManager menuManager) {
      MenuManager subMenuManager = new MenuManager("Differences", "diffTransaction");
      menuManager.add(subMenuManager);
      createDiffItems(subMenuManager, new DiffHandler(menuManager, 1, mergeXWidget), "Show Source Branch Differences");
      createDiffItems(subMenuManager, new DiffHandler(menuManager, 2, mergeXWidget),
         "Show Destination Branch Differences");
      createDiffItems(subMenuManager, new DiffHandler(menuManager, 3, mergeXWidget),
         "Show Source/Destination Differences");
      createDiffItems(subMenuManager, new DiffHandler(menuManager, 4, mergeXWidget), "Show Source/Merge Differences");
      createDiffItems(subMenuManager, new DiffHandler(menuManager, 5, mergeXWidget),
         "Show Destination/Merge Differences");
   }

   private String addDiffItems(MenuManager subMenuManager, String command) {
      CommandContributionItem diffCommand =
         Commands.getLocalCommandContribution(getSite(), subMenuManager.getId() + command, command, null, null, null,
            null, null, null);
      subMenuManager.add(diffCommand);
      return diffCommand.getId();
   }

   private void createDiffItems(MenuManager subMenuManager, DiffHandler handler, String command) {
      handlerService.activateHandler(addDiffItems(subMenuManager, command), handler);
   }

   private String addEditArtifactMenuItem(MenuManager menuManager) {
      CommandContributionItem editArtifactCommand =
         Commands.getLocalCommandContribution(getSite(), "editArtifactCommand", "Edit Merge Artifact", null, null,
            null, "E", null, "edit_Merge_Artifact");
      menuManager.add(editArtifactCommand);
      return editArtifactCommand.getId();
   }

   private String addSourceResourceHistoryMenuItem(MenuManager menuManager) {
      CommandContributionItem sourecResourceCommand =
         Commands.getLocalCommandContribution(getSite(), "sourceResourceHistory",
            "Show Source Artifact Resource History", null, null,
            ImageManager.getImageDescriptor(FrameworkImage.DB_ICON_BLUE_EDIT), null, null, "source_Resource_History");
      menuManager.add(sourecResourceCommand);
      return sourecResourceCommand.getId();
   }

   private String addDestResourceHistoryMenuItem(MenuManager menuManager) {
      CommandContributionItem sourecResourceCommand =
         Commands.getLocalCommandContribution(getSite(), "destResourceHistory", "Show Dest Artifact Resource History",
            null, null, ImageManager.getImageDescriptor(FrameworkImage.DB_ICON_BLUE_EDIT), null, null,
            "dest_Resource_History");
      menuManager.add(sourecResourceCommand);
      return sourecResourceCommand.getId();
   }

   private String addSourceRevealMenuItem(MenuManager menuManager) {
      CommandContributionItem sourceReveal =
         Commands.getLocalCommandContribution(getSite(), "sourceRevealArtifactExplorer",
            "Reveal Source Artifact in Artifact Explorer", null, null,
            ImageManager.getImageDescriptor(FrameworkImage.MAGNIFY), null, null, "source_Reveal");
      menuManager.add(sourceReveal);
      return sourceReveal.getId();
   }

   private String addDestRevealMenuItem(MenuManager menuManager) {
      CommandContributionItem destReveal =
         Commands.getLocalCommandContribution(getSite(), "destRevealArtifactExplorer",
            "Reveal Dest Artifact in Artifact Explorer", null, null,
            ImageManager.getImageDescriptor(FrameworkImage.MAGNIFY), null, null, "dest_Reveal");
      menuManager.add(destReveal);
      return destReveal.getId();
   }

   private String addRevertUnresolvableConflictsMenuItem(MenuManager menuManager) {
      CommandContributionItem revertSelected =
         Commands.getLocalCommandContribution(getSite(), "revertSelected",
            "Revert Source Artifacts for Unresolvable Conflicts", null, null, null, null, null, null);
      menuManager.add(revertSelected);
      return revertSelected.getId();
   }

   private String addMarkAsResolvedMenuItem(MenuManager menuManager) {
      CommandContributionItem markAsResolvedSelected =
         Commands.getLocalCommandContribution(getSite(), "markAsResolvedSelected", "Mark as Resolved", null, null,
            null, null, null, null);
      menuManager.add(markAsResolvedSelected);
      return markAsResolvedSelected.getId();
   }

   private String addMarkAsUnResolvedMenuItem(MenuManager menuManager) {
      CommandContributionItem markAsUnResolvedSelected =
         Commands.getLocalCommandContribution(getSite(), "markAsUnResolvedSelected", "Mark as Unresolved", null, null,
            null, null, null, null);
      menuManager.add(markAsUnResolvedSelected);
      return markAsUnResolvedSelected.getId();
   }

   private String addSourceAsMergeValueMenuItem(MenuManager menuManager) {
      CommandContributionItem mergeValueSourcePickerSelected =
         Commands.getLocalCommandContribution(getSite(), "mergeValueSourcePickerSelected",
            "Resolve using Source Value", null, null, null, null, null, null);
      menuManager.add(mergeValueSourcePickerSelected);
      return mergeValueSourcePickerSelected.getId();
   }

   private String addDestinationAsMergeValueMenuItem(MenuManager menuManager) {
      CommandContributionItem mergeValueDestinationPickerSelected =
         Commands.getLocalCommandContribution(getSite(), "mergeValueDestinationPickerSelected",
            "Resolve using Destination Value", null, null, null, null, null, null);
      menuManager.add(mergeValueDestinationPickerSelected);
      return mergeValueDestinationPickerSelected.getId();
   }

   private String addResetConflictMenuItem(MenuManager menuManager) {
      CommandContributionItem resetConflictMenuItem =
         Commands.getLocalCommandContribution(getSite(), "resetConflictMenuItem", "Reset Conflict", null, null, null,
            null, null, null);
      menuManager.add(resetConflictMenuItem);
      return resetConflictMenuItem.getId();
   }

   private void createEditArtifactMenuItem(MenuManager menuManager) {

      handlerService.activateHandler(addEditArtifactMenuItem(menuManager),

      new AbstractSelectionEnabledHandler(menuManager) {
         private AttributeConflict attributeConflict;

         @Override
         public Object executeWithException(ExecutionEvent event) throws OseeCoreException {
            if (attributeConflict != null) {
               if (MergeUtility.okToOverwriteEditedValue(attributeConflict, Displays.getActiveShell().getShell(), false)) {
                  RendererManager.openInJob(attributeConflict.getArtifact(), PresentationType.SPECIALIZED_EDIT);

                  attributeConflict.markStatusToReflectEdit();
               }
            }
            return null;
         }

         @Override
         public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
            List<Conflict> conflicts = mergeXWidget.getSelectedConflicts();
            attributeConflict = null;
            if (conflicts == null || conflicts.size() != 1 || !(conflicts.get(0) instanceof AttributeConflict) || !conflicts.get(
               0).statusEditable()) {
               return false;
            }
            attributeConflict = (AttributeConflict) conflicts.get(0);
            return true;
         }
      });
   }

   private void createSourceResourceHistoryMenuItem(MenuManager menuManager) {

      handlerService.activateHandler(addSourceResourceHistoryMenuItem(menuManager),

      new AbstractSelectionEnabledHandler(menuManager) {
         private AttributeConflict attributeConflict;

         @Override
         public Object executeWithException(ExecutionEvent event) throws OseeCoreException {
            if (attributeConflict != null) {
               HistoryView.open(attributeConflict.getSourceArtifact());
            }
            return null;
         }

         @Override
         public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
            List<Conflict> conflicts = mergeXWidget.getSelectedConflicts();
            attributeConflict = null;
            if (conflicts == null || conflicts.size() != 1) {
               return false;
            }
            attributeConflict = (AttributeConflict) conflicts.get(0);
            return true;
         }
      });
   }

   private void createDestinationResourceHistoryMenuItem(MenuManager menuManager) {

      handlerService.activateHandler(addDestResourceHistoryMenuItem(menuManager),

      new AbstractSelectionEnabledHandler(menuManager) {
         private AttributeConflict attributeConflict;

         @Override
         public Object executeWithException(ExecutionEvent event) throws OseeCoreException {
            if (attributeConflict != null) {
               HistoryView.open(attributeConflict.getDestArtifact());
            }
            return null;
         }

         @Override
         public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
            List<Conflict> conflicts = mergeXWidget.getSelectedConflicts();
            attributeConflict = null;
            if (conflicts == null || conflicts.size() != 1) {
               return false;
            }
            attributeConflict = (AttributeConflict) conflicts.get(0);
            return true;
         }
      });
   }

   private void createSourceRevealMenuItem(MenuManager menuManager) {

      handlerService.activateHandler(addSourceRevealMenuItem(menuManager),

      new AbstractSelectionEnabledHandler(menuManager) {
         private AttributeConflict attributeConflict;

         @Override
         public Object executeWithException(ExecutionEvent event) throws OseeCoreException {
            if (attributeConflict != null) {
               ArtifactExplorer.revealArtifact(attributeConflict.getSourceArtifact());
            }
            return null;
         }

         @Override
         public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
            List<Conflict> conflicts = mergeXWidget.getSelectedConflicts();
            attributeConflict = null;
            if (conflicts == null || conflicts.size() != 1) {
               return false;
            }
            attributeConflict = (AttributeConflict) conflicts.get(0);
            return true;
         }
      });
   }

   private void createDestinationRevealMenuItem(MenuManager menuManager) {

      handlerService.activateHandler(addDestRevealMenuItem(menuManager),

      new AbstractSelectionEnabledHandler(menuManager) {
         private AttributeConflict attributeConflict;

         @Override
         public Object executeWithException(ExecutionEvent event) throws OseeCoreException {
            if (attributeConflict != null) {
               ArtifactExplorer.revealArtifact(attributeConflict.getDestArtifact());
            }
            return null;
         }

         @Override
         public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
            List<Conflict> conflicts = mergeXWidget.getSelectedConflicts();
            attributeConflict = null;
            if (conflicts == null || conflicts.size() != 1) {
               return false;
            }
            attributeConflict = (AttributeConflict) conflicts.get(0);
            return true;
         }
      });
   }

   private void createRevertUnresolvableConflictsMenuItem(MenuManager menuManager) {
      handlerService.activateHandler(addRevertUnresolvableConflictsMenuItem(menuManager),
         new AbstractSelectionEnabledHandler(menuManager) {
            List<List<Artifact>> revertList;
            List<Conflict> selectedConflicts;

            @Override
            public Object executeWithException(ExecutionEvent event) {
               RevertWizard wizard = new RevertWizard(revertList);
               NonmodalWizardDialog wizardDialog = new NonmodalWizardDialog(Displays.getActiveShell(), wizard);
               wizardDialog.create();
               wizardDialog.open();
               return null;
            }

            @Override
            public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
               selectedConflicts = mergeXWidget.getSelectedConflicts();
               revertList = new ArrayList<List<Artifact>>();
               populateRevertList();
               return !revertList.isEmpty();
            }

            private void populateRevertList() {
               for (Conflict conflict : selectedConflicts) {
                  ArtifactConflict artifactConflict = (ArtifactConflict) conflict;
                  if (conflict.statusNotResolvable()) {
                     addArtifactToRevertList(artifactConflict);
                  }
               }
            }

            private void addArtifactToRevertList(ArtifactConflict artifactConflict) {
               Artifact art;
               try {
                  art = artifactConflict.getSourceArtifact();
               } catch (OseeCoreException ex) {
                  return;
               }
               List<Artifact> ins = new ArrayList<Artifact>();
               ins.add(art);
               revertList.add(ins);
            }
         });
   }

   private void createMarkResolvedMenuItem(MenuManager menuManager) {
      String commandId = addMarkAsResolvedMenuItem(menuManager);
      IHandler handler =
         new MergeManagerConflictHandler(menuManager,
            "Are you sure you want to Mark the selected [%s] conflict(s) as Resolved?",
            ConflictOperationEnum.MARK_RESOLVED);
      handlerService.activateHandler(commandId, handler);
   }

   private void createMarkUnResolvedMenuItem(MenuManager menuManager) {
      String commandId = addMarkAsUnResolvedMenuItem(menuManager);
      IHandler handler =
         new MergeManagerConflictHandler(menuManager,
            "Are you sure you want to Mark the selected [%s] conflict(s) as UnResolved?",
            ConflictOperationEnum.MARK_UNRESOLVED);
      handlerService.activateHandler(commandId, handler);
   }

   private void createSourceAsMergeMenuItem(MenuManager menuManager) {
      String commandId = addSourceAsMergeValueMenuItem(menuManager);
      IHandler handler =
         new MergeManagerConflictHandler(menuManager,
            "Are you sure you want to set the Merge value to the Source value for the selected [%s] conflict(s)?",
            ConflictOperationEnum.SET_SRC_AND_RESOLVE);
      handlerService.activateHandler(commandId, handler);
   }

   private void createDestinationAsMergeMenuItem(MenuManager menuManager) {
      String commandId = addDestinationAsMergeValueMenuItem(menuManager);
      IHandler handler =
         new MergeManagerConflictHandler(menuManager,
            "Are you sure you want to set the Merge value to the Destination value for the selected [%s] conflict(s)?",
            ConflictOperationEnum.SET_DST_AND_RESOLVE);
      handlerService.activateHandler(commandId, handler);
   }

   private void createResetConflictMenuItem(MenuManager menuManager) {
      String commandId = addResetConflictMenuItem(menuManager);
      IHandler handler =
         new MergeManagerConflictHandler(menuManager, "Are you sure you want to reset %s conflict(s)?",
            ConflictOperationEnum.RESET);
      handlerService.activateHandler(commandId, handler);
   }

   private String addMergeMenuItem(MenuManager menuManager) {
      CommandContributionItem mergeArtifactCommand =
         Commands.getLocalCommandContribution(getSite(), "mergeArtifactCommand",
            "Generate Three Way Merge (Developmental)", null, null, null, "E", null,
            "Merge_Source_Destination_Artifact");
      menuManager.add(mergeArtifactCommand);
      return mergeArtifactCommand.getId();
   }

   private void createMergeMenuItem(MenuManager menuManager) {

      handlerService.activateHandler(addMergeMenuItem(menuManager),

      new AbstractSelectionEnabledHandler(menuManager) {
         private AttributeConflict attributeConflict;

         @Override
         public Object executeWithException(ExecutionEvent event) {
            if (attributeConflict != null) {
               MergeUtility.launchMerge(attributeConflict, Displays.getActiveShell().getShell());
            }
            return null;
         }

         @Override
         public boolean isEnabledWithException(IStructuredSelection structuredSelection) {
            List<Conflict> conflicts = mergeXWidget.getSelectedConflicts();
            attributeConflict = null;
            if (conflicts == null || conflicts.size() != 1 || !(conflicts.get(0) instanceof AttributeConflict) || !conflicts.get(
               0).statusEditable()) {
               return false;
            }
            attributeConflict = (AttributeConflict) conflicts.get(0);
            return attributeConflict.isWordAttribute();
         }
      });
   }

   public void explore(final Branch sourceBranch, final Branch destBranch, final TransactionRecord transactionId, final TransactionRecord commitTrans, boolean showConflicts) {
      this.sourceBranch = sourceBranch;
      this.destBranch = destBranch;
      this.transactionId = transactionId;
      this.commitTrans = commitTrans;
      try {
         mergeXWidget.setInputData(sourceBranch, destBranch, transactionId, this, commitTrans, showConflicts);
         if (sourceBranch != null) {
            setPartName("Merge Manager: " + sourceBranch.getShortName() + " <=> " + destBranch.getShortName());
         } else if (commitTrans != null) {
            setPartName("Merge Manager: " + commitTrans.getId());
         } else {
            setPartName("Merge Manager");
         }

      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);
      try {
         Integer sourceBranchId = null;
         Integer destBranchId = null;

         if (memento != null) {
            memento = memento.getChild(INPUT);
            if (memento != null) {
               if (SkynetViews.isSourceValid(memento)) {

                  Integer commitTransaction = memento.getInteger(COMMIT_NUMBER);
                  if (commitTransaction != null) {
                     openViewUpon(null, null, null, TransactionManager.getTransactionId(commitTransaction), false);
                     return;
                  }
                  sourceBranchId = memento.getInteger(SOURCE_BRANCH_ID);
                  final Branch sourceBranch = BranchManager.getBranch(sourceBranchId);
                  if (sourceBranch == null) {
                     OseeLog.log(SkynetGuiPlugin.class, Level.WARNING,
                        "Merge View can't init due to invalid source branch id " + sourceBranchId);
                     mergeXWidget.setLabel("Could not restore this Merge View");
                     return;
                  }
                  destBranchId = memento.getInteger(DEST_BRANCH_ID);
                  final Branch destBranch = BranchManager.getBranch(destBranchId);
                  if (destBranch == null) {
                     OseeLog.log(SkynetGuiPlugin.class, Level.WARNING,
                        "Merge View can't init due to invalid destination branch id " + sourceBranchId);
                     mergeXWidget.setLabel("Could not restore this Merge View");
                     return;
                  }
                  try {
                     TransactionRecord transactionId =
                        TransactionManager.getTransactionId(memento.getInteger(TRANSACTION_NUMBER));
                     openViewUpon(sourceBranch, destBranch, transactionId, null, false);
                  } catch (OseeCoreException ex) {
                     OseeLog.log(SkynetGuiPlugin.class, Level.WARNING,
                        "Merge View can't init due to invalid transaction id " + transactionId);
                     mergeXWidget.setLabel("Could not restore this Merge View due to invalid transaction id " + transactionId);
                     return;
                  }
               } else {
                  SkynetViews.closeView(VIEW_ID, getViewSite().getSecondaryId());
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.WARNING, "Merge View error on init", ex);
      }
   }

   private static final String INPUT = "input";
   private static final String SOURCE_BRANCH_ID = "sourceBranchId";
   private static final String DEST_BRANCH_ID = "destBranchId";
   private static final String TRANSACTION_NUMBER = "transactionNumber";
   private static final String COMMIT_NUMBER = "commitTransactionNumber";

   @Override
   public void saveState(IMemento memento) {
      super.saveState(memento);
      memento = memento.createChild(INPUT);
      if (sourceBranch != null) {
         memento.putInteger(SOURCE_BRANCH_ID, sourceBranch.getId());
         memento.putInteger(DEST_BRANCH_ID, destBranch.getId());
         memento.putInteger(TRANSACTION_NUMBER, transactionId.getId());
      } else if (commitTrans != null) {
         memento.putInteger(COMMIT_NUMBER, commitTrans.getId());
      }

      if (sourceBranch != null || commitTrans != null) {
         SkynetViews.addDatabaseSourceId(memento);
      }
   }

   private class PreviewHandler extends AbstractSelectionEnabledHandler {
      private final int partToPreview;
      private List<Artifact> artifacts;

      public PreviewHandler(MenuManager menuManager, int partToPreview) {
         super(menuManager);
         this.partToPreview = partToPreview;
      }

      @Override
      public Object executeWithException(ExecutionEvent event) {
         RendererManager.openInJob(artifacts, PresentationType.PREVIEW);
         return null;
      }

      @Override
      public boolean isEnabledWithException(IStructuredSelection structuredSelection) throws OseeCoreException {
         artifacts = new LinkedList<Artifact>();
         List<Conflict> conflicts = mergeXWidget.getSelectedConflicts();
         for (Conflict conflict : conflicts) {
            try {
               switch (partToPreview) {
                  case 1:
                     if (conflict.getSourceArtifact() != null) {
                        artifacts.add(conflict.getSourceArtifact());
                     }
                     break;
                  case 2:
                     if (conflict.getDestArtifact() != null) {
                        artifacts.add(conflict.getDestArtifact());
                     }
                     break;
                  case 3:
                     if (conflict.statusNotResolvable() || conflict.statusInformational()) {
                        return false;
                     }
                     if (conflict.getArtifact() != null) {
                        artifacts.add(conflict.getArtifact());
                     }
                     break;
               }
            } catch (Exception ex) {
               OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }

         return AccessControlManager.hasPermission(artifacts, PermissionEnum.READ);
      }
   }

   protected void showConflicts(boolean show) {
      showConflicts = show;
   }

   private boolean isApplicableEvent(String branchGuid, Branch mergeBranch) {
      return Conditions.in(branchGuid, mergeBranch.getGuid()) || isApplicableSourceOrDestEvent(branchGuid);
   }

   private boolean isApplicableSourceOrDestEvent(String branchGuid) {
      return Conditions.notNull(sourceBranch, destBranch) && Conditions.in(branchGuid, sourceBranch.getGuid(),
         destBranch.getGuid());
   }

   @Override
   public void handleBranchEvent(Sender sender, BranchEvent branchEvent) {
      if (!isApplicableSourceOrDestEvent(branchEvent.getBranchGuid())) {
         return;
      }
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (mergeXWidget != null && mergeXWidget.getXViewer().getTree().isDisposed() != true) {
               mergeXWidget.refresh();
            }
         }
      });
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return null;
   }

   private boolean isDisposed() {
      return Conditions.anyNull(mergeXWidget.getXViewer(), mergeXWidget.getXViewer().getTree()) || mergeXWidget.getXViewer().getTree().isDisposed();
   }

   @Override
   public void handleArtifactEvent(ArtifactEvent artifactEvent, final Sender sender) {
      if (isDisposed()) {
         OseeEventManager.removeListener(this);
         return;
      }
      Branch mergeBranch = null;
      try {
         mergeBranch = BranchManager.getMergeBranch(sourceBranch, destBranch);
         if (mergeBranch == null || !mergeBranch.getGuid().equals(artifactEvent.getBranchGuid())) {
            return;
         }
         if (!isApplicableEvent(artifactEvent.getBranchGuid(), mergeBranch)) {
            return;
         }
      } catch (OseeCoreException ex1) {
         // Do nothing here
      }
      final Collection<Artifact> modifiedArts =
         artifactEvent.getCacheArtifacts(EventModType.Modified, EventModType.Reloaded);
      final Collection<EventBasicGuidArtifact> deletedPurgedArts = artifactEvent.get(EventModType.Deleted);
      if (modifiedArts.isEmpty() && deletedPurgedArts.isEmpty()) {
         return;
      }
      final MergeView mergeView = this;
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (isDisposed()) {
               return;
            }
            for (Artifact artifact : modifiedArts) {
               try {
                  Branch branch = artifact.getBranch();
                  if (showConflicts) {
                     Conflict[] conflicts = getConflicts();
                     for (Conflict conflict : conflicts) {
                        if (artifact.equals(conflict.getSourceArtifact()) && branch.equals(conflict.getSourceBranch()) || artifact.equals(conflict.getDestArtifact()) && branch.equals(conflict.getDestBranch())) {
                           mergeXWidget.setInputData(sourceBranch, destBranch, transactionId, mergeView, commitTrans,
                              "Source Artifact Changed", showConflicts);
                           if (artifact.equals(conflict.getSourceArtifact()) && sender.isLocal()) {
                              new MessageDialog(
                                 Displays.getActiveShell().getShell(),
                                 "Modifying Source artifact while merging",
                                 null,
                                 "Typically changes done while merging should be done on the merge branch.  You should not normally merge on the source branch.",
                                 2, new String[] {"OK"}, 1).open();
                           }
                           return;
                        } else if (artifact.equals(conflict.getArtifact())) {
                           conflict.computeEqualsValues();
                           mergeXWidget.refresh();
                        }
                     }
                     if (conflicts.length > 0 && (branch.equals(conflicts[0].getSourceBranch()) || branch.equals(conflicts[0].getDestBranch()))) {
                        mergeXWidget.setInputData(
                           sourceBranch,
                           destBranch,
                           transactionId,
                           mergeView,
                           commitTrans,
                           branch.equals(conflicts[0].getSourceBranch()) ? "Source Branch Changed" : "Destination Branch Changed",
                           showConflicts);
                     }
                  }
               } catch (Exception ex) {
                  OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
               }
            }
            if (!deletedPurgedArts.isEmpty()) {
               try {
                  Branch branch = BranchManager.getBranch(deletedPurgedArts.iterator().next());
                  Conflict[] conflicts = getConflicts();
                  if (conflicts.length > 0 && (branch.equals(conflicts[0].getSourceBranch()) || branch.equals(conflicts[0].getDestBranch()))) {
                     mergeXWidget.setInputData(
                        sourceBranch,
                        destBranch,
                        transactionId,
                        mergeView,
                        commitTrans,
                        branch.equals(conflicts[0].getSourceBranch()) ? "Source Branch Changed" : "Destination Branch Changed",
                        showConflicts);
                  }
               } catch (Exception ex) {
                  OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
               }
            }
         }

      });

   }

}
