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

package org.eclipse.osee.framework.skynet.core.relation;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.osee.framework.db.connection.core.transaction.AbstractDbTxTemplate;
import org.eclipse.osee.framework.plugin.core.config.ConfigUtil;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.CacheArtifactModifiedEvent;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;

public class RelationLinkGroup {

   private final Logger logger = ConfigUtil.getConfigFactory().getLogger(RelationLinkGroup.class);
   // NOTE: The TAIL_ADD_GAP has the following chararcterstics:
   //
   // For a TAIL_ADD_GAP = (int)Math.pow(2,x)
   // The constant will supply 2^(31-x) linear adds before losing order (since normalization is not
   // implemented)
   // and will allow the interim gaps to be bisected x times before losing order (again ... since
   // normalization is not implemented)
   private static final int TAIL_ADD_GAP = (int) Math.pow(2, 22);

   private LinkManager linkManager;
   private RelationType descriptor;
   private boolean sideA;
   private TreeSet<RelationLink> groupSide;
   private SkynetEventManager eventManager = SkynetEventManager.getInstance();
   private static final RelationPersistenceManager relationPersistenceManager =
         RelationPersistenceManager.getInstance();

   protected RelationLinkGroup(LinkManager linkManager, RelationType descriptor, boolean sideA) {
      super();

      if (linkManager == null) throw new IllegalArgumentException("linkManager can not be null");

      if (descriptor == null) throw new IllegalArgumentException("descriptor can not be null");

      this.linkManager = linkManager;
      this.descriptor = descriptor;
      this.sideA = sideA;
      this.groupSide = new TreeSet<RelationLink>(new LinkOrderComparator(!sideA));
   }

   public void fixOrder() {
      TreeSet<RelationLink> newOrderedLinks = new TreeSet<RelationLink>(new LinkOrderComparator(!sideA));
      newOrderedLinks.addAll(groupSide);
      groupSide = newOrderedLinks;
   }

   /**
    * Get a list of all the relation links. This set is for read-only purpose and is not backed by the group, and
    * therefor modification to the returned set will not result in any change to the actual linking between artifacts.<br/><br/>
    * Since the supplied set is not backed by this group, a copy of the internal data is created upon each call to
    * <code>getGroupSide</code>, so calling code should make as few calls to this method as is reasonable by storing
    * a local reference to the returned set.
    * 
    * @return Returns the groupSide.
    */
   public Set<RelationLink> getGroupSide() {
      return groupSide;
   }

   /**
    * Relate a supplied artifact to the artifact which owns this group. The type of the relation link for this operation
    * will be the same as this group represents, and the supplied artifact will be placed on the corresponding side of
    * the link for this group.<br/><br/> If the supplied artifact is already within the group then no operation will
    * be performed.
    * 
    * @param artifact
    * @return <b>true</b> if a new link was created, <b>false</b> otherwise.
    * @throws SQLException
    */
   public boolean addArtifact(Artifact artifact) throws SQLException {
      return addArtifact(artifact, null, false);
   }

   /**
    * Relate a supplied artifact to the artifact which owns this group. The type of the relation link for this operation
    * will be the same as this group represents, and the supplied artifact will be placed on the corresponding side of
    * the link for this group.<br/><br/> If the supplied artifact is already within the group then no operation will
    * be performed.
    * 
    * @param artifact
    * @return <b>true</b> if a new link was created, <b>false</b> otherwise.
    */
   public boolean addArtifact(Artifact artifact, String rationale, boolean persist) throws SQLException {
      return addArtifact(artifact, rationale, persist, 0, 0, true);
   }

   private boolean addArtifact(Artifact artifact, String rationale, boolean persist, int aOrder, int bOrder, boolean putLast) throws SQLException {
      checkArtifact(artifact);

      // If a link already exists for this artifact, then return false
      for (RelationLink link : groupSide)
         if (artifact == ((sideA) ? link.getArtifactA() : link.getArtifactB())) return false;

      // Check that both artifacts are valid for the type of relation
      // linkManager.ensureLinkValidity(descriptor, sideA, artifact);

      Artifact owningArtifact = linkManager.getOwningArtifact();

      Artifact artA = (sideA) ? artifact : owningArtifact;
      Artifact artB = (sideA) ? owningArtifact : artifact;
      if (putLast) {
         aOrder = artA.getLinkManager().ensureRelationGroupExists(descriptor, false).getLastOrderValue();
         bOrder = artB.getLinkManager().ensureRelationGroupExists(descriptor, true).getLastOrderValue();
      }

      return true;
   }

