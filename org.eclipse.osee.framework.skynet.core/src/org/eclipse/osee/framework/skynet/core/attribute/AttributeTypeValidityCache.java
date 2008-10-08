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
package org.eclipse.osee.framework.skynet.core.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Level;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.db.connection.exception.OseeTypeDoesNotExist;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactType;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;

/**
 * Caches the mapping of valid attribute types to artifact types for which they are valid
 * 
 * @see org.eclipse.osee.framework.skynet.core.artifact.ArtifactType
 * @author Ryan D. Brooks
 */
public class AttributeTypeValidityCache {
   private static final String attributeValiditySql = "SELECT art_type_id, attr_type_id FROM osee_valid_attributes";
   private final HashCollection<ArtifactType, AttributeType> artifactToAttributeMap;
   private final HashCollection<AttributeType, ArtifactType> attributeToartifactMap;

   public AttributeTypeValidityCache() {
      artifactToAttributeMap = new HashCollection<ArtifactType, AttributeType>(false, TreeSet.class);
      attributeToartifactMap = new HashCollection<AttributeType, ArtifactType>(false, TreeSet.class);
   }

   public Collection<AttributeType> getAttributeTypesFromArtifactType(ArtifactType artifactType, Branch branch) throws OseeDataStoreException, OseeTypeDoesNotExist {
      ensurePopulated();

      Collection<AttributeType> attributeTypes = artifactToAttributeMap.getValues(artifactType);
      if (attributeTypes == null) {
         throw new OseeTypeDoesNotExist(
               "There are no valid attribute types available for the artifact type \"" + artifactType + "\"");
      }
      //Partition attribute type id = 107, CSCI attribute type id = 695,
      int rootBranchId = branch.getRootBranch().getBranchId();
      if (rootBranchId == 2) {
         return removeAttributeType(attributeTypes, 695);
      } else if (rootBranchId == 221) {
         return removeAttributeType(attributeTypes, 107);
      }

      return attributeTypes;
   }

   private Collection<AttributeType> removeAttributeType(Collection<AttributeType> attributeTypes, int attributeTypeId) {
      for (AttributeType attributeType : attributeTypes) {
         if (attributeType.getAttrTypeId() == attributeTypeId) {
            attributeTypes = new ArrayList<AttributeType>(attributeTypes);
            attributeTypes.remove(attributeType);
            return attributeTypes;
         }
      }
      return attributeTypes;
   }

   private synchronized void ensurePopulated() throws OseeDataStoreException {
      if (artifactToAttributeMap.isEmpty()) {
         populateCache();
      }
   }

   private void populateCache() throws OseeDataStoreException {
      ConnectionHandlerStatement chStmt = null;
      try {
         chStmt = ConnectionHandler.runPreparedQuery(attributeValiditySql);
         while (chStmt.next()) {
            try {
               ArtifactType artifactType = ArtifactTypeManager.getType(chStmt.getInt("art_type_id"));
               AttributeType attributeType = AttributeTypeManager.getType(chStmt.getInt("attr_type_id"));

               artifactToAttributeMap.put(artifactType, attributeType);
               attributeToartifactMap.put(attributeType, artifactType);
            } catch (OseeTypeDoesNotExist ex) {
               OseeLog.log(SkynetActivator.class, Level.SEVERE, ex);
            }
         }
      } finally {
         ConnectionHandler.close(chStmt);
      }
   }

   public Collection<ArtifactType> getArtifactTypesFromAttributeType(AttributeType requestedAttributeType) throws OseeDataStoreException, OseeTypeDoesNotExist {
      ensurePopulated();

      Collection<ArtifactType> artifactTypes = attributeToartifactMap.getValues(requestedAttributeType);
      if (artifactTypes == null) {
         throw new OseeTypeDoesNotExist(
               "There are no valid artifact types available for the attribute type " + requestedAttributeType);
      }

      return artifactTypes;
   }

   public boolean isValid(ArtifactType artifactType, AttributeType attributeType) throws OseeDataStoreException {
      ensurePopulated();
      Collection<AttributeType> attributeTypes = artifactToAttributeMap.getValues(artifactType);
      if (attributeTypes != null) {
         for (AttributeType otherAttributeType : attributeTypes) {
            if (attributeType.equals(otherAttributeType)) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * @param attributeType
    * @param artifactType
    * @throws Exception
    */
   public void add(ArtifactType artifactType, AttributeType attributeType) throws OseeDataStoreException {
      ensurePopulated();
      artifactToAttributeMap.put(artifactType, attributeType);
      attributeToartifactMap.put(attributeType, artifactType);
   }
}