/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.link;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.thing.link.dto.ItemChannelLinkDTO;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a REST resource for links.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Yordan Zhelev - Added Swagger annotations
 */
@Path(ItemChannelLinkResource.PATH_LLINKS)
@Api
public class ItemChannelLinkResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(ItemChannelLinkResource.class);

    /** The URI path to this resource */
    public static final String PATH_LLINKS = "links";

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ManagedItemChannelLinkProvider managedItemChannelLinkProvider;

    @Context
    UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available links.", response = ItemChannelLinkDTO.class, responseContainer = "Collection")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getAll() {
        Collection<ItemChannelLink> links = itemChannelLinkRegistry.getAll();
        return Response.ok(toBeans(links)).build();
    }

    @PUT
    @Path("/{itemName}/{channelUID}")
    @ApiOperation(value = "Links item to a channel.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Item already linked to the channel.") })
    public Response link(@PathParam("itemName") @ApiParam(value = "itemName") String itemName,
            @PathParam("channelUID") @ApiParam(value = "channelUID") String channelUid) {

        try {
            managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, new ChannelUID(channelUid)));
        } catch (IllegalArgumentException ex) {
            logger.warn("Received HTTP PUT request at '{}' for existing ItemChannelLink.", uriInfo.getPath());
            return Response.status(Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/{itemName}/{channelUID}")
    @ApiOperation(value = "Unlinks item from a channel.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response unlink(@PathParam("itemName") @ApiParam(value = "itemName") String itemName,
            @PathParam("channelUID") @ApiParam(value = "channelUID") String channelUid) {
        managedItemChannelLinkProvider.remove(AbstractLink.getIDFor(itemName, new ChannelUID(channelUid)));
        return Response.ok().build();
    }

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void setManagedItemChannelLinkProvider(ManagedItemChannelLinkProvider managedItemChannelLinkProvider) {
        this.managedItemChannelLinkProvider = managedItemChannelLinkProvider;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void unsetManagedItemChannelLinkProvider(ManagedItemChannelLinkProvider managedItemChannelLinkProvider) {
        this.managedItemChannelLinkProvider = null;
    }

    private Collection<ItemChannelLinkDTO> toBeans(Collection<ItemChannelLink> links) {
        Collection<ItemChannelLinkDTO> beans = new ArrayList<>();
        for (ItemChannelLink link : links) {
            ItemChannelLinkDTO bean = new ItemChannelLinkDTO(link.getItemName(), link.getUID().toString());
            beans.add(bean);
        }
        return beans;
    }

}
