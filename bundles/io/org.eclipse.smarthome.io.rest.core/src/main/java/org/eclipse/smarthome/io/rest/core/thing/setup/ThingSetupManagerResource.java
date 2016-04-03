/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.setup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTO;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTOMapper;
import org.eclipse.smarthome.io.rest.core.thing.EnrichedThingDTO;
import org.eclipse.smarthome.io.rest.core.thing.EnrichedThingDTOMapper;
import org.eclipse.smarthome.io.rest.core.thing.ThingResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class acts as a REST resource for the setup manager.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Yordan Zhelev - Added Swagger annotations
 */
@Path(ThingSetupManagerResource.PATH_SETUP)
@Api(value = ThingSetupManagerResource.PATH_SETUP, hidden = true)
public class ThingSetupManagerResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(ThingSetupManagerResource.class);

    /** The URI path to this resource */
    public static final String PATH_SETUP = "setup";

    private ThingSetupManager thingSetupManager;

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("things")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Adds a new thing to the registry.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response addThing(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language,
            @ApiParam(value = "thing data", required = true) EnrichedThingDTO thingBean,
            @QueryParam("enableChannels") @DefaultValue("true") @ApiParam(value = "enable channels", required = false) boolean enableChannels)
                    throws IOException {
        final Locale locale = LocaleUtil.getLocale(language);

        ThingUID thingUIDObject = new ThingUID(thingBean.UID);
        ThingUID bridgeUID = null;

        if (thingBean.bridgeUID != null) {
            bridgeUID = new ThingUID(thingBean.bridgeUID);
        }

        Configuration configuration = new Configuration(thingBean.configuration);

        thingSetupManager.addThing(thingUIDObject, configuration, bridgeUID, thingBean.label, thingBean.item.groupNames,
                enableChannels, locale);

        return Response.ok().build();
    }

    @PUT
    @Path("things")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a thing.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response updateThing(@ApiParam(value = "thing data", required = true) EnrichedThingDTO thingBean)
            throws IOException {

        ThingUID thingUID = new ThingUID(thingBean.UID);
        ThingUID bridgeUID = null;

        if (thingBean.bridgeUID != null) {
            bridgeUID = new ThingUID(thingBean.bridgeUID);
        }

        Configuration configuration = new Configuration(thingBean.configuration);

        Thing thing = thingSetupManager.getThing(thingUID);

        if (thingBean.item != null && thing != null) {
            String label = thingBean.label;
            List<String> groupNames = thingBean.item.groupNames;

            @SuppressWarnings("deprecation")
            GroupItem thingGroupItem = thing.getLinkedItem();
            if (thingGroupItem != null) {
                boolean labelChanged = false;
                if (thingGroupItem.getLabel() == null || !thingGroupItem.getLabel().equals(label)) {
                    thingGroupItem.setLabel(label);
                    labelChanged = true;
                }
                boolean groupsChanged = setGroupNames(thingGroupItem, groupNames);
                if (labelChanged || groupsChanged) {
                    thingSetupManager.updateItem(thingGroupItem);
                }
            }
        }

        if (thing != null) {
            if (bridgeUID != null) {
                thing.setBridgeUID(bridgeUID);
            }
            ThingResource.updateConfiguration(thing, configuration);
            thingSetupManager.updateThing(thing);
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("/things/{thingUID}")
    @ApiOperation(value = "Removes a thing from the registry. Set \'force\' to __true__ if you want the thing te be removed immediately")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response removeThing(@PathParam("thingUID") @ApiParam(value = "thingUID") String thingUID,
            @DefaultValue("false") @QueryParam("force") @ApiParam(value = "force") boolean force) {
        thingSetupManager.removeThing(new ThingUID(thingUID), force);
        return Response.ok().build();
    }

    @DELETE
    @Path("/things/channels/{channelUID}")
    @ApiOperation(value = "Removes corresponding item and the link for the channel.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response disableChannel(@PathParam("channelUID") @ApiParam(value = "channelUID") String channelUID) {
        thingSetupManager.disableChannel(new ChannelUID(channelUID));
        return Response.ok().build();
    }

    @PUT
    @Path("/things/channels/{channelUID}")
    @ApiOperation(value = "Adds corresponding item and the link for the channel.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response enableChannel(
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language,
            @PathParam("channelUID") @ApiParam(value = "channelUID") String channelUID) {
        final Locale locale = LocaleUtil.getLocale(language);
        thingSetupManager.enableChannel(new ChannelUID(channelUID), locale);
        return Response.ok().build();
    }

    @GET
    @Path("things")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available things.", response = EnrichedThingDTO.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getThings(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language) {
        final Locale locale = LocaleUtil.getLocale(language);

        List<EnrichedThingDTO> thingBeans = new ArrayList<>();
        Collection<Thing> things = thingSetupManager.getThings();
        for (Thing thing : things) {
            EnrichedThingDTO thingItemBean = EnrichedThingDTOMapper.map(thing, uriInfo.getBaseUri(), locale);
            thingBeans.add(thingItemBean);
        }
        return Response.ok(thingBeans).build();
    }

    @PUT
    @Path("/things/{thingUID}/label")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Sets the label for a given thing UID")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response setLabel(@PathParam("thingUID") @ApiParam(value = "thingUID") String thingUID,
            @ApiParam(value = "label") String label) {
        thingSetupManager.setLabel(new ThingUID(thingUID), label);
        return Response.ok().build();
    }

    @PUT
    @Path("/things/{thingUID}/groups")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Sets group names to the group item linked to the thing.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response setGroups(@PathParam("thingUID") @ApiParam(value = "thingUID") String thingUID,
            @ApiParam(value = "group names") List<String> groupNames) {
        Thing thing = thingSetupManager.getThing(new ThingUID(thingUID));

        if (thing != null) {
            @SuppressWarnings("deprecation")
            GroupItem thingGroupItem = thing.getLinkedItem();
            if (thingGroupItem != null) {
                boolean groupsChanged = setGroupNames(thingGroupItem, groupNames);
                if (groupsChanged) {
                    thingSetupManager.updateItem(thingGroupItem);
                }
            }
        }

        return Response.ok().build();
    }

    @GET
    @Path("groups")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available group items with tag \'home-group\'.", response = EnrichedItemDTO.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getHomeGroups(
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language) {
        final Locale locale = LocaleUtil.getLocale(language);

        List<EnrichedItemDTO> itemBeans = new ArrayList<>();
        Collection<GroupItem> homeGroups = thingSetupManager.getHomeGroups();
        for (GroupItem homeGroupItem : homeGroups) {
            EnrichedItemDTO itemBean = EnrichedItemDTOMapper.map(homeGroupItem, true, uriInfo.getBaseUri(), locale);
            itemBeans.add(itemBean);
        }
        return Response.ok(itemBeans).build();
    }

    @POST
    @Path("groups")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a group item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response addHomeGroup(@ApiParam(value = "item data") ItemDTO itemBean) {
        thingSetupManager.addHomeGroup(itemBean.name, itemBean.label);
        return Response.ok().build();
    }

    @DELETE
    @Path("groups/{itemName}")
    @ApiOperation(value = "Removes a group item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response removeHomeGroup(@PathParam("itemName") @ApiParam(value = "item name") String itemName) {
        thingSetupManager.removeHomeGroup(itemName);
        return Response.ok().build();
    }

    @PUT
    @Path("groups/{itemName}/label")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Sets label of the group item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Group item not found") })
    public Response setHomeGroupLabel(@PathParam("itemName") @ApiParam(value = "item name") String itemName,
            @ApiParam(value = "label") String label) {
        try {
            thingSetupManager.setHomeGroupLabel(itemName, label);
        } catch (IllegalArgumentException ex) {
            logger.info("Received HTTP PUT request for set home group label at '{}' for the unknown group item '{}'.",
                    uriInfo.getPath(), itemName);
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    protected void setThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = thingSetupManager;
    }

    protected void unsetThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = null;
    }

    private boolean setGroupNames(GroupItem thingGroupItem, List<String> groupNames) {
        boolean itemUpdated = false;
        for (String groupName : groupNames) {
            if (!thingGroupItem.getGroupNames().contains(groupName)) {
                thingGroupItem.addGroupName(groupName);
                itemUpdated = true;
            }
        }
        for (String groupName : thingGroupItem.getGroupNames()) {
            if (!groupNames.contains(groupName)) {
                thingGroupItem.removeGroupName(groupName);
                itemUpdated = true;
            }
        }
        return itemUpdated;
    }
}
