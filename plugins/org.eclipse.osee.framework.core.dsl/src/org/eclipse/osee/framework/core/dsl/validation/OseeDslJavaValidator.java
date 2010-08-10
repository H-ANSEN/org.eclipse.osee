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
package org.eclipse.osee.framework.core.dsl.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AccessContext;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ArtifactInstanceRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ArtifactTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AttributeTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.HierarchyRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ObjectRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslPackage;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactRef;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.util.OseeDslSwitch;
import org.eclipse.xtext.validation.Check;

/**
 * @author Roberto E. Escobar
 */
public class OseeDslJavaValidator extends AbstractOseeDslJavaValidator {

   public static final String NON_UNIQUE_HIERARCHY = "non_unique_hierarchy";
   public static final String NON_UNIQUE_ARTIFACT_INSTANCE_RESTRICTION = "non_unique_artifact_instance_restriction";
   public static final String NON_UNIQUE_ARTIFACT_TYPE_RESTRICTION = "non_unique_artifact_type_restriction";
   public static final String NON_UNIQUE_ATTRIBUTE_TYPE_RESTRICTION = "non_unique_attribute_type_restriction";
   public static final String NON_UNIQUE_RELATION_TYPE_RESTRICTION = "non_unique_relation_type_restriction";

   @Check
   public void checkAccessContextRulesUnique(AccessContext accessContext) {
      checkObjectRestrictions(accessContext, accessContext.getAccessRules());
      checkHierarchyUnique(accessContext, accessContext.getHierarchyRestrictions());
   }

   private void checkHierarchyUnique(AccessContext accessContext, Collection<HierarchyRestriction> hierarchy) {
      Map<String, XArtifactRef> references = new HashMap<String, XArtifactRef>();
      for (HierarchyRestriction restriction : hierarchy) {
         XArtifactRef artifactRef = restriction.getArtifact();
         String guid = artifactRef.getGuid();
         XArtifactRef reference = references.get(guid);
         if (reference == null) {
            references.put(guid, artifactRef);
         } else {
            String message =
               String.format("Duplicate hierarchy restriction [%s] in context[%s]", reference.toString(),
                  accessContext.getName());
            error(message, restriction, OseeDslPackage.ACCESS_CONTEXT__HIERARCHY_RESTRICTIONS, NON_UNIQUE_HIERARCHY,
               reference.getGuid());
         }
         checkObjectRestrictions(accessContext, restriction.getAccessRules());
      }
   }

   private void checkObjectRestrictions(AccessContext accessContext, Collection<ObjectRestriction> restrictions) {
      CheckSwitch restrictionChecker = new CheckSwitch(accessContext);
      for (ObjectRestriction restriction : restrictions) {
         restrictionChecker.doSwitch(restriction);
      }
   }

   private final class CheckSwitch extends OseeDslSwitch<Object> {
      private final Map<String, XArtifactRef> artInstanceRestrictions = new HashMap<String, XArtifactRef>();
      private final Map<String, XArtifactType> artifactTypeRestrictions = new HashMap<String, XArtifactType>();
      private final Map<String, XRelationType> relationTypeRetrictions = new HashMap<String, XRelationType>();
      private final Collection<AttributeTypeRestriction> attrTypeRetrictions = new HashSet<AttributeTypeRestriction>();

      private final AccessContext accessContext;

      public CheckSwitch(AccessContext accessContext) {
         this.accessContext = accessContext;
      }

      @Override
      public Object caseArtifactInstanceRestriction(ArtifactInstanceRestriction restriction) {
         String guid = restriction.getArtifactRef().getGuid();
         XArtifactRef reference = artInstanceRestrictions.get(guid);
         if (reference == null) {
            artInstanceRestrictions.put(guid, restriction.getArtifactRef());
         } else {
            String message =
               String.format("Duplicate artifact instance restriction [%s] in context[%s]", reference.toString(),
                  accessContext.getName());
            error(message, restriction, OseeDslPackage.ACCESS_CONTEXT__ACCESS_RULES,
               NON_UNIQUE_ARTIFACT_INSTANCE_RESTRICTION, reference.getGuid());
         }
         return restriction;
      }

      @Override
      public Object caseArtifactTypeRestriction(ArtifactTypeRestriction restriction) {
         String guid = restriction.getArtifactTypeRef().getTypeGuid();
         XArtifactType reference = artifactTypeRestrictions.get(guid);
         if (reference == null) {
            artifactTypeRestrictions.put(guid, restriction.getArtifactTypeRef());
         } else {
            String message =
               String.format("Duplicate artifact type restriction [%s] in context[%s]", reference.toString(),
                  accessContext.getName());
            error(message, restriction, OseeDslPackage.ACCESS_CONTEXT__ACCESS_RULES,
               NON_UNIQUE_ARTIFACT_TYPE_RESTRICTION, reference.getTypeGuid());
         }
         return restriction;
      }

      @Override
      public Object caseAttributeTypeRestriction(AttributeTypeRestriction object) {
         XArtifactType artifactType = object.getArtifactTypeRef();
         String attrGuidToMatch = object.getAttributeTypeRef().getTypeGuid();

         for (AttributeTypeRestriction r1 : attrTypeRetrictions) {
            String storedGuid = r1.getAttributeTypeRef().getTypeGuid();
            if (attrGuidToMatch.equals(storedGuid)) {
               XArtifactType storedArtType = r1.getArtifactTypeRef();
               boolean dispatchError = false;
               if (storedArtType != null && artifactType != null) {
                  dispatchError = storedArtType.getTypeGuid().equals(artifactType.getTypeGuid());
               } else if (storedArtType == null && artifactType == null) {
                  dispatchError = true;
               }

               if (dispatchError) {
                  String message =
                     String.format("Duplicate attribute type restriction [%s] in context[%s]", object.toString(),
                        accessContext.getName());
                  error(message, object, OseeDslPackage.ACCESS_CONTEXT__ACCESS_RULES,
                     NON_UNIQUE_ATTRIBUTE_TYPE_RESTRICTION, r1.getAttributeTypeRef().getTypeGuid());
               }
            }
         }

         return object;
      }

      @Override
      public Object caseRelationTypeRestriction(RelationTypeRestriction restriction) {
         restriction.getRelationTypeRef();

         String guid = restriction.getRelationTypeRef().getTypeGuid();
         XRelationType reference = relationTypeRetrictions.get(guid);
         if (reference == null) {
            relationTypeRetrictions.put(guid, restriction.getRelationTypeRef());
         } else {
            String message =
               String.format("Duplicate artifact type restriction [%s] in context[%s]", reference.toString(),
                  accessContext.getName());
            error(message, restriction, OseeDslPackage.ACCESS_CONTEXT__ACCESS_RULES,
               NON_UNIQUE_RELATION_TYPE_RESTRICTION, reference.getTypeGuid());
         }
         return restriction;
      }

   }

}
