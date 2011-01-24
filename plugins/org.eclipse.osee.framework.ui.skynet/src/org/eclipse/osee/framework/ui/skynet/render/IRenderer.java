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
package org.eclipse.osee.framework.ui.skynet.render;

import java.util.Collection;
import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.render.compare.IComparator;
import org.eclipse.osee.framework.ui.skynet.render.word.AttributeElement;
import org.eclipse.osee.framework.ui.skynet.render.word.Producer;

/**
 * @author Jeff C. Phillips
 */
public interface IRenderer {

   public static final int SPECIALIZED_MATCH = 60;
   public static final int PRESENTATION_SUBTYPE_MATCH = 50;
   public static final int PRESENTATION_TYPE = 40;
   public static final int SUBTYPE_TYPE_MATCH = 30;
   public static final int ARTIFACT_TYPE_MATCH = 20;
   public static final int DEFAULT_MATCH = 10;
   public static final int NO_MATCH = -1;

   public static final String FILE_NAME_OPTION = "filename";
   public static final String FILE_PREFIX_OPTION = "filePrefix";
   public static final String NO_DISPLAY = "noDisplay";
   public static final String SKIP_DIALOGS = "skipDialogs";

   public static final String OPEN_IN_TABLE_EDITOR = "open.with.mass.artifact.editor";
   public static final String OPEN_IN_GRAPH = "open.with.sky.walker";
   public static final String OPEN_IN_HISTORY = "open.with.resource.history";
   public static final String OPEN_IN_EXPLORER = "open.with.artifact.explorer";

   public static enum CommandGroup {
      PREVIEW(PresentationType.PREVIEW),
      EDIT(PresentationType.SPECIALIZED_EDIT),
      SHOW(PresentationType.SPECIALIZED_EDIT);

      PresentationType presentationType;

      CommandGroup(PresentationType type) {
         this.presentationType = type;
      }

      public PresentationType getPresentationType() {
         return presentationType;
      }

      public boolean isEdit() {
         return CommandGroup.EDIT == this;
      }

      public boolean isPreview() {
         return CommandGroup.PREVIEW == this;
      }

      public boolean isShowIn() {
         return CommandGroup.SHOW == this;
      }
   }

   public List<String> getCommandIds(CommandGroup commandGroup);

   public ImageDescriptor getCommandImageDescriptor(Command command, Artifact artifact) throws OseeCoreException;

   public void renderAttribute(IAttributeType attributeType, Artifact artifact, PresentationType presentationType, Producer producer, VariableMap map, AttributeElement attributeElement) throws OseeCoreException;

   public int minimumRanking() throws OseeCoreException;

   public void open(List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException;

   public int getApplicabilityRating(PresentationType presentationType, Artifact artifact) throws OseeCoreException;

   public String getName();

   public void setOptions(VariableMap options) throws OseeArgumentException;

   public String getStringOption(String key) throws OseeArgumentException;

   public boolean getBooleanOption(String key) throws OseeArgumentException;

   public VariableMap getOptions();

   public IRenderer newInstance() throws OseeCoreException;

   public IComparator getComparator();

   public List<IAttributeType> getOrderedAttributeTypes(Artifact artifact, Collection<IAttributeType> attributeTypes);
}
