/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.id.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.core.id.InstanceUUID;
import org.eclipse.smarthome.io.rest.RESTResource;

/**
 * This class acts as a REST resource for accessing the UUID of the instance
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
@Path(UUIDResource.PATH_UUID)
public class UUIDResource implements RESTResource {

    public static final String PATH_UUID = "uuid";

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSitemaps() {
        return Response.ok(InstanceUUID.get()).build();
    }

}
