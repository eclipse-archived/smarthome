/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.discovery;

import java.util.Collection;
import java.util.LinkedHashSet;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.config.discovery.ScanListener;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a REST resource for discovery and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector
 */
@Path("discovery")
public class DiscoveryResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(DiscoveryResource.class);

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscoveryServices() {
        Collection<String> supportedBindings = discoveryServiceRegistry.getSupportedBindings();
        return Response.ok(new LinkedHashSet<>(supportedBindings)).build();
    }

    @POST
    @Path("/scan/{bindingId}")
    public Response scan(@PathParam("bindingId") final String bindingId) {
        discoveryServiceRegistry.startScan(bindingId, new ScanListener() {
            @Override
            public void onErrorOccurred(Exception exception) {
                logger.error("Error occured while scanning for binding '{}': {}", bindingId, exception.getMessage(),
                        exception);
            }

            @Override
            public void onFinished() {
                logger.debug("Scan for binding '{}' successfully finished.", bindingId);
            }
        });
        return Response.ok().build();
    }

}
