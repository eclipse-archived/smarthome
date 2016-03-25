/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.link;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.link.AbstractLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ItemThingLink;
import org.eclipse.smarthome.core.thing.link.ItemThingLinkRegistry;
import org.eclipse.smarthome.core.thing.link.dto.AbstractLinkDTO;
import org.eclipse.smarthome.core.thing.link.dto.ItemChannelLinkDTO;
import org.eclipse.smarthome.core.thing.link.dto.ItemThingLinkDTO;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.RESTResource;

import com.google.common.collect.Iterables;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class acts as a REST resource for links.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Yordan Zhelev - Added Swagger annotations
 */
@Path(ItemChannelLinkResource.PATH_LINKS)
@Api(value = ItemChannelLinkResource.PATH_LINKS)
public class ItemChannelLinkResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_LINKS = "links";

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ItemThingLinkRegistry itemThingLinkRegistry;

    @Context
    UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available links.", response = ItemChannelLinkDTO.class, responseContainer = "Collection")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getAll() {
        Collection<ItemChannelLink> channelLinks = itemChannelLinkRegistry.getAll();
        Collection<ItemThingLink> thingLinks = itemThingLinkRegistry.getAll();
        return Response.ok(toBeans(Iterables.concat(channelLinks, thingLinks))).build();
    }

    @PUT
    @Path("/{itemName}/{channelUID}")
    @ApiOperation(value = "Links item to a channel.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Item already linked to the channel.") })
    public Response link(@PathParam("itemName") @ApiParam(value = "itemName") String itemName,
            @PathParam("channelUID") @ApiParam(value = "channelUID") String channelUid) {
        itemChannelLinkRegistry.add(new ItemChannelLink(itemName, new ChannelUID(channelUid)));
        return Response.ok().build();
    }

    @DELETE
    @Path("/{itemName}/{channelUID}")
    @ApiOperation(value = "Unlinks item from a channel.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Link not found."),
            @ApiResponse(code = 405, message = "Link not editable.") })
    public Response unlink(@PathParam("itemName") @ApiParam(value = "itemName") String itemName,
            @PathParam("channelUID") @ApiParam(value = "channelUID") String channelUid) {

        String linkId = AbstractLink.getIDFor(itemName, new ChannelUID(channelUid));
        if (itemChannelLinkRegistry.get(linkId) == null) {
            String message = "Link " + linkId + " does not exist!";
            return JSONResponse.createResponse(Status.NOT_FOUND, null, message);
        }

        ItemChannelLink result = itemChannelLinkRegistry
                .remove(AbstractLink.getIDFor(itemName, new ChannelUID(channelUid)));
        if (result != null) {
            return Response.ok().build();
        } else {
            return JSONResponse.createErrorResponse(Status.METHOD_NOT_ALLOWED, "Channel is read-only.");
        }
    }

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void setItemThingLinkRegistry(ItemThingLinkRegistry itemThingLinkRegistry) {
        this.itemThingLinkRegistry = itemThingLinkRegistry;
    }

    protected void unsetItemThingLinkRegistry(ItemThingLinkRegistry itemThingLinkRegistry) {
        this.itemThingLinkRegistry = null;
    }

    private Collection<AbstractLinkDTO> toBeans(Iterable<AbstractLink> links) {
        Collection<AbstractLinkDTO> beans = new ArrayList<>();
        for (AbstractLink link : links) {
            if (link instanceof ItemChannelLink) {
                ItemChannelLinkDTO bean = new ItemChannelLinkDTO(link.getItemName(), link.getUID().toString());
                beans.add(bean);
            } else {
                ItemThingLinkDTO bean = new ItemThingLinkDTO(link.getItemName(), link.getUID().toString());
                beans.add(bean);
            }

        }
        return beans;
    }

}
