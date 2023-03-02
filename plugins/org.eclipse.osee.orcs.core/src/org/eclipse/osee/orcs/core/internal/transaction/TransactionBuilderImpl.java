/*********************************************************************
 * Copyright (c) 2013 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.orcs.core.internal.transaction;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.osee.framework.core.data.ApplicabilityId;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactReadable;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.data.AttributeId;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.data.BranchCategoryToken;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.framework.core.data.IUserGroupArtifactToken;
import org.eclipse.osee.framework.core.data.OrcsTypeJoin;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.data.TransactionToken;
import org.eclipse.osee.framework.core.data.Tuple2Type;
import org.eclipse.osee.framework.core.data.Tuple3Type;
import org.eclipse.osee.framework.core.data.Tuple4Type;
import org.eclipse.osee.framework.core.data.TupleTypeId;
import org.eclipse.osee.framework.core.data.UserId;
import org.eclipse.osee.framework.core.data.UserToken;
import org.eclipse.osee.framework.core.enums.CoreArtifactTokens;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreTupleTypes;
import org.eclipse.osee.framework.core.enums.CoreUserGroups;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.enums.RelationSorter;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.exception.OseeAccessDeniedException;
import org.eclipse.osee.framework.jdk.core.type.Id;
import org.eclipse.osee.framework.jdk.core.type.NamedId;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.orcs.KeyValueOps;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.OseeDb;
import org.eclipse.osee.orcs.core.ds.Attribute;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.search.TupleQuery;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;

/**
 * @author Roberto E. Escobar
 * @author Megumi Telles
 */
public class TransactionBuilderImpl implements TransactionBuilder {

   private final TxCallableFactory txFactory;
   private final TxDataManager txManager;
   private final TxData txData;
   private final QueryFactory queryFactory;
   private final OrcsApi orcsApi;
   private final KeyValueOps keyValueOps;
   private final TupleQuery tupleQuery;
   private boolean committed = false;

   public TransactionBuilderImpl(TxCallableFactory txFactory, TxDataManager dataManager, TxData txData, OrcsApi orcsApi, KeyValueOps keyValueOps) {
      this.txFactory = txFactory;
      this.txManager = dataManager;
      this.txData = txData;
      this.orcsApi = orcsApi;
      this.queryFactory = orcsApi.getQueryFactory();
      this.keyValueOps = keyValueOps;
      this.tupleQuery = orcsApi.getQueryFactory().tupleQuery();
   }

   private Artifact getForWrite(ArtifactId artifactId) {
      return txManager.getForWrite(txData, artifactId);
   }

   @Override
   public BranchId getBranch() {
      return txData.getBranch();
   }

   @Override
   public String getComment() {
      return txData.getComment();
   }

   @Override
   public void setComment(String comment) {
      validateBuilder();
      txManager.setComment(txData, comment);
   }

   @Override
   public UserId getAuthor() {
      return txData.getAuthor();
   }

   public void setAuthor(UserToken author) {
      validateBuilder();
      txManager.setAuthor(txData, author);
   }

   @Override
   public ArtifactToken createArtifact(ArtifactId parent, ArtifactToken token) {
      validateBuilder();
      ArtifactToken child = createArtifact(token);
      if (parent.isValid()) {
         addChild(parent, child);
      }
      return child;
   }

   @Override
   public ArtifactToken createArtifact(ArtifactId parent, ArtifactTypeToken artifactType, String name) {
      validateBuilder();
      ArtifactToken child = createArtifact(artifactType, name);
      if (parent.isValid()) {
         addChild(parent, child);
      }
      return child;
   }

   @Override
   public ArtifactToken createArtifact(ArtifactTypeToken artifactType, String name) {
      validateBuilder();
      return txManager.createArtifact(txData, artifactType, name, (String) null);
   }

   @Override
   public ArtifactToken createArtifact(ArtifactTypeToken artifactType, String name, ApplicabilityId appId) {
      validateBuilder();
      return txManager.createArtifact(txData, artifactType, name, (String) null, appId);
   }

   @Override
   public ArtifactToken createArtifact(ArtifactToken token) {
      validateBuilder();
      return txManager.createArtifact(txData, token.getArtifactType(), token.getName(), token);
   }

   @Override
   public ArtifactToken createArtifact(ArtifactTypeToken artifactType, String name, ArtifactId artifactId) {
      validateBuilder();
      return txManager.createArtifact(txData, artifactType, name, artifactId);
   }

