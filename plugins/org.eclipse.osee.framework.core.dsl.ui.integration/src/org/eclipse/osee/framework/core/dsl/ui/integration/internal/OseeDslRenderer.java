/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.dsl.ui.integration.internal;

import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.DEFAULT_OPEN;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERALIZED_EDIT;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.PRODUCE_ATTRIBUTE;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.SPECIALIZED_EDIT;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osee.framework.core.dsl.integration.util.OseeDslSegmentParser;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.render.DefaultArtifactRenderer;
import org.eclipse.osee.framework.ui.skynet.render.FileSystemRenderer;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * @author Roberto E. Escobar
 */
public final class OseeDslRenderer extends FileSystemRenderer {
   private static final String COMMAND_ID = "org.eclipse.osee.framework.core.dsl.OseeDsl.editor.command";
   private static final OseeDslSegmentParser parser = new OseeDslSegmentParser();

   @Override
   public String getName() {
      return "OseeDsl Editor";
   }

   @Override
   public DefaultArtifactRenderer newInstance() {
      return new OseeDslRenderer();
   }

   @Override
   public int getApplicabilityRating(PresentationType presentationType, Artifact artifact) throws OseeCoreException {
      if (!presentationType.matches(GENERALIZED_EDIT, PRODUCE_ATTRIBUTE) && !artifact.isHistorical()) {
         if (artifact.isOfType(CoreArtifactTypes.AccessControlModel)) {
            return ARTIFACT_TYPE_MATCH;
         }
      }
      return NO_MATCH;
   }

   @Override
   public List<String> getCommandIds(CommandGroup commandGroup) {
      ArrayList<String> commandIds = new ArrayList<String>(1);
      if (commandGroup.isEdit()) {
         commandIds.add(COMMAND_ID);
      }
      return commandIds;
   }

   @Override
   public boolean supportsCompare() {
      return true;
   }

   @SuppressWarnings("unused")
   @Override
   public void open(final List<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException {
      final PresentationType resultantpresentationType =
         presentationType == DEFAULT_OPEN ? SPECIALIZED_EDIT : presentationType;

      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (!artifacts.isEmpty()) {
               try {
                  IFile file = renderToFile(artifacts, resultantpresentationType);
                  if (file != null) {
                     IWorkbench workbench = PlatformUI.getWorkbench();
                     IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
                     IDE.openEditor(page, file);
                  }
               } catch (CoreException ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }
      });
   }

   @SuppressWarnings("unused")
   @Override
   public String getAssociatedExtension(Artifact artifact) throws OseeCoreException {
      return "osee";
   }

   @Override
   public InputStream getRenderInputStream(PresentationType presentationType, List<Artifact> artifacts) throws OseeCoreException {
      Artifact artifact = artifacts.iterator().next();
      StringBuilder builder = new StringBuilder();
      builder.append(parser.getStartTag(artifact));
      builder.append("\n");
      builder.append(artifact.getSoleAttributeValueAsString(CoreAttributeTypes.GeneralStringData, ""));
      builder.append("\n");
      builder.append(parser.getEndTag(artifact));
      InputStream inputStream = null;
      try {
         inputStream = new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
      } catch (UnsupportedEncodingException ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return inputStream;
   }

   @Override
   public Program getAssociatedProgram(Artifact artifact) throws OseeCoreException {
      throw new OseeCoreException("should not be called");
   }

   @Override
   protected IOperation getUpdateOperation(File file, List<Artifact> artifacts, Branch branch, PresentationType presentationType) {
      return new OseeDslArtifactUpdateOperation(parser, file);
   }
}
