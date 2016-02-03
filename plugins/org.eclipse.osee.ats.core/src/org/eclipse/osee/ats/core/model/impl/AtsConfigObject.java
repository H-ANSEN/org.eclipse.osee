/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.logger.Log;

/**
 * @author Donald G Dunne
 */
public abstract class AtsConfigObject extends org.eclipse.osee.ats.core.model.impl.AtsObject implements IAtsConfigObject {
   protected final ArtifactId artifact;
   protected final Log logger;
   protected final IAtsServices atsServices;

   public AtsConfigObject(Log logger, IAtsServices atsServices, ArtifactId artifact) {
      super(artifact.getName(), artifact.getUuid());
      this.logger = logger;
      this.atsServices = atsServices;
      this.artifact = artifact;
      setStoreObject(artifact);
   }

   public Log getLogger() {
      return logger;
   }

   public IAtsServices getAtsServices() {
      return atsServices;
   }

   public void setFullName(String fullName) {
      throw new UnsupportedOperationException("TeamDefinition.setFullName not implemented yet");
   }

   public abstract String getTypeName();

   public String getFullName() {
      return getTypeName();
   }

   public void setActionable(boolean actionable) {
      throw new UnsupportedOperationException("TeamDefinition.setActionable not implemented yet");
   }

   public boolean isActionable() {
      return getAttributeValue(AtsAttributeTypes.Actionable, false);
   }

   @SuppressWarnings("unchecked")
   protected <T> T getAttributeValue(IAttributeType attributeType, Object defaultValue) {
      T value = null;
      try {
         value = (T) atsServices.getAttributeResolver().getSoleAttributeValue(artifact, attributeType, defaultValue);
      } catch (OseeCoreException ex) {
         logger.error(ex, "Error getting attribute value for - attributeType[%s]", attributeType);
      }
      return value;
   }

   public void setActive(boolean active) {
      throw new UnsupportedOperationException("TeamDefinition.setActive not implemented yet");
   }

   @Override
   public boolean isActive() {
      return getAttributeValue(AtsAttributeTypes.Active, false);
   }

   public Collection<String> getStaticIds() {
      Collection<String> results = Collections.emptyList();
      try {
         results = atsServices.getAttributeResolver().getAttributeValues(artifact, CoreAttributeTypes.StaticId);
      } catch (OseeCoreException ex) {
         logger.error(ex, "Error getting static Ids");
      }
      return results;
   }

   public Collection<IAtsUser> getLeads() {
      return getRelatedUsers(AtsRelationTypes.TeamLead_Lead);
   }

   public Collection<IAtsUser> getSubscribed() {
      return getRelatedUsers(AtsRelationTypes.SubscribedUser_User);
   }

   protected Collection<IAtsUser> getRelatedUsers(IRelationTypeSide relation) {
      Set<IAtsUser> results = new HashSet<>();
      try {
         for (Object userArt : atsServices.getRelationResolver().getRelated(artifact, relation)) {
            IAtsUser lead = atsServices.getUserService().getUserById(
               (String) atsServices.getAttributeResolver().getSoleAttributeValue((ArtifactId) userArt,
                  CoreAttributeTypes.UserId, null));
            results.add(lead);
         }
      } catch (OseeCoreException ex) {
         logger.error(ex, "Error getting related Users for relationTypeSide[%s]", relation);
      }
      return results;
   }

   @Override
   public ArtifactId getStoreObject() {
      return artifact != null ? artifact : super.getStoreObject();
   }

   @Override
   public Long getUuid() {
      return artifact.getUuid();
   }

   @Override
   public String getDescription() {
      return atsServices.getAttributeResolver().getSoleAttributeValue(artifact, AtsAttributeTypes.Description, "");
   }
}