   @Override
   public ArtifactToken createArtifact(ArtifactTypeToken artifactType, String name, ArtifactId artifactId, ApplicabilityId appId) {
      validateBuilder();
      return txManager.createArtifact(txData, artifactType, name, artifactId, appId);
   }

   @Override
   public ArtifactToken createArtifact(ArtifactTypeToken artifactType, String name, String guid) {
      validateBuilder();
      return txManager.createArtifact(txData, artifactType, name, guid);
   }

   @Override
   public ArtifactToken createArtifact(ArtifactTypeToken artifactType, String name, String guid, ApplicabilityId appId) {
      validateBuilder();
      return txManager.createArtifact(txData, artifactType, name, guid, appId);
   }

   @Override
   public List<ArtifactToken> createArtifacts(ArtifactTypeToken artifactType, ArtifactId parent, List<String> names) {
      validateBuilder();
      ResultSet<ArtifactReadable> results =
         queryFactory.fromBranch(getBranch()).andTypeEquals(artifactType).and(CoreAttributeTypes.Name,
            names).getResults();
      if (!results.isEmpty()) {
         throw new OseeCoreException("Found %s artifacts of type %s with duplicate names: %s", results.size(),
            artifactType, results.getList());
      }

      List<ArtifactToken> tokens = new ArrayList<>(names.size());

      ArtifactTypeToken artifactTypeToken = orcsApi.tokenService().getArtifactType(artifactType.getId());
      for (String name : names) {
         tokens.add(createArtifact(parent, artifactTypeToken, name));
      }
      return tokens;
   }

   @Override
   public ArtifactToken copyArtifact(ArtifactReadable sourceArtifact) {
      validateBuilder();
      return copyArtifact(sourceArtifact.getBranch(), sourceArtifact);
   }

   @Override
   public ArtifactToken copyArtifact(BranchId fromBranch, ArtifactId artifactId) {
      validateBuilder();
      return txManager.copyArtifact(txData, fromBranch, artifactId);
   }

   @Override
   public ArtifactToken copyArtifact(ArtifactReadable sourceArtifact, Collection<AttributeTypeToken> attributesToDuplicate) {
      for (AttributeTypeToken typeToken : attributesToDuplicate) {
         checkPermissionsForLoginId(typeToken);
      }
      validateBuilder();
      return copyArtifact(sourceArtifact.getBranch(), sourceArtifact, attributesToDuplicate);
   }

   @Override
   public ArtifactToken copyArtifact(BranchId fromBranch, ArtifactId artifactId, Collection<AttributeTypeToken> attributesToDuplicate) {
      for (AttributeTypeToken typeToken : attributesToDuplicate) {
         checkPermissionsForLoginId(typeToken);
      }
      validateBuilder();
      return txManager.copyArtifact(txData, fromBranch, artifactId, attributesToDuplicate);
   }

   @Override
   public ArtifactToken introduceArtifact(BranchId fromBranch, ArtifactId sourceArtifact) {
      validateBuilder();
      checkAreOnDifferentBranches(txData, fromBranch);
      ArtifactReadable source = getArtifactReadable(txData.getSession(), queryFactory, fromBranch, sourceArtifact);
      Conditions.assertNotSentinel(source, "Source Artifact");
      ArtifactReadable destination =
         getArtifactReadable(txData.getSession(), queryFactory, txData.getBranch(), sourceArtifact);
      return txManager.introduceArtifact(txData, fromBranch, source, destination);
   }

   @Override
   public void introduceTuple(TupleTypeId tupleType, GammaId tupleGamma) {
      validateBuilder();
      txData.add(txManager.introduceTuple(tupleType, tupleGamma));
   }

   @Override
   public ArtifactToken replaceWithVersion(ArtifactReadable sourceArtifact, ArtifactReadable destination) {
      validateBuilder();
      return txManager.replaceWithVersion(txData, sourceArtifact.getBranch(), sourceArtifact, destination);
   }

   @Override
   public AttributeId createAttribute(ArtifactId sourceArtifact, AttributeTypeToken attributeType) {
      checkPermissionsForLoginId(attributeType);
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      return asArtifact.createAttribute(attributeType);
   }

   @Override
   public <T> AttributeId createAttribute(ArtifactId sourceArtifact, AttributeTypeToken attributeType, T value) {
      checkPermissionsForLoginId(attributeType);
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      return asArtifact.createAttribute(attributeType, value);
   }

