/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.core.util.AbstractTrackingHandler;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.messaging.event.res.IOseeCoreModelEventService;
import org.eclipse.osee.framework.skynet.core.attribute.HttpAttributeTaggingListener;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.listener.IEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.RemoteEventServiceEventType;
import org.eclipse.osee.framework.skynet.core.event.systems.EventManagerData;
import org.eclipse.osee.framework.skynet.core.event.systems.EventManagerFactory;
import org.eclipse.osee.framework.skynet.core.event.systems.InternalEventManager;
import org.eclipse.osee.framework.skynet.core.event.systems.ResMessagingConnectionListener;
import org.osgi.framework.BundleContext;

/**
 * @author Roberto E. Escobar
 */
public class OseeEventSystemServiceRegHandler extends AbstractTrackingHandler {

   private static final Class<?>[] DEPENDENCIES = new Class<?>[] {
      IOseeCachingService.class,
      IOseeCoreModelEventService.class};

   @Override
   public Class<?>[] getDependencies() {
      return DEPENDENCIES;
   }

   private final EventManagerData eventManagerData;

   private ResMessagingConnectionListener connectionStatusListener;
   private IOseeCoreModelEventService coreModelEventService;
   private final Collection<IEventListener> coreListeners = new ArrayList<IEventListener>();

   private Thread thread;

   public OseeEventSystemServiceRegHandler(EventManagerData eventManagerData) {
      this.eventManagerData = eventManagerData;
   }

   @Override
   public void onActivate(BundleContext context, Map<Class<?>, Object> services) {
      coreModelEventService = getService(IOseeCoreModelEventService.class, services);

      EventManagerFactory factory = new EventManagerFactory();

      connectionStatusListener = new ResMessagingConnectionListener(eventManagerData.getPreferences());
      final InternalEventManager eventManager =
         factory.createNewEventManager(coreModelEventService, eventManagerData.getPreferences(),
            eventManagerData.getListeners(), eventManagerData.getPriorityListeners(), connectionStatusListener);

      if (eventManager != null) {
         Runnable runnable = new Runnable() {
            @Override
            public void run() {
               try {
                  eventManagerData.setMessageEventManager(eventManager);
                  coreModelEventService.addConnectionListener(connectionStatusListener);
                  eventManager.start();
                  try {
                     OseeEventManager.kickLocalRemEvent(eventManager, RemoteEventServiceEventType.Rem_Connected);
                  } catch (OseeCoreException ex) {
                     OseeLog.log(Activator.class, Level.INFO, ex);
                  }
                  addCoreListeners();
               } catch (Throwable th) {
                  OseeLog.log(Activator.class, Level.SEVERE, th);
               }
            }
         };

         thread = new Thread(runnable);
         thread.start();

         OseeLog.log(Activator.class, Level.INFO, "Remote Event Service - Enabled");
      } else {
         OseeLog.log(Activator.class, Level.INFO, "Remote Event Service - Disabled");
      }

   }

   private void addCoreListeners() {
      coreListeners.add(new HttpAttributeTaggingListener());

      for (IEventListener listener : coreListeners) {
         OseeEventManager.addListener(listener);
      }
   }

   private void removeCoreListeners() {
      for (IEventListener listener : coreListeners) {
         OseeEventManager.removeListener(listener);
      }
      coreListeners.clear();
   }

   @Override
   public void onDeActivate() {
      if (thread != null) {
         thread.interrupt();
         thread = null;
      }
      removeCoreListeners();

      InternalEventManager eventManager = eventManagerData.getMessageEventManager();
      if (eventManager != null) {
         try {
            coreModelEventService.removeConnectionListener(connectionStatusListener);
            eventManager.stop();
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
         //         OseeEventManager.kickLocalRemEvent(eventManager, RemoteEventServiceEventType.Rem_DisConnected);
         eventManagerData.setMessageEventManager(null);
      }
   }
}
