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
package org.eclipse.osee.framework.ui.skynet;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @author Ryan D. Brooks
 */
public class AttributeMenuSelectionListener extends SelectionAdapter {
   private final AttributesComposite attrsComp;
   private final TableViewer tableViewer;
   private final IDirtiableEditor editor;

   public AttributeMenuSelectionListener(AttributesComposite attrsComp, TableViewer tableViewer, IDirtiableEditor editor) {
      this.attrsComp = attrsComp;
      this.tableViewer = tableViewer;
      this.editor = editor;
   }

   @Override
   public void widgetSelected(SelectionEvent ev) {
      AttributeType attributeType = (AttributeType) ((MenuItem) ev.getSource()).getData();
      try {
         attrsComp.getArtifact().addAttribute(attributeType);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

      tableViewer.refresh();
      attrsComp.layout();
      attrsComp.getParent().layout();
      editor.onDirtied();
      attrsComp.notifyModifyAttribuesListeners();
   }
}