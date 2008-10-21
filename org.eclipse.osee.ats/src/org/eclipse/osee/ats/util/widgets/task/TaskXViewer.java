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
package org.eclipse.osee.ats.util.widgets.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.artifact.ATSAttributes;
import org.eclipse.osee.ats.artifact.TaskArtifact;
import org.eclipse.osee.ats.editor.SMAManager;
import org.eclipse.osee.ats.util.AtsRelation;
import org.eclipse.osee.ats.util.widgets.dialog.TaskResOptionDefinition;
import org.eclipse.osee.ats.world.WorldXViewer;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.Sender;
import org.eclipse.osee.framework.skynet.core.utility.LoadedArtifacts;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.skynet.artifact.ArtifactPromptChange;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class TaskXViewer extends WorldXViewer {

   private final XTaskViewer xTaskViewer;
   private final IDirtiableEditor editor;
   private final List<TaskResOptionDefinition> taskResOptionDefinitions;
   private Map<String, TaskResOptionDefinition> nameToResOptionDef = null;
   private boolean tasksEditable = true;
   private static String viewerId = GUID.generateGuidStr();

   /**
    * @param parent
    * @param style
    */
   public TaskXViewer(Composite parent, int style, IDirtiableEditor editor, List<TaskResOptionDefinition> taskResOptionDefinition, XTaskViewer xTaskViewer) {
      super(parent, style, new TaskXViewerFactory());
      this.editor = editor;
      this.taskResOptionDefinitions = taskResOptionDefinition;
      this.xTaskViewer = xTaskViewer;
   }

   @Override
   public String toString() {
      if (xTaskViewer == null) return "TaskXViewer";
      try {
         if (xTaskViewer.getIXTaskViewer().getParentSmaMgr() != null) {
            return "TaskXViewer - id:" + viewerId + " - " + xTaskViewer.getIXTaskViewer().getParentSmaMgr().getSma().toString();
         }
         return "TaskXViewer - id:" + viewerId + " - " + xTaskViewer.getIXTaskViewer().toString();
      } catch (Exception ex) {
         return "TaskXViewer - id:" + viewerId;
      }
   }

   public boolean isUsingTaskResolutionOptions() {
      return this.taskResOptionDefinitions != null && taskResOptionDefinitions.size() > 0;
   }

   @Override
   public void handleColumnMultiEdit(TreeColumn treeColumn, Collection<TreeItem> treeItems) {
      handleColumnMultiEdit(treeColumn, treeItems, false);
      Set<TaskArtifact> items = new HashSet<TaskArtifact>();
      for (TreeItem item : treeItems)
         items.add((TaskArtifact) item.getData());
      refresh();
      editor.onDirtied();
   }

   @Override
   public void set(Collection<? extends Artifact> artifacts) {
      for (Artifact art : artifacts)
         if (!(art instanceof TaskArtifact)) throw new IllegalArgumentException("set only allowed for TaskArtifact");
      ((TaskContentProvider) getContentProvider()).set(artifacts);
   }

   @Override
   public void add(final Artifact artifact) {
      if (!(artifact instanceof TaskArtifact)) throw new IllegalArgumentException("set only allowed for TaskArtifact");
      add(Arrays.asList(artifact));
   }

   @Override
   public void add(Collection<Artifact> artifacts) {
      for (Artifact art : artifacts)
         if (!(art instanceof TaskArtifact)) throw new IllegalArgumentException("add only allowed for TaskArtifact");
      ((TaskContentProvider) getContentProvider()).add(artifacts);
   }

   public void removeTask(final Collection<TaskArtifact> artifacts) {
      ((TaskContentProvider) getContentProvider()).remove(artifacts);
   }

   public TaskArtifact getSelectedTaskArtifact() {
      Collection<TaskArtifact> arts = getSelectedTaskArtifacts();
      if (arts.size() > 0) return arts.iterator().next();
      return null;
   }

   public Collection<TaskArtifact> getSelectedTaskArtifacts() {
      Iterator<?> i = ((IStructuredSelection) getSelection()).iterator();
      ArrayList<TaskArtifact> taskArts = new ArrayList<TaskArtifact>();
      while (i.hasNext()) {
         Object obj = i.next();
         if (obj instanceof TaskArtifact) taskArts.add((TaskArtifact) obj);
      }
      return taskArts;
   }

   public boolean isSelectedTaskArtifactsAreInWork() {
      Iterator<?> i = ((IStructuredSelection) getSelection()).iterator();
      while (i.hasNext()) {
         Object obj = i.next();
         if (obj instanceof TaskArtifact) if (!((TaskArtifact) obj).isInWork()) return false;
      }
      return true;
   }

   Action editTaskTitleAction;
   Action editTaskAssigneesAction;
   Action editTaskStatusAction;
   Action editTaskResolutionAction;
   Action editTaskEstimateAction;
   Action editTaskRelatedStateAction;
   Action editTaskNotesAction;
   Action addNewTaskAction;
   Action deleteTasksAction;

   @Override
   public void createMenuActions() {
      super.createMenuActions();

      editTaskTitleAction = new Action("Edit Task Title", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            SMAManager taskSmaMgr = new SMAManager(getSelectedTaskArtifact());
            if (taskSmaMgr.promptChangeAttribute(ATSAttributes.TITLE_ATTRIBUTE, false)) {
               editor.onDirtied();
               update(getSelectedTaskArtifacts().toArray(), null);
            }
         }
      };

      editTaskAssigneesAction = new Action("Edit Task Assignees", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            try {
               if (SMAManager.promptChangeAssignees(getSelectedTaskArtifacts())) {
                  editor.onDirtied();
                  update(getSelectedTaskArtifacts().toArray(), null);
               }
            } catch (Exception ex) {
               OSEELog.logException(AtsPlugin.class, ex, true);
            }
         }
      };

      editTaskStatusAction = new Action("Edit Task Status", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            try {
               if (SMAManager.promptChangeStatus((isUsingTaskResolutionOptions() ? taskResOptionDefinitions : null),
                     getSelectedTaskArtifacts(), false)) {
                  editor.onDirtied();
                  update(getSelectedTaskArtifacts().toArray(), null);
               }
            } catch (Exception ex) {
               OSEELog.logException(AtsPlugin.class, ex, true);
            }
         }
      };

      editTaskResolutionAction = new Action("Edit Task Resolution", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            try {
               handleChangeResolution();
            } catch (Exception ex) {
               OSEELog.logException(AtsPlugin.class, ex, true);
            }
         }
      };

      editTaskEstimateAction = new Action("Edit Task Estimate", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            try {
               if (ArtifactPromptChange.promptChangeFloatAttribute(
                     ATSAttributes.ESTIMATED_HOURS_ATTRIBUTE.getStoreName(),
                     ATSAttributes.ESTIMATED_HOURS_ATTRIBUTE.getDisplayName(), getSelectedTaskArtifacts(), false)) {
                  editor.onDirtied();
                  update(getSelectedTaskArtifacts().toArray(), null);
               }
            } catch (Exception ex) {
               OSEELog.logException(AtsPlugin.class, ex, true);
            }
         }
      };

      editTaskRelatedStateAction = new Action("Edit Task Related to State", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            if (SMAManager.promptChangeAttribute(ATSAttributes.RELATED_TO_STATE_ATTRIBUTE, getSelectedTaskArtifacts(),
                  false)) {
               editor.onDirtied();
               update(getSelectedTaskArtifacts().toArray(), null);
            }
         }
      };

      editTaskNotesAction = new Action("Edit Task Notes", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            if (SMAManager.promptChangeAttribute(ATSAttributes.SMA_NOTE_ATTRIBUTE, getSelectedTaskArtifacts(), false)) {
               editor.onDirtied();
               update(getSelectedTaskArtifacts().toArray(), null);
            }
         }
      };

      addNewTaskAction = new Action("New Task", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            xTaskViewer.handleNewTask();
         }
      };

      deleteTasksAction = new Action("Delete Task", Action.AS_PUSH_BUTTON) {
         @Override
         public void run() {
            try {
               xTaskViewer.handleDeleteTask();
            } catch (Exception ex) {
               OSEELog.logException(AtsPlugin.class, ex, true);
            }
         }
      };

   }

   @Override
   public void updateEditMenuActions() {
      MenuManager mm = getMenuManager();

      // EDIT MENU BLOCK
      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_EDIT, editTaskTitleAction);
      editTaskTitleAction.setEnabled(isTasksEditable() && getSelectedArtifacts().size() == 1 && isSelectedTaskArtifactsAreInWork());

      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_EDIT, editTaskAssigneesAction);
      editTaskAssigneesAction.setEnabled(isTasksEditable() && getSelectedArtifacts().size() > 0 && isSelectedTaskArtifactsAreInWork());

      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_EDIT, editTaskStatusAction);
      editTaskStatusAction.setEnabled(isTasksEditable() && getSelectedArtifacts().size() > 0);

      if (!isUsingTaskResolutionOptions()) {
         mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_EDIT, editTaskResolutionAction);
         editTaskResolutionAction.setEnabled(isTasksEditable() && getSelectedArtifacts().size() > 0 && isSelectedTaskArtifactsAreInWork());
      }

      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_EDIT, editTaskEstimateAction);
      editTaskEstimateAction.setEnabled(isTasksEditable() && getSelectedArtifacts().size() > 0 && isSelectedTaskArtifactsAreInWork());

      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_EDIT, editTaskRelatedStateAction);
      editTaskRelatedStateAction.setEnabled(isTasksEditable() && getSelectedArtifacts().size() > 0 && isSelectedTaskArtifactsAreInWork());

      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_EDIT, editTaskNotesAction);
      editTaskNotesAction.setEnabled(isTasksEditable() && getSelectedArtifacts().size() > 0 && isSelectedTaskArtifactsAreInWork());

   }

   @Override
   public void updateMenuActions() {
      super.updateMenuActions();
      MenuManager mm = getMenuManager();

      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_OPEN, new Separator());
      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_OPEN, addNewTaskAction);
      addNewTaskAction.setEnabled(isTasksEditable());

      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_OPEN, deleteTasksAction);
      deleteTasksAction.setEnabled(isTasksEditable() && getSelectedTaskArtifacts().size() > 0);

   }

   public boolean handleChangeResolution() throws OseeCoreException {
      if (isUsingTaskResolutionOptions()) {
         if (SMAManager.promptChangeStatus(taskResOptionDefinitions, getSelectedTaskArtifacts(), false)) {
            editor.onDirtied();
            update(getSelectedTaskArtifacts().toArray(), null);
            return true;
         }
      } else if (SMAManager.promptChangeAttribute(ATSAttributes.RESOLUTION_ATTRIBUTE, getSelectedTaskArtifacts(), false)) {
         editor.onDirtied();
         update(getSelectedTaskArtifacts().toArray(), null);
         return true;
      }
      return false;
   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem, boolean persist) {
      if (!isTasksEditable()) {
         AWorkbench.popup("ERROR", "Editing disabled for current state.");
         return false;
      }
      XViewerColumn xCol = (XViewerColumn) treeColumn.getData();
      SMAManager taskSmaMgr = new SMAManager((TaskArtifact) treeItem.getData());
      boolean modified = false;
      try {
         if (isSelectedTaskArtifactsAreInWork() && xCol.equals(WorldXViewerFactory.Estimated_Hours_Col)) {
            modified = taskSmaMgr.promptChangeFloatAttribute(ATSAttributes.ESTIMATED_HOURS_ATTRIBUTE, false);
         } else if (isSelectedTaskArtifactsAreInWork() && xCol.equals(WorldXViewerFactory.Title_Col)) {
            modified = taskSmaMgr.promptChangeAttribute(ATSAttributes.TITLE_ATTRIBUTE, false);
         } else if (isSelectedTaskArtifactsAreInWork() && xCol.equals(WorldXViewerFactory.Related_To_State_Col)) {
            modified = taskSmaMgr.promptChangeAttribute(ATSAttributes.RELATED_TO_STATE_ATTRIBUTE, false);
         } else if (isSelectedTaskArtifactsAreInWork() && xCol.equals(WorldXViewerFactory.Assignees_Col)) {
            modified = taskSmaMgr.promptChangeAssignees();
         } else if (isUsingTaskResolutionOptions() && (xCol.equals(WorldXViewerFactory.Hours_Spent_State_Col) || xCol.equals(WorldXViewerFactory.Hours_Spent_Total_Col) || xCol.equals(WorldXViewerFactory.Percent_Complete_State_Col) || xCol.equals(WorldXViewerFactory.Percent_Complete_Total_Col))) {
            modified = handleChangeResolution();
         } else if (isSelectedTaskArtifactsAreInWork() && xCol.equals(WorldXViewerFactory.Resolution_Col)) {
            modified = handleChangeResolution();
         } else if (xCol.equals(WorldXViewerFactory.Hours_Spent_State_Col) || xCol.equals(WorldXViewerFactory.Hours_Spent_Total_Col) || xCol.equals(WorldXViewerFactory.Percent_Complete_State_Col) || xCol.equals(WorldXViewerFactory.Percent_Complete_Total_Col)) {
            modified = taskSmaMgr.promptChangeStatus(false);
         } else
            modified = super.handleAltLeftClick(treeColumn, treeItem, false);

         if (modified) {
            editor.onDirtied();
            update((treeItem.getData()), null);
            return true;
         }
      } catch (Exception ex) {
         OSEELog.logException(AtsPlugin.class, ex, true);
      }
      return false;
   }

   /**
    * @return the tasksEditable
    */
   public boolean isTasksEditable() {
      return tasksEditable;
   }

   /**
    * @param tasksEditable the tasksEditable to set
    */
   public void setTasksEditable(boolean tasksEditable) {
      this.tasksEditable = tasksEditable;
   }

   /**
    * @return the TaskResOptionDefinition
    */
   public TaskResOptionDefinition getTaskResOptionDefinition(String optionName) {
      if (nameToResOptionDef == null) {
         nameToResOptionDef = new HashMap<String, TaskResOptionDefinition>();
         for (TaskResOptionDefinition def : taskResOptionDefinitions) {
            nameToResOptionDef.put(def.getName(), def);
         }
      }
      return nameToResOptionDef.get(optionName);
   }

   @Override
   public void handleArtifactsPurgedEvent(Sender sender, final LoadedArtifacts loadedArtifacts) {
      try {
         if (loadedArtifacts.getLoadedArtifacts().size() == 0) return;
         // ContentProvider ensures in display thread
         Displays.ensureInDisplayThread(new Runnable() {
            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {
               try {
                  ((TaskContentProvider) xTaskViewer.getXViewer().getContentProvider()).remove(loadedArtifacts.getLoadedArtifacts());
               } catch (Exception ex) {
                  OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
               }
            }
         });
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

   @Override
   public void handleArtifactsChangeTypeEvent(Sender sender, int toArtifactTypeId, LoadedArtifacts loadedArtifacts) {
      try {
         if (loadedArtifacts.getLoadedArtifacts().size() == 0) return;
         // ContentProvider ensures in display thread
         ((TaskContentProvider) xTaskViewer.getXViewer().getContentProvider()).remove(loadedArtifacts.getLoadedArtifacts());
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.world.WorldXViewer#dispose()
    */
   @Override
   public void dispose() {
      OseeEventManager.removeListener(this);
      super.dispose();
   }

   @Override
   public void handleFrameworkTransactionEvent(Sender sender, final FrameworkTransactionData transData) {
      if (transData.branchId != AtsPlugin.getAtsBranch().getBranchId()) return;
      Displays.ensureInDisplayThread(new Runnable() {
         /* (non-Javadoc)
          * @see java.lang.Runnable#run()
          */
         @Override
         public void run() {
            if (xTaskViewer.getXViewer().getContentProvider() == null) return;
            ((TaskContentProvider) xTaskViewer.getXViewer().getContentProvider()).remove(transData.cacheDeletedArtifacts);
            xTaskViewer.getXViewer().update(transData.cacheChangedArtifacts, null);

            try {
               if (xTaskViewer.getIXTaskViewer().getParentSmaMgr() == null) {
                  return;
               }
               Artifact parentSma = xTaskViewer.getIXTaskViewer().getParentSmaMgr().getSma();
               if (parentSma != null) {
                  // Add any new tasks related to parent sma
                  Collection<Artifact> artifacts =
                        transData.getRelatedArtifacts(parentSma.getArtId(),
                              AtsRelation.SmaToTask_Task.getRelationType().getRelationTypeId(),
                              AtsPlugin.getAtsBranch().getBranchId(), transData.cacheAddedRelations);
                  if (artifacts.size() > 0) {
                     ((TaskContentProvider) xTaskViewer.getXViewer().getContentProvider()).add(artifacts);
                  }

                  // Remove any tasks related to parent sma
                  artifacts =
                        transData.getRelatedArtifacts(parentSma.getArtId(),
                              AtsRelation.SmaToTask_Task.getRelationType().getRelationTypeId(),
                              AtsPlugin.getAtsBranch().getBranchId(), transData.cacheDeletedRelations);
                  if (artifacts.size() > 0) {
                     ((TaskContentProvider) xTaskViewer.getXViewer().getContentProvider()).remove(artifacts);
                  }
               }
            } catch (Exception ex) {
               OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
            }
         }
      });
   }

}
