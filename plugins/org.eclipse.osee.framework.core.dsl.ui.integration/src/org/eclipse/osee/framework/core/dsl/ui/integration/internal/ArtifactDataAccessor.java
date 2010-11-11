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

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.dsl.integration.ArtifactDataProvider;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.OseeSystemArtifacts;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public final class ArtifactDataAccessor implements ArtifactDataProvider {

   @Override
   public boolean isApplicable(Object object) {
      boolean result = false;
      try {
         result = asCastedObject(object) != null;
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return result;
   }

   @Override
   public ArtifactData asCastedObject(Object object) throws OseeCoreException {
      ArtifactData wrapper = null;
      if (object instanceof Artifact) {
         final Artifact artifact = (Artifact) object;
         wrapper = new ArtifactWrapper(artifact);
      } else if (object instanceof Branch) {
         Branch branch = (Branch) object;
         final Artifact artifact = OseeSystemArtifacts.getDefaultHierarchyRootArtifact(branch);
         wrapper = new ArtifactWrapper(artifact);
      }
      return wrapper;
   }

   private final class ArtifactWrapper implements ArtifactData {
      private final Artifact artifact;

      public ArtifactWrapper(Artifact artifact) {
         this.artifact = artifact;
      }

      @Override
      public String getGuid() {
         return artifact.getGuid();
      }

      @Override
      public ArtifactType getArtifactType() {
         return artifact.getArtifactType();
      }

      @Override
      public boolean isAttributeTypeValid(IAttributeType attributeType) throws OseeCoreException {
         return artifact.isAttributeTypeValid(attributeType);
      }

      @Override
      public Collection<RelationType> getValidRelationTypes() throws OseeCoreException {
         return artifact.getValidRelationTypes();
      }

      @Override
      public IBasicArtifact<?> getObject() {
         return artifact;
      }

      @Override
      public Collection<String> getHierarchy() {
         Collection<String> hierarchy = new HashSet<String>();
         try {
            Artifact artifactPtr = artifact;
            while (artifactPtr != null) {
               hierarchy.add(artifactPtr.getGuid());
               artifactPtr = artifactPtr.getParent();
            }
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
         return hierarchy;
      }
   }
}