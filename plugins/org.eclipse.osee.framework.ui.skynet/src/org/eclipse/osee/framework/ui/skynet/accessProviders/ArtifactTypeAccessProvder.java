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
package org.eclipse.osee.framework.ui.skynet.accessProviders;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.ui.skynet.artifact.IAccessPolicyHandlerService;

/**
 * @author Jeff C. Phillips
 */
public class ArtifactTypeAccessProvder {

   private final Collection<? extends IArtifactType> artifactTypes;
   private final Branch branch;
   private final IAccessPolicyHandlerService accessService;

   public ArtifactTypeAccessProvder(IAccessPolicyHandlerService accessService, Branch branch, Collection<? extends IArtifactType> artifactTypes) {
      super();
      this.artifactTypes = artifactTypes;
      this.branch = branch;
      this.accessService = accessService;
   }

   /**
    * @return Returns artifact types that a user has write permissions for on a specific branch
    */
   public Collection<? extends IArtifactType> getWritableTypes() throws OseeCoreException {
      Iterator<? extends IArtifactType> artTypeIterator = artifactTypes.iterator();

      // Remove ArtifactTypes that do not have write permission.
      while (artTypeIterator.hasNext()) {
         artTypeIterator.next();
         if (!accessService.hasArtifactTypePermission(branch, Collections.singleton(artTypeIterator.next()),
            PermissionEnum.WRITE, Level.WARNING).matched()) {
            artTypeIterator.remove();
         }
      }
      return artifactTypes;
   }
}
