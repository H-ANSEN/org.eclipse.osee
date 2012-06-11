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
package org.eclipse.osee.framework.skynet.core.event.model;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.osee.framework.skynet.core.User;

/**
 * @author Donald G. Dunne
 */
public class BroadcastEvent implements FrameworkEvent, HasNetworkSender {

   BroadcastEventType broadcastEventType;
   Collection<User> users;
   String message;
   NetworkSender networkSender;

   public BroadcastEvent(BroadcastEventType broadcastEventType, Collection<User> users, String message) {
      this.broadcastEventType = broadcastEventType;
      this.users = users != null ? users : new ArrayList<User>();
      this.message = message;
   }

   public BroadcastEventType getBroadcastEventType() {
      return broadcastEventType;
   }

   public void setBroadcastEventType(BroadcastEventType broadcastEventType) {
      this.broadcastEventType = broadcastEventType;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public Collection<User> getUsers() {
      return users;
   }

   public void setUsers(Collection<User> users) {
      this.users = users;
   }

   public void addUser(User user) {
      this.users.add(user);
   }

   @Override
   public NetworkSender getNetworkSender() {
      return networkSender;
   }

   @Override
   public void setNetworkSender(NetworkSender networkSender) {
      this.networkSender = networkSender;
   }
}
