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
package org.eclipse.osee.ats.core.cpa;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.ats.api.cpa.CpaBuild;
import org.eclipse.osee.ats.api.cpa.CpaDecision;
import org.eclipse.osee.ats.api.cpa.CpaProgram;
import org.eclipse.osee.ats.api.program.IAtsProgram;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;

/**
 * @author Donald G. Dunne
 */
public class CpaFactory {

   private static final Map<String, Object> idToStoreObject = new HashMap<String, Object>(500);
   public static final String CPA_BASEPATH_KEY = "CpaBasepath";

   public static CpaProgram get(IAtsProgram program) {
      return getProgram(program, null);
   }

   public static CpaProgram getProgram(IAtsProgram program, Object storeObject) {
      CpaProgram prog = new CpaProgram(program.getUuid(), program.getName());
      setStoreObject(String.valueOf(program.getUuid()), storeObject);
      return prog;
   }

   private static void setStoreObject(String key, Object object) {
      idToStoreObject.put(key, object);
   }

   public static Object getStoreObject(Object obj) {
      Object result = null;
      if (obj instanceof IAtsProgram) {
         result = idToStoreObject.get(((IAtsProgram) obj).getUuid());
      } else if (obj instanceof CpaDecision) {
         result = idToStoreObject.get(((CpaDecision) obj).getGuid());
      }
      return result;
   }

   public static CpaDecision getDecision(IAtsTeamWorkflow teamWf) {
      return getDecision(teamWf, null);
   }

   public static CpaDecision getDecision(IAtsTeamWorkflow teamWf, Object storeObject) {
      CpaDecision decision = new CpaDecision(teamWf.getAtsId(), teamWf.getName());
      setStoreObject(teamWf.getAtsId(), storeObject);
      return decision;
   }

   public static CpaBuild getVersion(IAtsVersion version, Object storeObject) {
      Long uuid = version.getUuid();
      CpaBuild build = new CpaBuild(uuid, version.getName());
      setStoreObject(String.valueOf(uuid), storeObject);
      return build;
   }

}