   public Set<Artifact> getArtifacts() {
      Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
      Artifact artToAdd;

      for (RelationLink link : groupSide) {

         if (sideA) {
            artToAdd = (link.getArtifactA().isDeleted() ? null : link.getArtifactA());
         } else {
            artToAdd = (link.getArtifactB().isDeleted() ? null : link.getArtifactB());
         }
         if (artToAdd != null) artifacts.add(artToAdd);
      }

      return artifacts;
   }

   public RelationType getDescriptor() {
      return descriptor;
   }

   /**
    * @return Returns the sideA.
    */
   public boolean isSideA() {
      return sideA;
   }

   public RelationSide getSide() {
      return sideA ? RelationSide.SIDE_A : RelationSide.SIDE_B;
   }

   /**
    * @return Returns the linkManager.
    */
   public LinkManager getLinkManager() {
      return linkManager;
   }

   private int getLastOrderValue() {
      int orderValue;

      if (!groupSide.isEmpty()) {
         RelationLink lastLink = groupSide.last();
         orderValue = sideA ? lastLink.getBOrder() : lastLink.getAOrder();
      } else {
         // Leave considerable space to bisect prior to this order value
         orderValue = 0;
      }

      return split(orderValue, Integer.MAX_VALUE);
   }

   private int split(int val1, int val2) {
      // NOTE : values are casted up to longs so that addition and difference of large values does
      // not
      // result in values outside the representable domain.

      // Special handling of end splitting ...
      if (val2 == Integer.MAX_VALUE) {
         // Check that there is enough space to fit in another gap
         if ((long) Integer.MAX_VALUE - (long) val1 > TAIL_ADD_GAP)
            return val1 + TAIL_ADD_GAP;
         else
            return 1;
      }
      return (int) (((long) val1 + (long) val2) / 2);
   }

   /**
    * Transfers the dropLink around the targetLink location by changing the dropLink's order.
    * 
    * @param targetLink The destination link
    * @param dropLink The link to be transfered
    * @param isBeforeTarget If true the dropLink will be placed after the targetLink else it will be placed before.
    */
   public void moveLink(RelationLink targetLink, RelationLink dropLink, boolean isBeforeTarget) {
      RelationLink neighborTargetLink = null;
      int neighborTargetOrder;
      int splitValue;
      int boundaryValue;

      if (isBeforeTarget) {
         neighborTargetLink = getLinkBefore(targetLink);
         boundaryValue = Integer.MIN_VALUE;
      } else {
         neighborTargetLink = getLinkAfter(targetLink);
         boundaryValue = Integer.MAX_VALUE;
      }

      neighborTargetOrder = computeNeighborTargetOrder(neighborTargetLink, boundaryValue);

      if (neighborTargetOrder == Integer.MIN_VALUE) {
         splitValue = computeBoundarySplitValue(targetLink, false);
      } else if (neighborTargetOrder == Integer.MAX_VALUE) {
         splitValue = computeBoundarySplitValue(targetLink, true);
      } else {
         splitValue = computeSplitValue(targetLink, neighborTargetLink, boundaryValue);
      }

      if (splitValue == computeTargetOrder(targetLink) || splitValue == neighborTargetOrder) {

         redistributeLinks();
         splitValue = computeSplitValue(targetLink, neighborTargetLink, boundaryValue);
      }

      if (sideA) {
         dropLink.setBOrder(splitValue);
      } else {
         dropLink.setAOrder(splitValue);
      }
      fixOrder();

      eventManager.kick(new CacheArtifactModifiedEvent(getLinkManager().getOwningArtifact(),
            org.eclipse.osee.framework.skynet.core.artifact.ArtifactModifiedEvent.ModType.Changed, this));
   }

