/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.discovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.dto.DiscoveryResultDTO;
import org.eclipse.smarthome.config.discovery.dto.DiscoveryResultDTOMapper;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.io.rest.RESTResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class acts as a REST resource for the inbox and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector
 * @author Yordan Zhelev - Added Swagger annotations
 */
@Path(InboxResource.PATH_INBOX)
@Api(value = InboxResource.PATH_INBOX)
public class InboxResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_INBOX = "inbox";

    private ThingSetupManager thingSetupManager;
    private ThingTypeRegistry thingTypeRegistry;
    private ConfigDescriptionRegistry configDescRegistry;
    private Inbox inbox;

    protected void setInbox(Inbox inbox) {
        this.inbox = inbox;
    }

    protected void unsetInbox(Inbox inbox) {
        this.inbox = null;
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescRegistry = null;
    }

    protected void setThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = thingSetupManager;
    }

    protected void unsetThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = null;
    }

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/{thingUID}/approve")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Approves the discovery result by adding the thing to the registry.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Thing not found in the inbox.") })
    public Response approve(@PathParam("thingUID") @ApiParam(value = "thingUID", required = true) String thingUID,
            @ApiParam(value = "thing label") String label,
            @QueryParam("enableChannels") @DefaultValue("true") @ApiParam(value = "enable channels", required = false) boolean enableChannels) {
        ThingUID thingUIDObject = new ThingUID(thingUID);
        List<DiscoveryResult> results = inbox.get(new InboxFilterCriteria(thingUIDObject, null));
        if (results.isEmpty()) {
            return Response.status(Status.NOT_FOUND).build();
        }
        DiscoveryResult result = results.get(0);
        Map<String, Object> discoveryProperties = result.getProperties();
        Set<String> configDescParamNames = getConfigurationDescParamNames(result.getThingTypeUID());
        Configuration config = new Configuration();
        Map<String, String> properties = new HashMap<>();
        for (String key : discoveryProperties.keySet()) {
            Object value = discoveryProperties.get(key);
            if (configDescParamNames.contains(key)) {
                config.put(key, value);
            } else {
                properties.put(key, String.valueOf(value));
            }
        }
        thingSetupManager.addThing(result.getThingUID(), config, result.getBridgeUID(),
                label != null && !label.isEmpty() ? label : null, new ArrayList<String>(), enableChannels, properties);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{thingUID}")
    @ApiOperation(value = "Removes the discovery result from the inbox.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Discovery result not found in the inbox.") })
    public Response delete(@PathParam("thingUID") @ApiParam(value = "thingUID", required = true) String thingUID) {
        if (inbox.remove(new ThingUID(thingUID))) {
            return Response.ok().build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Produces({ MediaType.WILDCARD })
    @ApiOperation(value = "Get all discovered things.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getAll() {
        List<DiscoveryResult> discoveryResults = inbox.getAll();
        Set<DiscoveryResultDTO> discoveryResultBeans = convertToListBean(discoveryResults);

        return Response.ok(discoveryResultBeans).build();
    }

    @POST
    @Path("/{thingUID}/ignore")
    @ApiOperation(value = "Flags a discovery result as ignored for further processing.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response ignore(@PathParam("thingUID") @ApiParam(value = "thingUID", required = true) String thingUID) {
        inbox.setFlag(new ThingUID(thingUID), DiscoveryResultFlag.IGNORED);
        return Response.ok().build();
    }

    @POST
    @Path("/{thingUID}/unignore")
    @ApiOperation(value = "Removes ignore flag from a discovery result.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response unignore(@PathParam("thingUID") @ApiParam(value = "thingUID", required = true) String thingUID) {
        inbox.setFlag(new ThingUID(thingUID), DiscoveryResultFlag.NEW);
        return Response.ok().build();
    }

    private Set<DiscoveryResultDTO> convertToListBean(List<DiscoveryResult> discoveryResults) {
        Set<DiscoveryResultDTO> discoveryResultBeans = new LinkedHashSet<>();
        for (DiscoveryResult discoveryResult : discoveryResults) {
            discoveryResultBeans.add(DiscoveryResultDTOMapper.map(discoveryResult));
        }
        return discoveryResultBeans;
    }

    private Set<String> getConfigurationDescParamNames(ThingTypeUID typeUID) {
        Set<String> paramNames = new HashSet<>();
        ThingType type = thingTypeRegistry.getThingType(typeUID);
        if (type != null && type.hasConfigDescriptionURI()) {
            URI descURI = type.getConfigDescriptionURI();
            ConfigDescription desc = configDescRegistry.getConfigDescription(descURI);
            if (desc != null) {
                for (ConfigDescriptionParameter param : desc.getParameters()) {
                    paramNames.add(param.getName());
                }
            }
        }
        return paramNames;
    }

}