   @Override
   public <T> AttributeId createAttribute(ArtifactId sourceArtifact, AttributeTypeToken attributeType, UserToken user, T value) {
      checkPermissionsForLoginId(attributeType, user);
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      return asArtifact.createAttribute(attributeType, value);
   }

   @Override
   public <T> AttributeId createAttributeNoAccess(ArtifactId sourceArtifact, AttributeTypeToken attributeType, T value) {
      if (!OseeProperties.isInTest()) {
         throw new OseeArgumentException("createAttributeNoAccess can only be used in tests or bootstrapping");
      }
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      return asArtifact.createAttribute(attributeType, value);
   }

   @Override
   public <T> void setSoleAttributeValue(ArtifactId sourceArtifact, AttributeTypeToken attributeType, T value) {
      checkPermissionsForLoginId(attributeType);
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.setSoleAttributeValue(attributeType, value);
   }

   @Override
   public void setSoleAttributeFromStream(ArtifactId sourceArtifact, AttributeTypeToken attributeType, InputStream stream) {
      checkPermissionsForLoginId(attributeType);
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.setSoleAttributeFromStream(attributeType, stream);
   }

   @Override
   public void setSoleAttributeFromString(ArtifactId sourceArtifact, AttributeTypeToken attributeType, String value) {
      checkPermissionsForLoginId(attributeType);
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.setSoleAttributeFromString(attributeType, value);
   }

   @Override
   public void setName(ArtifactId sourceArtifact, String value) {
      validateBuilder();
      setSoleAttributeFromString(sourceArtifact, CoreAttributeTypes.Name, value);
   }

   @Override
   public <T> void setAttributesFromValues(ArtifactId sourceArtifact, AttributeTypeToken attributeType, Collection<T> values) {
      checkPermissionsForLoginId(attributeType);

      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.setAttributesFromValues(attributeType, values);
   }

   public void checkPermissionsForLoginId(AttributeTypeToken attributeType) {
      if (attributeType.equals(CoreAttributeTypes.LoginId)) {
         UserToken user = orcsApi.userService().getUser();
         Collection<IUserGroupArtifactToken> roles = user.getRoles();
         if (!roles.contains(CoreUserGroups.AccountAdmin)) {
            throw new OseeAccessDeniedException("User %s is not an account admin", user.toStringWithId());
         }
      }
   }

   public void checkPermissionsForLoginId(AttributeTypeToken attributeType, UserToken user) {
      if (attributeType.equals(CoreAttributeTypes.LoginId)) {
         Collection<IUserGroupArtifactToken> roles = user.getRoles();
         if (!roles.contains(CoreUserGroups.AccountAdmin)) {
            throw new OseeAccessDeniedException("User %s is not an account admin", user.toStringWithId());
         }
      }
   }

   @Override
   public void setAttributesFromStrings(ArtifactId sourceArtifact, AttributeTypeToken attributeType, String... values) {
      if (attributeType.getName().contains("Login Id")) {
         checkPermissionsForLoginId(attributeType);
      }
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.setAttributesFromStrings(attributeType, values);
   }

   @Override
   public void setAttributesFromStrings(ArtifactId sourceArtifact, AttributeTypeToken attributeType, Collection<String> values) {
      checkPermissionsForLoginId(attributeType);
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.setAttributesFromStrings(attributeType, values);
   }

   @Override
   public <T> void setAttributeById(ArtifactId sourceArtifact, AttributeId attrId, T value) {
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.getAttributeById(attrId).setValue(value);
   }

   @Override
   public void setAttributeById(ArtifactId sourceArtifact, AttributeId attrId, String value) {
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.getAttributeById(attrId).setFromString(value);
   }

   @Override
   public void setAttributeById(ArtifactId sourceArtifact, AttributeId attrId, InputStream stream) {
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.getAttributeById(attrId).setValueFromInputStream(stream);
   }

   @Override
   public void setAttributeApplicability(ArtifactId art, AttributeId attrId, ApplicabilityId applicId) {
      validateBuilder();
      Artifact asArtifact = getForWrite(art);
      Attribute<Object> attribute = asArtifact.getAttributeById(attrId);
      attribute.getOrcsData().setApplicabilityId(applicId);
   }

   @Override
   public void deleteByAttributeId(ArtifactId sourceArtifact, AttributeId attrId) {
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.getAttributeById(attrId).delete();
   }