   /**
    * @param targetLink
    * @param neighborTargetLink
    * @param boundaryValue
    * @return Returns the split value
    */
   private int computeSplitValue(RelationLink targetLink, RelationLink neighborTargetLink, int boundaryValue) {
      return (int) (((long) computeTargetOrder(targetLink) + (long) computeNeighborTargetOrder(neighborTargetLink,
            boundaryValue)) / 2);
   }

   /**
    * @param targetLink
    * @return Returns the target link order value.
    */
   private int computeTargetOrder(RelationLink targetLink) {
      int targetOrder;

      if (sideA) {
         targetOrder = targetLink.getBOrder();
      } else {
         targetOrder = targetLink.getAOrder();
      }
      return targetOrder;
   }

   /**
    * @param neighborTargetLink
    * @param boundaryValue
    * @return Returns the order of the neighborTarget link.
    */
   private int computeNeighborTargetOrder(RelationLink neighborTargetLink, int boundaryValue) {
      int neighborTargetOrder;

      if (sideA) {
         neighborTargetOrder = neighborTargetLink != null ? neighborTargetLink.getBOrder() : boundaryValue;
      } else {
         neighborTargetOrder = neighborTargetLink != null ? neighborTargetLink.getAOrder() : boundaryValue;
      }
      return neighborTargetOrder;
   }

   /**
    * @param targetOrder
    * @param isMax
    * @return Returns the boundary split value.
    */
   private int computeBoundarySplitValue(RelationLink targetLink, boolean isMax) {
      int targetOrder = computeTargetOrder(targetLink);

      if ((long) Integer.MAX_VALUE - (long) targetOrder <= TAIL_ADD_GAP) {
         redistributeLinks();

         targetOrder = computeTargetOrder(targetLink);
      }

      return isMax ? targetOrder + TAIL_ADD_GAP : targetOrder - TAIL_ADD_GAP;
   }

   private void redistributeLinks() {
      final int delta = (Integer.MAX_VALUE / (getGroupSide().size() + 1));

      AbstractDbTxTemplate dbTxWrapper = new AbstractDbTxTemplate() {

         @Override
         protected void handleTxWork() throws Exception {
            int nextOrderValue = 0;
            for (RelationLink link : getGroupSide()) {
               boolean isDirty = link.isDirty();
               nextOrderValue += delta;
               if (sideA) {
                  link.setBOrder(nextOrderValue);
               } else {
                  link.setAOrder(nextOrderValue);
               }
               link.setDirty(isDirty);
            }
            relationPersistenceManager.updateRelationOrdersWithoutTransaction(getGroupSide().toArray(
                  new RelationLink[getGroupSide().size()]));
         }

      };

      try {
         dbTxWrapper.execute();
      } catch (Exception ex) {
         logger.log(Level.SEVERE, "Error redistributing Links.", ex);
      }
   }

   /**
    * @param targetLink
    * @return Returns the link after the target link.
    */
   private RelationLink getLinkBefore(RelationLink targetLink) {
      RelationLink[] links = getGroupSide().toArray(new RelationLink[getGroupSide().size()]);
      RelationLink afterLink = null;

      for (int i = 0; i < links.length; i++) {
         if (links[i].equals(targetLink) && i - 1 > -1) {
            afterLink = links[i - 1];
            break;
         }
      }
      return afterLink;
   }

   /**
    * @param targetLink
    * @return Returns the link before the target link.
    */
   private RelationLink getLinkAfter(RelationLink targetLink) {
      RelationLink[] links = getGroupSide().toArray(new RelationLink[getGroupSide().size()]);
      RelationLink beforeLink = null;

      for (int i = 0; i < links.length; i++) {
         if (links[i].equals(targetLink) && i + 1 < links.length) {
            beforeLink = links[i + 1];
            break;
         }
      }
      return beforeLink;
   }

   private void checkArtifact(Artifact artifact) {
      if (artifact == null) throw new IllegalArgumentException("artifact can not be null");
      if (artifact.getBranch() != linkManager.getOwningArtifact().getBranch()) throw new IllegalArgumentException(
            "artifact must be on the same branch as artifact being related to");
   }
}
