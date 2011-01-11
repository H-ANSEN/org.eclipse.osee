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
package org.eclipse.osee.framework.ui.skynet.widgets;

import java.util.Collection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.ArtifactLabelProvider;

/**
 * Set a Artifact list
 * 
 * @author Donald G. Dunne
 */
public class XArtifactList extends XListViewer {

   public XArtifactList(String displayLabel) {
      super(displayLabel);
      setLabelProvider(new ArtifactLabelProvider());
      setContentProvider(new ArrayContentProvider());
   }

   public Collection<Artifact> getSelectedArtifacts() {
      return Collections.castMatching(Artifact.class, getSelected());
   }
}