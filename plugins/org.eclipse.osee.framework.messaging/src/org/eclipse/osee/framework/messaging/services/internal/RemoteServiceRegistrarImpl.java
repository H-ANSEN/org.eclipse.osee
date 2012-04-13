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
package org.eclipse.osee.framework.messaging.services.internal;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.osee.framework.jdk.core.type.CompositeKeyHashMap;
import org.eclipse.osee.framework.messaging.ConnectionNode;
import org.eclipse.osee.framework.messaging.services.BaseMessages;
import org.eclipse.osee.framework.messaging.services.RegisteredServiceReference;
import org.eclipse.osee.framework.messaging.services.RemoteServiceRegistrar;
import org.eclipse.osee.framework.messaging.services.ServiceInfoPopulator;

/**
 * @author Andrew M. Finkbeiner
 */
public class RemoteServiceRegistrarImpl implements RemoteServiceRegistrar {

   private final ConnectionNode connectionNode;
   private final ConcurrentHashMap<String, ScheduledFuture<?>> map;
   private final ConcurrentHashMap<String, UpdateStatus> mapOfUpdateStatus;
   private final CompositeKeyHashMap<String, String, List<UpdateStatus>> mapForReplys;
   private final ScheduledExecutorService executor;
   private final HealthRequestListener healthRequestListener;

   public RemoteServiceRegistrarImpl(ConnectionNode node, ScheduledExecutorService executor) {
      this.connectionNode = node;
      this.executor = executor;
      mapOfUpdateStatus = new ConcurrentHashMap<String, UpdateStatus>();
      map = new ConcurrentHashMap<String, ScheduledFuture<?>>();
      mapForReplys = new CompositeKeyHashMap<String, String, List<UpdateStatus>>(8, true);
      healthRequestListener = new HealthRequestListener(mapForReplys);
   }

   public void start() {
      connectionNode.subscribe(BaseMessages.ServiceHealthRequest, healthRequestListener, new OseeMessagingStatusImpl(
         "Failed to subscribe to " + BaseMessages.ServiceHealthRequest.getName(), RemoteServiceRegistrarImpl.class));
   }

   public void stop() {
      connectionNode.unsubscribe(BaseMessages.ServiceHealthRequest, healthRequestListener, new OseeMessagingStatusImpl(
         "Failed to subscribe to " + BaseMessages.ServiceHealthRequest.getName(), RemoteServiceRegistrarImpl.class));
   }

   @Override
   public RegisteredServiceReference registerService(String serviceName, String serviceVersion, String serviceUniqueId, URI broker, ServiceInfoPopulator infoPopulator, int refreshRateInSeconds) {
      String key = getKey(serviceName, serviceVersion, serviceUniqueId);
      if (!mapOfUpdateStatus.containsKey(key)) {
         UpdateStatus updateStatus =
            new UpdateStatus(this.connectionNode, serviceName, serviceVersion, serviceUniqueId, broker,
               refreshRateInSeconds, infoPopulator);
         ScheduledFuture<?> scheduled =
            executor.scheduleAtFixedRate(updateStatus, 0, refreshRateInSeconds, TimeUnit.SECONDS);
         map.put(key, scheduled);
         mapOfUpdateStatus.put(key, updateStatus);
         addToReplyMap(serviceName, serviceVersion, updateStatus);
      }
      return new ServiceReferenceImp(mapOfUpdateStatus.get(key));
   }

   private String getKey(String serviceName, String serviceVersion, String serviceUniqueId) {
      return serviceName + serviceVersion + serviceUniqueId;
   }

   @Override
   public boolean unregisterService(String serviceName, String serviceVersion, String serviceUniqueId) {
      String key = getKey(serviceName, serviceVersion, serviceUniqueId);

      UpdateStatus updateStatus = mapOfUpdateStatus.remove(key);
      if (updateStatus != null) {
         updateStatus.close();
         removeFromReplyMap(serviceName, serviceVersion, updateStatus);
      }

      ScheduledFuture<?> scheduled = map.remove(key);
      if (scheduled == null) {
         return false;
      } else {
         return scheduled.cancel(false);
      }
   }

   public void addToReplyMap(String serviceName, String serviceVersion, UpdateStatus updateForReply) {
      List<UpdateStatus> list = mapForReplys.get(serviceName, serviceVersion);
      if (list == null) {
         list = new CopyOnWriteArrayList<UpdateStatus>();
         mapForReplys.put(serviceName, serviceVersion, list);
      }
      list.add(updateForReply);
   }

   public boolean removeFromReplyMap(String serviceName, String serviceVersion, UpdateStatus updateForReply) {
      List<UpdateStatus> list = mapForReplys.get(serviceName, serviceVersion);
      if (list != null) {
         return list.remove(updateForReply);
      }
      return false;
   }

   void updateService(String key) {
      UpdateStatus update = mapOfUpdateStatus.get(key);
      if (update != null) {
         update.run();
      }
   }
}
