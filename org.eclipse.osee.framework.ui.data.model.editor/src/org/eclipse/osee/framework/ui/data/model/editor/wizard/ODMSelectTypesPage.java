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
package org.eclipse.osee.framework.ui.data.model.editor.wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.internal.ide.misc.ResourceAndContainerGroup;

/**
 * @author Roberto E. Escobar
 */
public class ODMSelectTypesPage extends WizardPage {

   private List list;

   public ODMSelectTypesPage(String pageName) {
      super(pageName, "Specify Osee Data Model Types", null);
      setDescription("You can specify existing ecore files, or create new ones.  There" + " must be at least one ecore file specified.");
   }

   public void createControl(Composite parent) {
      Composite page = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 3;
      page.setLayout(layout);

      Label label = new Label(page, SWT.NONE);
      GridData data = new GridData();
      data.verticalAlignment = GridData.BEGINNING;
      label.setLayoutData(data);
      label.setText("&ECore files: ");

      list = new List(page, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      data = new GridData(SWT.FILL, SWT.FILL, true, false);
      data.heightHint = 80;
      list.setLayoutData(data);

      Composite buttonBar = new Composite(page, SWT.NONE);
      RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
      rowLayout.fill = true;
      buttonBar.setLayout(rowLayout);
      data = new GridData();
      data.verticalAlignment = SWT.TOP;
      buttonBar.setLayoutData(data);

      Button create = new Button(buttonBar, SWT.PUSH);
      create.setText("&Create");
      create.addSelectionListener(new SelectionListener() {
         public void widgetSelected(SelectionEvent e) {
            new CreationDialog().open();
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }
      });
      Button browseWorkspace = new Button(buttonBar, SWT.PUSH);
      browseWorkspace.setText("Browse &Workspace");
      browseWorkspace.addSelectionListener(new SelectionListener() {
         public void widgetSelected(SelectionEvent e) {
            ResourceSelectionDialog dialog =
                  new ResourceSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(),
                        "Select .ecore files");
            if (dialog.open() == Window.OK) {
               Object[] result = dialog.getResult();
               for (int i = 0; i < result.length; i++) {
                  String file = ((IResource) result[i]).getFullPath().toString();
                  if (file.toLowerCase().endsWith(".ecore")) list.add(file);
               }
               updatePageStatus();
            }
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }
      });
      Button specifyPlugin = new Button(buttonBar, SWT.PUSH);
      specifyPlugin.setText("&Plugin Format");
      specifyPlugin.addSelectionListener(new SelectionListener() {
         public void widgetSelected(SelectionEvent e) {
            InputDialog dialog =
                  new InputDialog(
                        getShell(),
                        "Specify .ecore file",
                        "Here you can specify Ecore files in binary, external plug-ins.  " + "Enter the plugin name and path below.  Use forward slash as path" + " separator.\nNOTE: Changes made to Ecore" + " files loaded in this format cannot be saved." + "\n\nExample: org.eclipse.emf.ecore/model/Ecore.ecore",
                        null, new IInputValidator() {
                           public String isValid(String newText) {
                              if (newText != null && newText.trim().length() > 8 && newText.indexOf(":") == -1 && newText.indexOf("\\") == -1 && !newText.startsWith("/") && newText.indexOf("/") != -1 && newText.toLowerCase().endsWith(
                                    ".ecore")) return null;
                              return "Invalid input";
                           }
                        });
            if (dialog.open() == Window.OK) {
               list.add("platform:/plugin/" + dialog.getValue().trim());
               updatePageStatus();
            }
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }
      });
      Button browseFileSystem = new Button(buttonBar, SWT.PUSH);
      browseFileSystem.setText("Browse File &System");
      browseFileSystem.addSelectionListener(new SelectionListener() {
         public void widgetSelected(SelectionEvent e) {
            FileDialog dialog = new FileDialog(getShell());
            dialog.setFilterExtensions(new String[] {"*.ecore"});
            String file = dialog.open();
            if (file != null && file.trim().length() > 1) {
               list.add(file);
               updatePageStatus();
            }
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }
      });
      final Button remove = new Button(buttonBar, SWT.PUSH);
      remove.setText("&Remove");
      list.addSelectionListener(new SelectionListener() {
         public void widgetSelected(SelectionEvent e) {
            remove.setEnabled(list.getSelectionCount() > 0);
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }
      });
      remove.addSelectionListener(new SelectionListener() {
         public void widgetSelected(SelectionEvent e) {
            list.remove(list.getSelectionIndices());
            updatePageStatus();
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }
      });
      remove.setEnabled(false);

      setControl(page);
   }

   public String[] getECoreFileNames() {
      return list == null ? null : list.getItems();
   }

   public boolean isPageComplete() {
      return list != null && list.getItemCount() > 0;
   }

   private void updatePageStatus() {
      setPageComplete(list.getItemCount() > 0);
   }

   private class CreationDialog extends Dialog {
      private ResourceAndContainerGroup fileChooser;

      protected CreationDialog() {
         super(ODMSelectTypesPage.this.getShell());
      }

      protected void configureShell(Shell newShell) {
         super.configureShell(newShell);
         if (newShell != null) newShell.setText("Create File");
      }

      protected Control createButtonBar(Composite parent) {
         Control bar = super.createButtonBar(parent);
         getButton(OK).setEnabled(false);
         return bar;
      }

      protected Control createDialogArea(Composite parent) {
         Composite panel = (Composite) super.createDialogArea(parent);
         fileChooser = new ResourceAndContainerGroup(panel, new Listener() {
            public void handleEvent(Event event) {
               getButton(OK).setEnabled(fileChooser.areAllValuesValid());
            }
         }, "&Ecore Filename: ", "file", false);
         return panel;
      }

      protected void okPressed() {
         String filename = fileChooser.getResource();
         if (!filename.endsWith(".ecore")) filename = filename.concat(".ecore");
         list.add(fileChooser.getContainerFullPath().toString() + '/' + filename);
         updatePageStatus();
         super.okPressed();
      }
   }

}
