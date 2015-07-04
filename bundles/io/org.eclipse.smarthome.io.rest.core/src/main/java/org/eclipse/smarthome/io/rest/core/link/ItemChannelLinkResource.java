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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.link.AbstractLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.thing.link.dto.ItemChannelLinkDTO;
import org.eclipse.smarthome.io.rest.RESTResource;

/**
 * This class acts as a REST resource for links.
 *
 * @author Dennis Nobel - Initial contribution
 */
@Path("links")
public class ItemChannelLinkResource implements RESTResource {

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ManagedItemChannelLinkProvider managedItemChannelLinkProvider;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        Collection<ItemChannelLink> links = itemChannelLinkRegistry.getAll();
        return Response.ok(toBeans(links)).build();
    }

    @PUT
    @Path("/{itemName}/{channelUID}")
    public Response link(@PathParam("itemName") String itemName, @PathParam("channelUID") String channelUid) {
        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, new ChannelUID(channelUid)));
        return Response.ok().build();
    }

    @DELETE
    @Path("/{itemName}/{channelUID}")
    public Response unlink(@PathParam("itemName") String itemName, @PathParam("channelUID") String channelUid) {
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
