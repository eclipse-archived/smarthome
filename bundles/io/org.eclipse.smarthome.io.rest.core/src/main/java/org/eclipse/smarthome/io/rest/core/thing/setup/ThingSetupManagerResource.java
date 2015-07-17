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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTO;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTOMapper;
import org.eclipse.smarthome.io.rest.core.thing.EnrichedThingDTO;
import org.eclipse.smarthome.io.rest.core.thing.EnrichedThingDTOMapper;
import org.eclipse.smarthome.io.rest.core.thing.ThingResource;

/**
 * This class acts as a REST resource for the setup manager.
 *
 * @author Dennis Nobel - Initial contribution
 */
@Path("setup")
public class ThingSetupManagerResource implements RESTResource {

    private ThingSetupManager thingSetupManager;

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("things")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addThing(EnrichedThingDTO thingBean) throws IOException {

        ThingUID thingUIDObject = new ThingUID(thingBean.UID);
        ThingUID bridgeUID = null;

        if (thingBean.bridgeUID != null) {
            bridgeUID = new ThingUID(thingBean.bridgeUID);
        }

        Configuration configuration = ThingResource.getConfiguration(thingBean);

        thingSetupManager.addThing(thingUIDObject, configuration, bridgeUID, thingBean.item.label,
                thingBean.item.groupNames);

        return Response.ok().build();
    }

    @PUT
    @Path("things")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateThing(EnrichedThingDTO thingBean) throws IOException {

        ThingUID thingUID = new ThingUID(thingBean.UID);
        ThingUID bridgeUID = null;

        if (thingBean.bridgeUID != null) {
            bridgeUID = new ThingUID(thingBean.bridgeUID);
        }

        Configuration configuration = ThingResource.getConfiguration(thingBean);

        Thing thing = thingSetupManager.getThing(thingUID);

        if (thingBean.item != null && thing != null) {
            String label = thingBean.item.label;
            List<String> groupNames = thingBean.item.groupNames;

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
    public Response removeThing(@PathParam("thingUID") String thingUID,
            @DefaultValue("false") @QueryParam("force") boolean force) {
        thingSetupManager.removeThing(new ThingUID(thingUID), force);
        return Response.ok().build();
    }

    @DELETE
    @Path("/things/channels/{channelUID}")
    public Response disableChannel(@PathParam("channelUID") String channelUID) {
        thingSetupManager.disableChannel(new ChannelUID(channelUID));
        return Response.ok().build();
    }

    @PUT
    @Path("/things/channels/{channelUID}")
    public Response enableChannel(@PathParam("channelUID") String channelUID) {
        thingSetupManager.enableChannel(new ChannelUID(channelUID));
        return Response.ok().build();
    }

    @GET
    @Path("things")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThings() {
        List<EnrichedThingDTO> thingBeans = new ArrayList<>();
        Collection<Thing> things = thingSetupManager.getThings();
        for (Thing thing : things) {
            EnrichedThingDTO thingItemBean = EnrichedThingDTOMapper.map(thing, uriInfo.getBaseUri());
            thingBeans.add(thingItemBean);
        }
        return Response.ok(thingBeans).build();
    }

    @PUT
    @Path("/things/{thingUID}/label")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response setLabel(@PathParam("thingUID") String thingUID, String label) {
        thingSetupManager.setLabel(new ThingUID(thingUID), label);
        return Response.ok().build();
    }

    @PUT
    @Path("/things/{thingUID}/groups")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setGroups(@PathParam("thingUID") String thingUID, List<String> groupNames) {
        Thing thing = thingSetupManager.getThing(new ThingUID(thingUID));

        if (thing != null) {
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
    public Response getHomeGroups() {
        List<EnrichedItemDTO> itemBeans = new ArrayList<>();
        Collection<GroupItem> homeGroups = thingSetupManager.getHomeGroups();
        for (GroupItem homeGroupItem : homeGroups) {
            EnrichedItemDTO itemBean = EnrichedItemDTOMapper.map(homeGroupItem, true, uriInfo.getBaseUri());
            itemBeans.add(itemBean);
        }
        return Response.ok(itemBeans).build();
    }

    @POST
    @Path("groups")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addHomeGroup(ItemDTO itemBean) {
        thingSetupManager.addHomeGroup(itemBean.name, itemBean.label);
        return Response.ok().build();
    }

    @DELETE
    @Path("groups/{itemName}")
    public Response removeHomeGroup(@PathParam("itemName") String itemName) {
        thingSetupManager.removeHomeGroup(itemName);
        return Response.ok().build();
    }

    @PUT
    @Path("groups/{itemName}/label")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response setHomeGroupLabel(@PathParam("itemName") String itemName, String label) {
        thingSetupManager.setHomeGroupLabel(itemName, label);
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