   @Override
   public void deleteSoleAttribute(ArtifactId sourceArtifact, AttributeTypeToken attributeType) {
      checkPermissionsForLoginId(attributeType);
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.deleteSoleAttribute(attributeType);
   }

   @Override
   public void deleteAttributes(ArtifactId sourceArtifact, AttributeTypeToken attributeType) {
      checkPermissionsForLoginId(attributeType);
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.deleteAttributes(attributeType);
   }

   @Override
   public void deleteAttributesWithValue(ArtifactId sourceArtifact, AttributeTypeToken attributeType, Object value) {
      checkPermissionsForLoginId(attributeType);
      validateBuilder();
      Artifact asArtifact = getForWrite(sourceArtifact);
      asArtifact.deleteAttributesWithValue(attributeType, value);
   }

   @Override
   public void addChild(ArtifactId parent, ArtifactId child) {
      validateBuilder();
      txManager.addChild(txData, parent, child);
   }

   @Override
   public void relate(ArtifactId artA, RelationTypeToken relType, ArtifactId artB) {
      validateBuilder();
      if (relType.isNewRelationTable()) {
         relate(relType, artA, artB, ArtifactId.SENTINEL, "end", 0, 0);
      } else {
         txManager.relate(txData, artA, relType, artB);
      }
   }

   @Override
   public void relate(ArtifactId artA, RelationTypeToken relType, ArtifactId artB, String rationale) {
      validateBuilder();
      if (relType.isNewRelationTable()) {
         relate(relType, artA, artB, ArtifactId.SENTINEL, "end", 0, 0);
      } else {
         txManager.relate(txData, artA, relType, artB, rationale);
      }
   }

   @Override
   public void relate(ArtifactId artA, RelationTypeToken relType, ArtifactId artB, RelationSorter sortType) {
      validateBuilder();
      if (relType.isNewRelationTable()) {
         relate(relType, artA, artB, ArtifactId.SENTINEL, "end", 0, 0);
      } else {
         txManager.relate(txData, artA, relType, artB, sortType);
      }
   }

   @Override
   public void relate(ArtifactId artA, RelationTypeToken relType, ArtifactId artB, String rationale, RelationSorter sortType) {
      validateBuilder();
      if (relType.isNewRelationTable()) {
         relate(relType, artA, artB, ArtifactId.SENTINEL, "end", 0, 0);
      } else {
         txManager.relate(txData, artA, relType, artB, rationale, sortType);
      }
   }

   @Override
   public void relate(ArtifactId artA, RelationTypeToken relType, ArtifactId artB, ArtifactId relatedArtifact, String insertType, int afterIndex, int beforeIndex) {
      relate(relType, artA, artB, relatedArtifact, insertType, afterIndex, beforeIndex);
   }

   @Override
   public void relate(ArtifactId artA, RelationTypeToken relType, ArtifactId artB, ArtifactId relatedArtifact, String afterArtifact) {
      Integer afterIndex = 0;
      Integer beforeIndex = 0;
      String insertType = afterArtifact;
      ArtifactId afterArtifactId = ArtifactId.SENTINEL;
      try {
         Long afterId = Long.parseLong(afterArtifact);
         afterArtifactId = ArtifactId.valueOf(afterId);
         insertType = "insert";
      } catch (NumberFormatException e) {
         if (afterArtifact == null) {
            insertType = "end";
         }
      }
      if (afterArtifactId.isValid()) {
         RelationTypeSide rts = new RelationTypeSide(relType, RelationSide.SIDE_B);
         ArtifactReadable artifactA = orcsApi.getQueryFactory().fromBranch(getBranch()).andId(artA).asArtifact();
         List<ArtifactId> related = artifactA.getRelatedIds(rts);
         if (related.indexOf(afterArtifactId) + 1 > related.size() - 1) {
            insertType = "end";
         } else {
            ArtifactId beforeArtifact = related.get(related.indexOf(afterArtifactId) + 1);
            Integer selectAfterIndex = orcsApi.getJdbcService().getClient().fetch(0,
               "SELECT rel_order from osee_txs tx, osee_relation rel where tx.branch_id = ? and tx.tx_current = 1 and tx.gamma_id = rel.gamma_id and rel.a_art_id = ? and rel.rel_type = ? and rel.b_art_id = ?",
               getBranch(), artA, relType.getId(), afterArtifactId);
            Integer selectBeforeIndex = orcsApi.getJdbcService().getClient().fetch(0,
               "SELECT rel_order from osee_txs tx, osee_relation rel where tx.branch_id = ? and tx.tx_current = 1 and tx.gamma_id = rel.gamma_id and rel.a_art_id = ? and rel.rel_type = ? and rel.b_art_id = ?",
               getBranch(), artA, relType.getId(), beforeArtifact);
            afterIndex = selectAfterIndex != null ? selectAfterIndex : 0;
            beforeIndex = selectBeforeIndex != null ? selectBeforeIndex : 0;
         }
      }
      relate(relType, artA, artB, relatedArtifact, insertType, afterIndex, beforeIndex);

   }

