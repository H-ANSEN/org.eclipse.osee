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
package org.eclipse.osee.framework.core.dsl.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.BranchToken;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.dsl.integration.internal.Activator;
import org.eclipse.osee.framework.core.dsl.integration.util.OseeUtil;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AddEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDsl;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslFactory;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OverrideOption;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RemoveEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeTypeRef;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumEntry;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumOverride;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationType;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.OseeEnumEntry;
import org.eclipse.osee.framework.core.model.cache.AttributeTypeCache;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.core.model.type.OseeEnumType;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryService;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Ryan D. Brooks
 * @author Roberto E. Escobar
 */
public class XTextToOseeTypeOperation extends AbstractOperation {
   private final IOseeModelFactoryService provider;
   private final OseeDsl model;
   private final OseeTypeCache typeCache;
   private final BranchCache branchCache;

   public XTextToOseeTypeOperation(IOseeModelFactoryService provider, OseeTypeCache typeCache, BranchCache branchCache, OseeDsl model) {
      super("OSEE Text Model to OSEE", Activator.PLUGIN_ID);
      this.provider = provider;
      this.typeCache = typeCache;
      this.branchCache = branchCache;
      this.model = model;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      double workAmount = 1.0;

      int workTotal = model.getArtifactTypes().size();
      workTotal += model.getAttributeTypes().size();
      workTotal += model.getRelationTypes().size();
      workTotal += model.getEnumTypes().size();
      workTotal += model.getEnumOverrides().size();

      if (workTotal > 0) {
         double workPercentage = workAmount / workTotal;

         for (XArtifactType xArtifactType : model.getArtifactTypes()) {
            translateXArtifactType(xArtifactType);
            monitor.worked(calculateWork(workPercentage));
         }

         for (XOseeEnumOverride xEnumOverride : model.getEnumOverrides()) {
            translateXEnumOverride(xEnumOverride);
            monitor.worked(calculateWork(workPercentage));
         }

         for (XOseeEnumType xEnumType : model.getEnumTypes()) {
            translateXEnumType(xEnumType);
            monitor.worked(calculateWork(workPercentage));
         }

         for (XAttributeType xAttributeType : model.getAttributeTypes()) {
            translateXAttributeType(xAttributeType);
            monitor.worked(calculateWork(workPercentage));
         }

         for (XArtifactType xArtifactType : model.getArtifactTypes()) {
            handleXArtifactTypeCrossRef(xArtifactType);
            monitor.worked(calculateWork(workPercentage));
         }

         for (XRelationType xRelationType : model.getRelationTypes()) {
            translateXRelationType(xRelationType);
            monitor.worked(calculateWork(workPercentage));
         }
      }
   }

   private void handleXArtifactTypeCrossRef(XArtifactType xArtifactType) throws OseeCoreException {
      ArtifactType targetArtifactType = typeCache.getArtifactTypeCache().getByGuid(xArtifactType.getTypeGuid());
      translateSuperTypes(targetArtifactType, xArtifactType);
      Map<IOseeBranch, Collection<AttributeType>> validAttributesPerBranch = getOseeAttributes(xArtifactType);
      targetArtifactType.setAllAttributeTypes(validAttributesPerBranch);
   }

   private void translateSuperTypes(ArtifactType targetArtifactType, XArtifactType xArtifactType) throws OseeCoreException {
      Set<ArtifactType> oseeSuperTypes = new HashSet<ArtifactType>();
      for (XArtifactType xSuperType : xArtifactType.getSuperArtifactTypes()) {
         String superTypeName = Strings.unquote(xSuperType.getName());
         ArtifactType oseeSuperType = typeCache.getArtifactTypeCache().getUniqueByName(superTypeName);
         oseeSuperTypes.add(oseeSuperType);
      }

      if (!oseeSuperTypes.isEmpty()) {
         targetArtifactType.setSuperTypes(oseeSuperTypes);
      }
   }

   private Map<IOseeBranch, Collection<AttributeType>> getOseeAttributes(XArtifactType xArtifactType) throws OseeCoreException {
      Map<IOseeBranch, Collection<AttributeType>> validAttributes =
         new HashMap<IOseeBranch, Collection<AttributeType>>();
      for (XAttributeTypeRef xAttributeTypeRef : xArtifactType.getValidAttributeTypes()) {
         XAttributeType xAttributeType = xAttributeTypeRef.getValidAttributeType();
         IOseeBranch branch = getAttributeBranch(xAttributeTypeRef);
         AttributeType oseeAttributeType = typeCache.getAttributeTypeCache().getByGuid(xAttributeType.getTypeGuid());
         if (oseeAttributeType != null) {
            Collection<AttributeType> listOfAllowedAttributes = validAttributes.get(branch);
            if (listOfAllowedAttributes == null) {
               listOfAllowedAttributes = new HashSet<AttributeType>();
               validAttributes.put(branch, listOfAllowedAttributes);
            }
            listOfAllowedAttributes.add(oseeAttributeType);
         } else {
            System.out.println(String.format("Type was null for \"%s\"", xArtifactType.getName()));
         }
      }
      return validAttributes;
   }

   private IOseeBranch getAttributeBranch(XAttributeTypeRef xAttributeTypeRef) throws OseeCoreException {
      String branchGuid = xAttributeTypeRef.getBranchGuid();
      if (branchGuid == null) {
         return CoreBranches.SYSTEM_ROOT;
      } else {
         IOseeBranch branch = branchCache.getByGuid(branchGuid);
         if (branch == null) {
            branch = new BranchToken(branchGuid, branchGuid);
         }
         return branch;
      }
   }

