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
package org.eclipse.osee.framework.skynet.core.artifact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.model.type.ArtifactType;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.IdJoinQuery;
import org.eclipse.osee.framework.database.core.JoinUtility;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.model.EventChangeTypeBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeManager;

/**
 * Changes the descriptor type of an artifact to the provided descriptor.
 * 
 * @author Jeff C. Phillips
 */
public class ChangeArtifactType {
   private static List<Attribute<?>> attributesToPurge;
   private static List<RelationLink> relationsToDelete;
   private static final IStatus promptStatus = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 256, "", null);

   /**
    * Changes the descriptor of the artifacts to the provided artifact descriptor
    */
   public static void changeArtifactType(Collection<? extends Artifact> artifacts, IArtifactType artifactTypeToken) throws OseeCoreException {
      if (artifacts.isEmpty()) {
         throw new OseeArgumentException("The artifact list can not be empty");
      }

      ArtifactType artifactType = ArtifactTypeManager.getType(artifactTypeToken);
      List<Artifact> artifactsUserAccepted = new ArrayList<Artifact>();
      Set<EventBasicGuidArtifact> artifactChanges = new HashSet<EventBasicGuidArtifact>();
      for (Artifact artifact : artifacts) {
         processAttributes(artifact, artifactType);
         processRelations(artifact, artifactType);
         artifactsUserAccepted.add(artifact);
         if (doesUserAcceptArtifactChange(artifact, artifactType)) {
            ArtifactType originalType = artifact.getArtifactType();
            boolean success = changeArtifactTypeThroughHistory(artifact, artifactType);
            if (success) {
               artifactChanges.add(new EventChangeTypeBasicGuidArtifact(artifact.getBranch().getGuid(),
                  originalType.getGuid(), artifactType.getGuid(), artifact.getGuid()));
            }
         }
      }

      // Kick Local and Remote Events
      ArtifactEvent artifactEvent = new ArtifactEvent(artifacts.iterator().next().getBranch());
      for (EventBasicGuidArtifact guidArt : artifactChanges) {
         artifactEvent.getArtifacts().add(guidArt);
      }
      OseeEventManager.kickPersistEvent(ChangeArtifactType.class, artifactEvent);

   }

   public static void handleRemoteChangeType(EventChangeTypeBasicGuidArtifact guidArt) {
      try {
         Artifact artifact = ArtifactCache.getActive(guidArt);
         if (artifact == null) {
            return;
         }
         ArtifactCache.deCache(artifact);
         RelationManager.deCache(artifact);
         artifact.setArtifactType(ArtifactTypeManager.getType(guidArt));
         artifact.clearEditState();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, "Error handling remote change type", ex);
      }
   }

   public static void changeArtifactTypeReportOnly(StringBuffer results, Collection<Artifact> artifacts, IArtifactType artifactType) throws OseeCoreException {
      if (artifacts.isEmpty()) {
         throw new OseeArgumentException("The artifact list can not be empty");
      }

      for (Artifact artifact : artifacts) {
         processAttributes(artifact, artifactType);
         processRelations(artifact, artifactType);

         if (!relationsToDelete.isEmpty() || !attributesToPurge.isEmpty()) {
            getConflictString(results, artifact, artifactType);
         }
      }
   }

   private static void getConflictString(StringBuffer results, Artifact artifact, IArtifactType artifactType) {
      results.append("There has been a conflict in changing artifact " + artifact.getGuid() + " - \"" + artifact.getName() + "\"" +
      //
      " to \"" + artifactType.getName() + "\" type. \n" + "The following data will need to be purged ");
      for (RelationLink relationLink : relationsToDelete) {
         results.append("([Relation][" + relationLink + "])");
      }
      for (Attribute<?> attribute : attributesToPurge) {
         results.append("([Attribute][" + attribute.getAttributeType().getName() + "][" + attribute.toString() + "])");
      }
      results.append("\n\n");
   }

   /**
    * Splits the attributes of the current artifact into two groups. The attributes that are compatible for the new type
    * and the attributes that will need to be purged.
    */
   private static void processAttributes(Artifact artifact, IArtifactType artifactType) throws OseeCoreException {
      attributesToPurge = new LinkedList<Attribute<?>>();

      for (IAttributeType attributeType : artifact.getAttributeTypes()) {
         ArtifactType aType = ArtifactTypeManager.getType(artifactType);
         if (!aType.isValidAttributeType(attributeType, artifact.getBranch())) {
            attributesToPurge.addAll(artifact.getAttributes(attributeType));
         }
      }
   }

   /**
    * Splits the relationLinks of the current artifact into Two groups. The links that are compatible for the new type
    * and the links that will need to be purged.
    */
   private static void processRelations(Artifact artifact, IArtifactType artifactType) throws OseeCoreException {
      relationsToDelete = new LinkedList<RelationLink>();

      for (RelationLink link : artifact.getRelationsAll(DeletionFlag.EXCLUDE_DELETED)) {
         if (RelationTypeManager.getRelationSideMax(link.getRelationType(), artifactType, link.getSide(artifact)) == 0) {
            relationsToDelete.add(link);
         }
      }
   }

   /**
    * @return true if the user accepts the purging of the attributes and relations that are not compatible for the new
    * artifact type else false.
    */
   private static boolean doesUserAcceptArtifactChange(final Artifact artifact, final ArtifactType artifactType) {
      if (!relationsToDelete.isEmpty() || !attributesToPurge.isEmpty()) {

         StringBuffer sb = new StringBuffer(50);
         getConflictString(sb, artifact, artifactType);
         try {
            return (Boolean) DebugPlugin.getDefault().getStatusHandler(promptStatus).handleStatus(promptStatus,
               sb.toString());
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            return false;
         }
      } else {
         return true;
      }
   }

   /**
    * Sets the artifact descriptor.
    */
   private static boolean changeArtifactTypeThroughHistory(Artifact artifact, ArtifactType newArtifactType) throws OseeCoreException {
      for (Attribute<?> attribute : attributesToPurge) {
         attribute.purge();
      }
      purgeAttributes();

      for (RelationLink relation : relationsToDelete) {
         relation.delete(true);
      }
      ArtifactCache.deCache(artifact);
      RelationManager.deCache(artifact);

      ArtifactType originalType = artifact.getArtifactType();
      artifact.setArtifactType(newArtifactType);
      try {
         ConnectionHandler.runPreparedUpdate("UPDATE osee_artifact SET art_type_id = ? WHERE art_id = ?",
            newArtifactType.getId(), artifact.getArtId());
      } catch (OseeDataStoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         artifact.setArtifactType(originalType);
         return false;
      } finally {
         artifact.clearEditState();
      }
      return true;
   }

   private static void purgeAttributes() throws OseeCoreException {
      IdJoinQuery txsJoin = populateTxsJoinTable();

      try {
         String delete_txs =
            "DELETE FROM osee_txs txs1 WHERE EXISTS (select 1 from osee_join_id jt1 WHERE jt1.query_id = ? AND jt1.id = txs1.gamma_id)";
         String delete_attr =
            "DELETE FROM osee_attribute attr1 WHERE EXISTS (select 1 from osee_join_id jt1 WHERE jt1.query_id = ? AND jt1.id = attr1.gamma_id)";
         ConnectionHandler.runPreparedUpdate(delete_txs, txsJoin.getQueryId());
         ConnectionHandler.runPreparedUpdate(delete_attr, txsJoin.getQueryId());
      } finally {
         txsJoin.delete();
      }
   }

   private static IdJoinQuery populateTxsJoinTable() throws OseeDataStoreException, OseeCoreException {
      IdJoinQuery attributeJoin = JoinUtility.createIdJoinQuery();

      for (Attribute<?> attribute : attributesToPurge) {
         attributeJoin.add(attribute.getId());
      }

      IdJoinQuery txsJoin = JoinUtility.createIdJoinQuery();
      try {
         attributeJoin.store();
         IOseeStatement chStmt = ConnectionHandler.getStatement();
         String selectAttrGammas =
            "select gamma_id from osee_attribute t1, osee_join_id t2 where t1.attr_id = t2.id and t2.query_id = ?";

         try {
            chStmt.runPreparedQuery(10000, selectAttrGammas, attributeJoin.getQueryId());
            while (chStmt.next()) {
               txsJoin.add(chStmt.getInt("gamma_id"));
            }
            txsJoin.store();
         } finally {
            chStmt.close();
         }
      } finally {
         attributeJoin.delete();
      }
      return txsJoin;
   }
}