   private void relate(RelationTypeToken relType, ArtifactId artA, ArtifactId artB, ArtifactId relationArt, String insertType, int afterIndex, int beforeIndex) {
      int minOrder = 0;
      int maxOrder = 0;
      int relOrder = 0;
      RelationTypeMultiplicity mult = relType.getMultiplicity();
      if (mult.equals(RelationTypeMultiplicity.MANY_TO_MANY) || mult.equals(RelationTypeMultiplicity.ONE_TO_MANY)) {

         if (txData.relationSideAExists(relType, artA)) {
            minOrder = txData.getNewRelations().get(relType, artA).getMinOrder();
            maxOrder = txData.getNewRelations().get(relType, artA).getMaxOrder();
         } else {
            String minMaxString = orcsApi.getJdbcService().getClient().fetch("0,0",
               "SELECT min(rel.rel_order) || ',' ||max(rel.rel_order) from osee_relation rel where rel.a_art_id = ? and rel.rel_type = ?",
               artA, relType.getId());
            if (minMaxString != null && minMaxString.length() > 3) {
               minOrder = Integer.parseInt(minMaxString.substring(0, minMaxString.indexOf(",") - 1));
               maxOrder = Integer.parseInt(minMaxString.substring(minMaxString.indexOf(",") + 1));
            }
            txData.addRelationSideA(relType, artA, minOrder, maxOrder);
         }

         if (insertType.equals("start")) {
            relOrder = txData.calculateHeadInsertionOrderIndex(minOrder);
            txData.getNewRelations().get(relType, artA).setMinOrder(relOrder);
         } else if (insertType.equals("insert")) {
            relOrder = txData.calculateInsertionOrderIndex(afterIndex, beforeIndex);
         } else {
            relOrder = txData.calculateEndInsertionOrderIndex(maxOrder);
            txData.getNewRelations().get(relType, artA).setMaxOrder(relOrder);

         }
      }
      txManager.relate(txData, artA, relType, artB, relationArt, relOrder, RelationSorter.USER_DEFINED);
   }

   @Override
   public void setRelations(ArtifactId artA, RelationTypeToken relType, Iterable<? extends ArtifactId> artBs) {
      validateBuilder();
      txManager.setRelations(txData, artA, relType, artBs);
   }

   @Override
   public void setRelationsAndOrder(ArtifactId artifact, RelationTypeSide relationSide, List<? extends ArtifactId> artifacts) {
      validateBuilder();
      txManager.setRelationsAndOrder(txData, artifact, relationSide, artifacts);
   }

   @Override
   public void setRationale(ArtifactId artA, RelationTypeToken relType, ArtifactId artB, String rationale) {
      validateBuilder();
      txManager.setRationale(txData, artA, relType, artB, rationale);
   }

   @Override
   public void unrelate(ArtifactId artA, RelationTypeToken relType, ArtifactId artB) {
      validateBuilder();
      txManager.unrelate(txData, artA, relType, artB);
   }

   @Override
   public void unrelateFromAll(RelationTypeSide typeAndSide, ArtifactId art) {
      validateBuilder();
      RelationTypeToken type = typeAndSide.getRelationType();
      txManager.unrelateFromAll(txData, type, art, typeAndSide.getSide());
   }

   @Override
   public void unrelateFromAll(ArtifactId artA) {
      validateBuilder();
      txManager.unrelateFromAll(txData, artA);
   }

   @Override
   public void setRelationApplicability(ArtifactId artA, RelationTypeToken relType, ArtifactId artB, ApplicabilityId applicId) {
      validateBuilder();
      txManager.setRelationApplicabilityId(txData, artA, relType, artB, applicId);
   }

