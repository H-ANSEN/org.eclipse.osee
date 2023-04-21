/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.ats.api.config;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.osee.framework.jdk.core.annotation.Swagger;

/**
 * @author Donald G. Dunne
 */
@Swagger
public interface BaseConfigEndpointApi<T extends JaxAtsObject> {

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<T> get() throws Exception;

   @GET
   @Path("{id}")
   @Produces(MediaType.APPLICATION_JSON)
   public T get(@PathParam("id") long id) throws Exception;

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response create(T atsConfigObject) throws Exception;

   @DELETE
   @Path("{id}")
   public Response delete(@PathParam("id") long id) throws Exception;

}
