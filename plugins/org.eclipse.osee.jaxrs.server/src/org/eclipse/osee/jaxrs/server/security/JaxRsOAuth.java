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
package org.eclipse.osee.jaxrs.server.security;

import java.util.ArrayList;
import java.util.List;
import org.apache.cxf.rs.security.oauth2.provider.OAuthContextProvider;
import org.apache.cxf.rs.security.oauth2.provider.OAuthJSONProvider;
import org.eclipse.osee.jaxrs.server.internal.security.oauth2.OseeOAuthContextProvider;
import org.eclipse.osee.jaxrs.server.internal.security.oauth2.provider.writers.OAuthErrorHtmlWriter;

/**
 * @author Roberto E. Escobar
 */
public final class JaxRsOAuth {

   public static final String OAUTH2_OOB_CALLBACK = "urn:ietf:wg:oauth:2.0:oob";

   private static List<? extends Object> OAUTH_PROVIDERS;

   private JaxRsOAuth() {
      // Utility Class
   }

   public static List<? extends Object> getOAuthProviders() {
      if (OAUTH_PROVIDERS == null) {
         List<Object> providers = new ArrayList<Object>();
         providers.add(new OAuthJSONProvider());
         providers.add(new OAuthContextProvider());
         providers.add(new OseeOAuthContextProvider());
         providers.add(new OAuthErrorHtmlWriter());
         OAUTH_PROVIDERS = providers;
      }
      return OAUTH_PROVIDERS;
   }

}
