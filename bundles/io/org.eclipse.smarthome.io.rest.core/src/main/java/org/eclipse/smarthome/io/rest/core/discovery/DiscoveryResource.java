/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.discovery;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.config.discovery.ScanListener;
import org.eclipse.smarthome.config.discovery.dto.DiscoveryBindingDTO;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class acts as a REST resource for discovery and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector
 * @author Yordan Zhelev - Added Swagger annotations
 * @author Ivaylo Ivanov - Added payload to the response of <code>scan</code>
 */
@Path(DiscoveryResource.PATH_DISCOVERY)
@Api(value = DiscoveryResource.PATH_DISCOVERY)
public class DiscoveryResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_DISCOVERY = "discovery";

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
    @ApiOperation(value = "Gets all bindings that support discovery.", response = String.class, responseContainer = "Set")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getDiscoveryServices() {
        Collection<DiscoveryBindingDTO> dtoList = new ArrayList<DiscoveryBindingDTO>();
        Collection<String> supportedBindings = discoveryServiceRegistry.getSupportedBindings();
        for (String binding : supportedBindings) {
            DiscoveryBindingDTO dto = new DiscoveryBindingDTO();
            dto.id = binding;
            dto.timeout = discoveryServiceRegistry.getMaxScanTimeout(binding);
            dtoList.add(dto);
        }
        return JSONResponse.createResponse(Status.OK, dtoList, "");
    }

    @POST
    @Path("/bindings/{bindingId}/scan")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Starts asynchronous discovery process for a binding and returns the timeout in seconds of the discovery operation.", response = Integer.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response scan(@PathParam("bindingId") @ApiParam(value = "bindingId") final String bindingId) {
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

        DiscoveryBindingDTO dto = new DiscoveryBindingDTO();
        dto.timeout = discoveryServiceRegistry.getMaxScanTimeout(bindingId);
        return JSONResponse.createResponse(Status.OK, dto, "");
    }

}
