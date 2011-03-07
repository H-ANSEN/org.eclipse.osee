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
package org.eclipse.osee.ats.artifact;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.ats.actions.wizard.ITeamWorkflowProvider;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsArtifactTypes;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.osgi.framework.Bundle;

/**
 * @author Donald G. Dunne
 */
public final class TeamWorkflowProviders {

   private static Set<ITeamWorkflowProvider> atsTeamWorkflows;

   private TeamWorkflowProviders() {
      // private constructor
   }

   public static Set<IArtifactType> getAllTeamWorkflowArtifactTypes() throws OseeCoreException {
      Set<IArtifactType> artifactTypes = new HashSet<IArtifactType>();
      artifactTypes.add(AtsArtifactTypes.TeamWorkflow);
      for (ITeamWorkflowProvider ext : getAtsTeamWorkflowExtensions()) {
         artifactTypes.addAll(ext.getTeamWorkflowArtifactTypes());
      }
      return artifactTypes;
   }

   /*
    * due to lazy initialization, this function is non-reentrant therefore, the synchronized keyword is necessary
    */
   public synchronized static Set<ITeamWorkflowProvider> getAtsTeamWorkflowExtensions() {
      if (atsTeamWorkflows != null) {
         return atsTeamWorkflows;
      }
      atsTeamWorkflows = new HashSet<ITeamWorkflowProvider>();

      IExtensionPoint point =
         Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.osee.ats.AtsTeamWorkflowProvider");
      if (point == null) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, "Can't access AtsTeamWorkflow extension point");
         return atsTeamWorkflows;
      }
      IExtension[] extensions = point.getExtensions();
      for (IExtension extension : extensions) {
         IConfigurationElement[] elements = extension.getConfigurationElements();
         String classname = null;
         String bundleName = null;
         for (IConfigurationElement el : elements) {
            if (el.getName().equals("TeamWorkflowProvider")) {
               classname = el.getAttribute("classname");
               bundleName = el.getContributor().getName();
               if (classname != null && bundleName != null) {
                  Bundle bundle = Platform.getBundle(bundleName);
                  try {
                     Class<?> taskClass = bundle.loadClass(classname);
                     Object obj = taskClass.newInstance();
                     atsTeamWorkflows.add((ITeamWorkflowProvider) obj);
                  } catch (Exception ex) {
                     OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP,
                        "Error loading TeamWorkflowProvider extension", ex);
                  }
               }
            }
         }
      }
      return atsTeamWorkflows;
   }

}