   @Override
   public void deleteArtifact(ArtifactId sourceArtifact) {
      validateBuilder();
      txManager.deleteArtifact(txData, sourceArtifact);
   }

   @Override
   public boolean isCommitInProgress() {
      return txData.isCommitInProgress();
   }

   @Override
   public TransactionToken commit() {
      validateBuilder();
      try {
         TransactionToken txId = txFactory.createTx(txData).call();
         if (txId.isValid()) {
            committed = true;
            return txId;
         }
      } catch (Exception ex) {
         throw OseeCoreException.wrap(ex);
      }
      return TransactionToken.SENTINEL;
   }

   @Override
   public List<ArtifactReadable> getTxDataReadables() {
      List<ArtifactReadable> list = new ArrayList<ArtifactReadable>(txData.getAllReadables());
      return list;
   }

   private void checkAreOnDifferentBranches(TxData txData, BranchId sourceBranch) {
      boolean isOnSameBranch = txData.isOnBranch(sourceBranch);
      Conditions.checkExpressionFailOnTrue(isOnSameBranch, "Source branch is same branch as transaction branch[%s]",
         txData.getBranch());
   }

   protected ArtifactReadable getArtifactReadable(OrcsSession session, QueryFactory queryFactory, BranchId branch, ArtifactId id) {
      return queryFactory.fromBranch(branch).includeDeletedArtifacts().andId(id).getResults().getOneOrDefault(
         ArtifactReadable.SENTINEL);
   }

   @Override
   public void setApplicability(ArtifactId artId, ApplicabilityId applicId) {
      validateBuilder();
      txManager.setApplicabilityId(txData, artId, applicId);
   }

   @Override
   public void setApplicabilityReference(HashMap<ArtifactId, List<ApplicabilityId>> artifacts) {
      validateBuilder();
      TupleQuery tupleQuery = queryFactory.tupleQuery();

      for (Entry<? extends ArtifactId, List<ApplicabilityId>> entry : artifacts.entrySet()) {
         for (ApplicabilityId appId : entry.getValue()) {
            if (!tupleQuery.doesTuple2Exist(CoreTupleTypes.ArtifactReferenceApplicabilityType, entry.getKey(), appId)) {
               addTuple2(CoreTupleTypes.ArtifactReferenceApplicabilityType, entry.getKey(), appId);
            }
         }
      }
   }

   @Override
   public void setApplicability(ApplicabilityId applicId, List<? extends ArtifactId> artifacts) {
      validateBuilder();
      for (ArtifactId artifact : artifacts) {
         setApplicability(artifact, applicId);
      }
   }

   @Override
   public ArtifactToken createView(BranchId branch, String viewName) {
      validateBuilder();
      // Retrieve from transaction in case it has not be persisted yet
      ArtifactId plFolder = txData.getWriteable(CoreArtifactTokens.ProductsFolder);
      if (plFolder == null) {
         plFolder = CoreArtifactTokens.ProductsFolder;
      }
      ArtifactToken view = createArtifact(plFolder, CoreArtifactTypes.BranchView, viewName);
      addTuple2(CoreTupleTypes.ApplicabilityDefinition, view, "Config = " + viewName);
      addTuple2(CoreTupleTypes.ViewApplicability, view, ApplicabilityToken.BASE.getName());
      addTuple2(CoreTupleTypes.ViewApplicability, view, "Config = " + viewName);

      return view;
   }

   @Override
   public void createBranchCategory(BranchId branch, BranchCategoryToken category) {
      validateBuilder();
      txManager.createBranchCategory(txData, category);
   }

   @Override
   public boolean deleteBranchCategory(BranchId branch, BranchCategoryToken category) {
      validateBuilder();
      List<GammaId> categories = orcsApi.getQueryFactory().branchQuery().getBranchCategoryGammaId(branch, category);

      if (categories != null) {
         if (categories.isEmpty()) {
            return false;
         }
         for (GammaId gammaId : categories) {
            txData.deleteBranchCategory(gammaId);
         }
      }
      return true;

   }

   @Override
   public void createApplicabilityForView(ArtifactId viewId, String applicability) {
      validateBuilder();
      /**
       * If the view/applicability combo exists (b/c it was created on another branch), update current branch to
       * reference associated gamma_id
       */
      GammaId tupleGamma = tupleQuery.getTuple2GammaFromE1E2(CoreTupleTypes.ViewApplicability, viewId, applicability);
      if (tupleGamma.isValid()) {
         introduceTuple(CoreTupleTypes.ViewApplicability, tupleGamma);
      } else {
         addTuple2(CoreTupleTypes.ViewApplicability, viewId, applicability);
      }
   }