   private void translateXArtifactType(XArtifactType xArtifactType) throws OseeCoreException {
      String artifactTypeName = Strings.unquote(xArtifactType.getName());

      IArtifactType oseeArtifactType =
         provider.getArtifactTypeFactory().createOrUpdate(typeCache.getArtifactTypeCache(),
            xArtifactType.getTypeGuid(), xArtifactType.isAbstract(), artifactTypeName);
      xArtifactType.setTypeGuid(oseeArtifactType.getGuid());
   }

   private void translateXEnumType(XOseeEnumType xEnumType) throws OseeCoreException {
      String enumTypeName = Strings.unquote(xEnumType.getName());

      OseeEnumType oseeEnumType =
         provider.getOseeEnumTypeFactory().createOrUpdate(typeCache.getEnumTypeCache(), xEnumType.getTypeGuid(),
            enumTypeName);

      int lastOrdinal = 0;
      List<OseeEnumEntry> oseeEnumEntries = new ArrayList<OseeEnumEntry>();
      for (XOseeEnumEntry xEnumEntry : xEnumType.getEnumEntries()) {
         String entryName = Strings.unquote(xEnumEntry.getName());
         String ordinal = xEnumEntry.getOrdinal();
         if (Strings.isValid(ordinal)) {
            lastOrdinal = Integer.parseInt(ordinal);
         }

         String entryGuid = xEnumEntry.getEntryGuid();
         oseeEnumEntries.add(provider.getOseeEnumTypeFactory().createEnumEntry(entryGuid, entryName, lastOrdinal));
         lastOrdinal++;
      }
      oseeEnumType.setEntries(oseeEnumEntries);
   }

   private void translateXEnumOverride(XOseeEnumOverride xEnumOverride) throws OseeCoreException {
      XOseeEnumType xEnumType = xEnumOverride.getOverridenEnumType();
      if (!xEnumOverride.isInheritAll()) {
         xEnumType.getEnumEntries().clear();
      }
      OseeDslFactory factory = OseeDslFactory.eINSTANCE;
      for (OverrideOption xOverrideOption : xEnumOverride.getOverrideOptions()) {
         if (xOverrideOption instanceof AddEnum) {
            String entryName = ((AddEnum) xOverrideOption).getEnumEntry();
            String entryGuid = ((AddEnum) xOverrideOption).getEntryGuid();
            XOseeEnumEntry xEnumEntry = factory.createXOseeEnumEntry();
            xEnumEntry.setName(entryName);
            xEnumEntry.setEntryGuid(entryGuid);
            xEnumType.getEnumEntries().add(xEnumEntry);
         } else if (xOverrideOption instanceof RemoveEnum) {
            XOseeEnumEntry enumEntry = ((RemoveEnum) xOverrideOption).getEnumEntry();
            xEnumType.getEnumEntries().remove(enumEntry);
         } else {
            throw new OseeStateException("Unsupported Override Operation");
         }
      }
   }

   private void translateXAttributeType(XAttributeType xAttributeType) throws OseeCoreException {
      int min = Integer.parseInt(xAttributeType.getMin());
      int max = Integer.MAX_VALUE;
      if (!xAttributeType.getMax().equals("unlimited")) {
         max = Integer.parseInt(xAttributeType.getMax());
      }
      XOseeEnumType xEnumType = xAttributeType.getEnumType();
      OseeEnumType oseeEnumType = null;
      if (xEnumType != null) {
         oseeEnumType = typeCache.getEnumTypeCache().getByGuid(xEnumType.getTypeGuid());
      }

      AttributeTypeCache cache = typeCache.getAttributeTypeCache();
      AttributeType oseeAttributeType = provider.getAttributeTypeFactory().createOrUpdate(cache, //
         xAttributeType.getTypeGuid(), //
         Strings.unquote(xAttributeType.getName()), //
         getQualifiedTypeName(xAttributeType.getBaseAttributeType()), //
         getQualifiedTypeName(xAttributeType.getDataProvider()), //
         xAttributeType.getFileExtension(), //
         xAttributeType.getDefaultValue(), //
         oseeEnumType, //
         min, //
         max, //
         xAttributeType.getDescription(), //
         xAttributeType.getTaggerId()//
      );
      xAttributeType.setTypeGuid(oseeAttributeType.getGuid());
   }

   private String getQualifiedTypeName(String id) {
      String value = id;
      if (!value.contains(".")) {
         value = "org.eclipse.osee.framework.skynet.core." + id;
      }
      return value;
   }

   private void translateXRelationType(XRelationType xRelationType) throws OseeCoreException {
      RelationTypeMultiplicity multiplicity =
         RelationTypeMultiplicity.getFromString(xRelationType.getMultiplicity().name());

      String sideATypeName = Strings.unquote(xRelationType.getSideAArtifactType().getName());
      String sideBTypeName = Strings.unquote(xRelationType.getSideBArtifactType().getName());

      IArtifactType sideAType = typeCache.getArtifactTypeCache().getUniqueByName(sideATypeName);
      IArtifactType sideBType = typeCache.getArtifactTypeCache().getUniqueByName(sideBTypeName);

      RelationType oseeRelationType =
         provider.getRelationTypeFactory().createOrUpdate(typeCache.getRelationTypeCache(), //
            xRelationType.getTypeGuid(), //
            Strings.unquote(xRelationType.getName()), //
            xRelationType.getSideAName(), //
            xRelationType.getSideBName(), //
            sideAType, //
            sideBType, //
            multiplicity, //
            OseeUtil.orderTypeNameToGuid(xRelationType.getDefaultOrderType()) //
         );

      xRelationType.setTypeGuid(oseeRelationType.getGuid());
   }

}
