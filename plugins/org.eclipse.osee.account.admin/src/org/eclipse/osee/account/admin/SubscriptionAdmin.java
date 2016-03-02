/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.account.admin;

import org.eclipse.osee.account.rest.model.SubscriptionGroupId;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.jdk.core.type.ResultSet;

/**
 * @author Roberto E. Escobar
 */
public interface SubscriptionAdmin {

   Subscription getSubscriptionsByEncodedId(String encodedId);

   ResultSet<Subscription> getSubscriptionsByAccountId(ArtifactId accountId);

   SubscriptionGroup getSubscriptionGroupById(SubscriptionGroupId subscriptionId);

   boolean setSubscriptionActive(Subscription subscription, boolean active);

   ResultSet<SubscriptionGroup> getSubscriptionGroups();

   SubscriptionGroupId createSubscriptionGroup(String groupName);

   boolean deleteSubscriptionById(SubscriptionGroupId subscriptionId);

   ResultSet<Account> getSubscriptionMembersOfSubscriptionById(SubscriptionGroupId groupId);
}
