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
package org.eclipse.osee.framework.ui.skynet.blam;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Donald G. Dunne
 */
public class BlamEditorInput implements IEditorInput {

   private final static String titleEnd = " BLAM";

   private final AbstractBlam blamOperation;

   public BlamEditorInput(AbstractBlam blamOperation) {
      this.blamOperation = blamOperation;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof BlamEditorInput) {
         return ((BlamEditorInput) obj).getBlamOperation().equals(getBlamOperation());
      }
      return false;
   }

   @Override
   public String getName() {
      return blamOperation.getName().toLowerCase().contains(titleEnd.toLowerCase().trim()) ? blamOperation.getName() : blamOperation.getName() + titleEnd;
   }

   public Image getImage() {
      return ImageManager.getImage(FrameworkImage.BLAM);
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.BLAM);
   }

   public AbstractBlam getBlamOperation() {
      return blamOperation;
   }

   @Override
   public boolean exists() {
      return false;
   }

   @Override
   public IPersistableElement getPersistable() {
      return null;
   }

   @Override
   public String getToolTipText() {
      return "";
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class arg0) {
      return null;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

}