   private Long insertValue(String value) {
      return keyValueOps.putIfAbsent(value);
   }

   @Override
   public <E1, E2> GammaId addTuple2(Tuple2Type<E1, E2> tupleType, E1 e1, E2 e2) {
      validateBuilder();
      return txManager.createTuple2(txData, tupleType, toLong(e1), toLong(e2));
   }

   @Override
   public <J extends OrcsTypeJoin<J, T>, T extends NamedId> void addOrcsTypeJoin(J typeJoin) {
      validateBuilder();
      Tuple2Type<J, T> tupleType = typeJoin.getTupleType();
      for (T type : typeJoin.getTypes()) {
         addTuple2(tupleType, typeJoin, type);
      }
   }

   @Override
   public <E1, E2, E3> GammaId addTuple3(Tuple3Type<E1, E2, E3> tupleType, E1 e1, E2 e2, E3 e3) {
      validateBuilder();
      return txManager.createTuple3(txData, tupleType, toLong(e1), toLong(e2), toLong(e3));
   }

   @Override
   public <E1, E2, E3, E4> GammaId addTuple4(Tuple4Type<E1, E2, E3, E4> tupleType, E1 e1, E2 e2, E3 e3, E4 e4) {
      validateBuilder();
      return txManager.createTuple4(txData, tupleType, toLong(e1), toLong(e2), toLong(e3), toLong(e4));
   }

   private Long toLong(Object element) {
      if (element instanceof String) {
         return insertValue((String) element);
      } else if (element instanceof Id) {
         return ((Id) element).getId();
      } else if (element instanceof Enum<?>) {
         return Long.valueOf(((Enum<?>) element).ordinal());
      }
      return (Long) element;
   }

   @Override
   public void deleteTuple2(GammaId gammaId) {
      validateBuilder();
      txData.deleteTuple(OseeDb.TUPLE2, gammaId);
   }

   @Override
   public void deleteTuple3(GammaId gammaId) {
      validateBuilder();
      txData.deleteTuple(OseeDb.TUPLE3, gammaId);
   }

   @Override
   public void deleteTuple4(GammaId gammaId) {
      validateBuilder();
      txData.deleteTuple(OseeDb.TUPLE4, gammaId);
   }

   @Override
   public <E1, E2> boolean deleteTuple2(Tuple2Type<E1, E2> tupleType, E1 e1, E2 e2) {
      validateBuilder();
      List<GammaId> tuples = new ArrayList<>();
      tupleQuery.getTuple2GammaFromE1E2(tupleType, getBranch(), e1, e2, tuples::add);
      if (tuples.isEmpty()) {
         return false;
      }
      tuples.forEach(this::deleteTuple2);
      return true;

   }

   @Override
   public <E1, E2, E3> boolean deleteTuple3(Tuple3Type<E1, E2, E3> tupleType, E1 element1, E2 element2, E3 element3) {
      validateBuilder();
      return false;
   }

   @Override
   public <E1, E2, E3, E4> boolean deleteTuple4(Tuple4Type<E1, E2, E3, E4> tupleType, E1 element1, E2 element2, E3 element3, E4 element4) {
      validateBuilder();
      return false;
   }

   @Override
   public <E1, E2, E3, E4> boolean deleteTuple4ByE1E2(Tuple4Type<E1, E2, E3, E4> tupleType, E1 e1, E2 e2) {
      validateBuilder();
      List<GammaId> tuples = new ArrayList<>();
      tupleQuery.getTuple4GammaFromE1E2(tupleType, getBranch(), e1, e2, tuples::add);
      if (tuples.isEmpty()) {
         return false;
      }
      tuples.forEach(this::deleteTuple4);
      return true;
   }

   @Override
   public void addKeyValueOps(Long id, String name) {
      validateBuilder();
      keyValueOps.putByKey(id, name);
   }

   @Override
   public ArtifactToken getWriteable(ArtifactId artifact) {
      ArtifactToken art = txData.getWriteable(artifact);
      if (art == null) {
         art = ArtifactToken.SENTINEL;
      }
      return art;
   }

   private void validateBuilder() {
      if (committed) {
         throw new OseeStateException("Transaction has been committed and can not be re-used");
      }
   }
}