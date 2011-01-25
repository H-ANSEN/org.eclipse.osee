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

import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.DEFAULT_OPEN;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.GENERAL_REQUESTED;
import static org.eclipse.osee.framework.ui.skynet.render.PresentationType.PRODUCE_ATTRIBUTE;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.plugin.core.util.ExtensionDefinedObjects;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.change.ArtifactDelta;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.render.compare.IComparator;
import org.eclipse.osee.framework.ui.skynet.render.word.AttributeElement;
import org.eclipse.osee.framework.ui.skynet.render.word.Producer;

/**
 * @author Ryan D. Brooks
 */
public final class RendererManager {
   private static final List<IRenderer> renderers = new ArrayList<IRenderer>(20);
   private static boolean firstTimeThrough = true;

   private RendererManager() {
      // Utility Class
   }

   /**
    * @return Returns the intersection of renderers applicable for all of the artifacts
    */
   public static List<IRenderer> getCommonRenderers(Collection<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException {
      List<IRenderer> commonRenders = getApplicableRenderers(presentationType, artifacts.iterator().next());

      for (Artifact artifact : artifacts) {
         List<IRenderer> applicableRenders = getApplicableRenderers(presentationType, artifact);

         Iterator<?> commIterator = commonRenders.iterator();

         while (commIterator.hasNext()) {
            IRenderer commRenderer = (IRenderer) commIterator.next();
            boolean found = false;
            for (IRenderer appRenderer : applicableRenders) {
               if (appRenderer.getName().equals(commRenderer.getName())) {
                  found = true;
                  continue;
               }
            }

            if (!found) {
               commIterator.remove();
            }
         }
      }
      return commonRenders;
   }

   /**
    * Maps all renderers in the system to their applicable artifact types
    */
   private static synchronized void ensurePopulated() {
      if (firstTimeThrough) {
         firstTimeThrough = false;
         registerRendersFromExtensionPoints();
      }
   }

   private static void registerRendersFromExtensionPoints() {
      ExtensionDefinedObjects<IRenderer> contributions =
         new ExtensionDefinedObjects<IRenderer>(SkynetGuiPlugin.PLUGIN_ID + ".ArtifactRenderer", "Renderer",
            "classname");
      for (IRenderer renderer : contributions.getObjects()) {
         renderers.add(renderer);
      }
   }

   public static FileSystemRenderer getBestFileRenderer(PresentationType presentationType, Artifact artifact) throws OseeCoreException {
      return getBestFileRenderer(presentationType, artifact, null);
   }

   public static FileSystemRenderer getBestFileRenderer(PresentationType presentationType, Artifact artifact, VariableMap options) throws OseeCoreException {
      IRenderer bestRenderer = getBestRenderer(presentationType, artifact, options);
      if (bestRenderer instanceof FileSystemRenderer) {
         return (FileSystemRenderer) bestRenderer;
      }
      throw new OseeArgumentException("No FileRenderer found for [%s] of type [%s]", artifact,
         artifact.getArtifactType());
   }

   public static IRenderer getBestRenderer(PresentationType presentationType, Artifact artifact, VariableMap options) throws OseeCoreException {
      IRenderer bestRenderer = getBestRendererPrototype(presentationType, artifact).newInstance();
      bestRenderer.setOptions(options);
      return bestRenderer;
   }

   private static IRenderer getBestRendererPrototype(PresentationType presentationType, Artifact artifact) throws OseeCoreException {
      if (presentationType == DEFAULT_OPEN && UserManager.getBooleanSetting(UserManager.DOUBLE_CLICK_SETTING_KEY)) {
         presentationType = GENERAL_REQUESTED;
      }
      IRenderer bestRendererPrototype = null;
      int bestRating = IRenderer.NO_MATCH;
      ensurePopulated();
      for (IRenderer renderer : renderers) {
         int rating = renderer.getApplicabilityRating(presentationType, artifact);
         if (rating > bestRating) {
            bestRendererPrototype = renderer;
            bestRating = rating;
         }
      }
      if (bestRendererPrototype == null) {
         throw new OseeStateException("No renderer configured for %s of %s", presentationType, artifact);
      }

      return bestRendererPrototype;
   }

   public static void renderAttribute(IAttributeType attributeType, PresentationType presentationType, Artifact artifact, VariableMap options, Producer producer, AttributeElement attributeElement) throws OseeCoreException {
      getBestRenderer(PRODUCE_ATTRIBUTE, artifact, options).renderAttribute(attributeType, artifact, presentationType,
         producer, options, attributeElement);
   }

   public static Collection<IAttributeType> getAttributeTypeOrderList(Artifact artifact) throws OseeCoreException {
      return getBestRenderer(PresentationType.PRODUCE_ATTRIBUTE, artifact, null).getOrderedAttributeTypes(artifact,
         artifact.getAttributeTypes());
   }

   private static List<IRenderer> getApplicableRenderers(PresentationType presentationType, Artifact artifact) throws OseeCoreException {
      ArrayList<IRenderer> applicableRenderers = new ArrayList<IRenderer>();
      int minimumRank =
         Math.max(getBestRenderer(presentationType, artifact, null).minimumRanking(), IRenderer.DEFAULT_MATCH);

      for (IRenderer prototypeRenderer : renderers) {
         // Add Catch Exception Code --

         int rating = prototypeRenderer.getApplicabilityRating(presentationType, artifact);
         if (rating >= minimumRank) {
            IRenderer renderer = prototypeRenderer.newInstance();
            applicableRenderers.add(renderer);
         }
      }
      return applicableRenderers;
   }

   public static HashCollection<IRenderer, Artifact> createRenderMap(PresentationType presentationType, Collection<Artifact> artifacts, VariableMap options) throws OseeCoreException {
      HashCollection<IRenderer, Artifact> prototypeRendererArtifactMap =
         new HashCollection<IRenderer, Artifact>(false, LinkedList.class);
      for (Artifact artifact : artifacts) {
         prototypeRendererArtifactMap.put(getBestRendererPrototype(presentationType, artifact), artifact);
      }

      // now that the artifacts are grouped based on best renderer type, create instances of those renderer with the supplied options
      HashCollection<IRenderer, Artifact> rendererArtifactMap =
         new HashCollection<IRenderer, Artifact>(false, LinkedList.class);
      for (IRenderer prototypeRenderer : prototypeRendererArtifactMap.keySet()) {
         IRenderer renderer = prototypeRenderer.newInstance();
         renderer.setOptions(options);
         rendererArtifactMap.put(renderer, prototypeRendererArtifactMap.getValues(prototypeRenderer));
      }
      return rendererArtifactMap;
   }

   public static void openInJob(Artifact artifact, PresentationType presentationType) {
      openInJob(Collections.singletonList(artifact), null, presentationType);
   }

   public static void openInJob(Collection<Artifact> artifacts, PresentationType presentationType) {
      openInJob(artifacts, null, presentationType);
   }

   public static void openInJob(Collection<Artifact> artifacts, VariableMap options, PresentationType presentationType) {
      Operations.executeAsJob(new OpenUsingRenderer(artifacts, options, presentationType), true);
   }

   public static void open(Collection<Artifact> artifacts, PresentationType presentationType, VariableMap options, IProgressMonitor monitor) throws OseeCoreException {
      Operations.executeWorkAndCheckStatus(new OpenUsingRenderer(artifacts, options, presentationType), monitor);
   }

   public static void open(Collection<Artifact> artifacts, PresentationType presentationType) throws OseeCoreException {
      open(artifacts, presentationType, null, new NullProgressMonitor());
   }

   public static void open(Artifact artifact, PresentationType presentationType, VariableMap options) throws OseeCoreException {
      open(Collections.singletonList(artifact), presentationType, options, new NullProgressMonitor());
   }

   public static void open(Artifact artifact, final PresentationType presentationType, IProgressMonitor monitor) throws OseeCoreException {
      open(Collections.singletonList(artifact), presentationType, null, monitor);
   }

   public static void open(Artifact artifact, final PresentationType presentationType) throws OseeCoreException {
      open(Collections.singletonList(artifact), presentationType);
   }

   public static String merge(Artifact baseVersion, Artifact newerVersion, IFile baseFile, IFile newerFile, VariableMap options) throws OseeCoreException {
      IRenderer renderer = getBestRenderer(PresentationType.MERGE, baseVersion, options);
      IComparator comparator = renderer.getComparator();
      return comparator.compare(baseVersion, newerVersion, baseFile, newerFile, PresentationType.MERGE);
   }

   public static void diffInJob(ArtifactDelta artifactDelta) throws OseeCoreException {
      diffInJob(artifactDelta, new VariableMap(IRenderer.FILE_PREFIX_OPTION, "Diff_For"));
   }

   public static void diffInJob(ArtifactDelta artifactDelta, VariableMap options) {
      Operations.executeAsJob(new DiffUsingRenderer(artifactDelta, options), true);
   }

   public static String diff(ArtifactDelta artifactDelta, VariableMap options) {
      DiffUsingRenderer operation = new DiffUsingRenderer(artifactDelta, options);
      Operations.executeWork(operation);
      return operation.getDiffResultPath();
   }

   public static void diffInJob(Collection<ArtifactDelta> artifactDeltas) {
      diffInJob(artifactDeltas, null);
   }

   public static void diffInJob(Collection<ArtifactDelta> artifactDeltas, VariableMap options) {
      IOperation operation = new DiffUsingRenderer(artifactDeltas, options);
      Operations.executeAsJob(operation, true);
   }

   public static void diff(Collection<ArtifactDelta> artifactDeltas, VariableMap options) {
      IOperation operation = new DiffUsingRenderer(artifactDeltas, options);
      Operations.executeWork(operation);
   }
}