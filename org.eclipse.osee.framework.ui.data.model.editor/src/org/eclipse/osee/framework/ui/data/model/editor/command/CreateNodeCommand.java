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
package org.eclipse.osee.framework.ui.data.model.editor.command;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.osee.framework.ui.data.model.editor.model.DataType;
import org.eclipse.osee.framework.ui.data.model.editor.model.ODMDiagram;

/**
 * @author Roberto E. Escobar
 */
public class CreateNodeCommand extends Command {

   private int width = -1;
   private Point loc;
   private DataType node;
   private ODMDiagram diagram;

   public CreateNodeCommand(DataType newObject, ODMDiagram parent, Point location) {
      super("Create Node");
      node = newObject;
      diagram = parent;
      loc = location;
   }

   public CreateNodeCommand(DataType newObject, ODMDiagram parent, Point location, int width) {
      this(newObject, parent, location);
      this.width = width;
   }

   public boolean canExecute() {
      return node != null && diagram != null && loc != null && (width == -1 || width > 0);
   }

   public void execute() {
      //      if (node instanceof NamedElementView) {
      //         NamedElementView view = (NamedElementView) node;
      //         if (view.getENamedElement().getName() == null) {
      //            // Name the classifier or package if it doesn't have a name; it'll have a 
      //            // name if it's being dragged from the outline
      //            view.getENamedElement().setName("DefaultName" + (int) (Math.random() * 10000000));
      //         }
      //         if (view.getENamedElement() instanceof EClassifier) {
      //            // Give the classifier a default package if it doesn't belong to one
      //            if (((EClassifier) view.getENamedElement()).getEPackage() == null) {
      //               ((EPackage) diagram.getImports().get(0)).getEClassifiers().add((EClassifier) view.getENamedElement());
      //               //               ((EPackage) diagram.getImports().get(0)).getEClassifiers().add(view.getENamedElement());
      //               packageSet = true;
      //            }
      //         } else if (view.getENamedElement() instanceof EPackage) {
      //            if (!Utilities.importsPackage((EPackage) view.getENamedElement(), diagram)) {
      //               // If this is a new package, add it to an ecore file and to the diagram's
      //               // imports
      //               ((EPackage) diagram.getImports().get(0)).eResource().getContents().add(view.getENamedElement());
      //               diagram.getImports().add(view.getENamedElement());
      //               packageAdded = true;
      //            }
      //         }
      //      } else if (node instanceof StickyNote) ((StickyNote) node).setText("Comment");
      //      node.setLocation(loc);
      //      node.setWidth(width);
      //      node.setDiagram(diagram);
   }

   public void undo() {
      //      node.setDiagram(null);
      //      if (node instanceof NamedElementView) {
      //         NamedElementView view = (NamedElementView) node;
      //         if (packageSet)
      //            ((EClassifier) view.getENamedElement()).getEPackage().getEClassifiers().remove(view.getENamedElement());
      //         else if (packageAdded) {
      //            diagram.getImports().remove(view.getENamedElement());
      //            view.getENamedElement().eResource().getContents().remove(view.getENamedElement());
      //         }
      //      }
   }

}
