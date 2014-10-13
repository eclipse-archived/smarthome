/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.io.rest.AbstractRESTResource;
import org.eclipse.smarthome.io.rest.core.thing.beans.ChannelBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ThingBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ThingListBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a REST resource for things and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 */
@Path("things")
public class ThingResource extends AbstractRESTResource {

    private static final Logger logger = LoggerFactory.getLogger(ThingResource.class);

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/{thingUID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("thingUID") String thingUID, String body) throws JsonParseException,
            JsonMappingException, IOException {

        // TODO: Use ThingBean as method argument instead of String (be aware of class loader problems)
        ThingBean thingBean = parse(body);

        ThingUID thingUIDObject = new ThingUID(thingUID);
        ThingUID bridgeUID = null;

        if (thingBean.bridgeUID != null) {
            bridgeUID = new ThingUID(thingBean.bridgeUID);
        }

        Configuration configuration = new Configuration(thingBean.configuration);

        ManagedThingProvider managedThingProvider = getService(ManagedThingProvider.class);
        managedThingProvider.createThing(thingUIDObject.getThingTypeUID(), thingUIDObject, bridgeUID, configuration);

        return Response.ok().build();
    }

    @GET
    @Produces({ MediaType.WILDCARD })
    public Response getAll() {

        ThingRegistry thingRegistry = getService(ThingRegistry.class);

        Collection<Thing> things = thingRegistry.getAll();
        ThingListBean thingListBean = convertToListBean(things);

        return Response.ok(thingListBean).build();
    }

    @POST
    @Path("/{thingUID}/channels/{channelId}/link")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response link(@PathParam("thingUID") String thingUID, @PathParam("channelId") String channelId,
            String itemName) {

        ThingRegistry thingRegistry = getService(ThingRegistry.class);

        Thing thing = thingRegistry.getByUID(new ThingUID(thingUID));
        if (thing == null) {
            logger.info("Received HTTP POST request at '{}' for the unknown thing '{}'.", uriInfo.getPath(), thingUID);
            throw new WebApplicationException(404);
        }

        Channel channel = findChannel(channelId, thing);
        if (channel == null) {
            logger.info("Received HTTP POST request at '{}' for the unknown channel '{}' of the thing '{}'",
                    uriInfo.getPath(), channel, thingUID);
            throw new WebApplicationException(404);
        }

        ItemRegistry itemRegistry = getService(ItemRegistry.class);
        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException ex) {
            ManagedItemProvider managedItemProvider = getService(ManagedItemProvider.class);
            if (managedItemProvider == null) {
                logger.error("Cannot create new item. ManagedItemProvider OSGi service was not found.");
                return Response.serverError().build();
            }

            ItemFactory itemFactory = getService(ItemFactory.class);
            if (itemFactory == null) {
                logger.error("Cannot create new item. ItemFactory OSGi service was not found.");
                return Response.serverError().build();
            }

            GenericItem item = itemFactory.createItem(channel.getAcceptedItemType(), itemName);
            managedItemProvider.add(item);
        }

        ManagedItemChannelLinkProvider managedItemChannelLinkProvider = getService(ManagedItemChannelLinkProvider.class);
        if (managedItemChannelLinkProvider == null) {
            logger.error("Cannot link channel. ManagedItemChannelLinkProvider OSGi service was not found.");
            return Response.serverError().build();
        }

        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, new ChannelUID(new ThingUID(thingUID),
                channelId)));

        return Response.ok().build();
    }

    @DELETE
    @Path("/{thingUID}")
    public Response remove(@PathParam("thingUID") String thingUID) {

        ManagedThingProvider managedThingProvider = getService(ManagedThingProvider.class);

        if (managedThingProvider.remove(new ThingUID(thingUID)) == null) {
            logger.info("Received HTTP DELETE request at '{}' for the unknown thing '{}'.", uriInfo.getPath(), thingUID);
            throw new WebApplicationException(404);
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("/{thingUID}/channels/{channelId}/link")
    public Response unlink(@PathParam("thingUID") String thingUID, @PathParam("channelId") String channelId,
            String itemName) {

        ItemChannelLinkRegistry itemChannelLinkRegistry = getService(ItemChannelLinkRegistry.class);
        ChannelUID channelUID = new ChannelUID(new ThingUID(thingUID), channelId);
        String boundItem = itemChannelLinkRegistry.getBoundItem(channelUID);

        if (boundItem != null) {
            ManagedItemChannelLinkProvider managedItemChannelLinkProvider = getService(ManagedItemChannelLinkProvider.class);
            managedItemChannelLinkProvider.remove(new ItemChannelLink(boundItem, channelUID).getID());
        }

        return Response.ok().build();
    }

    private ChannelBean convertToChannelBean(Channel channel) {
        ItemChannelLinkRegistry itemChannelLinkRegistry = getService(ItemChannelLinkRegistry.class);
        String boundItem = itemChannelLinkRegistry.getBoundItem(channel.getUID());
        return new ChannelBean(channel.getUID().getId(), channel.getAcceptedItemType().toString(), boundItem);
    }

    private ThingListBean convertToListBean(Collection<Thing> things) {
        List<ThingBean> thingBeans = new ArrayList<>();
        for (Thing thing : things) {
            ThingBean thingBean = convertToThingBean(thing);
            thingBeans.add(thingBean);
        }
        return new ThingListBean(thingBeans);
    }

    private ThingBean convertToThingBean(Thing thing) {
        List<ChannelBean> channelBeans = new ArrayList<>();
        for (Channel channel : thing.getChannels()) {
            ChannelBean channelBean = convertToChannelBean(channel);
            channelBeans.add(channelBean);
        }

        String thingUID = thing.getUID().toString();
        String bridgeUID = thing.getBridgeUID() != null ? thing.getBridgeUID().toString() : null;

        return new ThingBean(thingUID, bridgeUID, thing.getStatus(), channelBeans, thing.getConfiguration());
    }

    private Channel findChannel(String channelId, Thing thing) {
        for (Channel channel : thing.getChannels()) {
            if (channel.getUID().getId().equals(channelId)) {
                return channel;
            }
        }
        return null;
    }

    private ThingBean parse(String body) throws IOException, JsonParseException, JsonMappingException {
        // Deserialization of the bean works different compared to
        // serialization. It does not respect the xml annotations.
        // Therefore a valid json looks like this: { UID: 'a:b:c',
        // configuration: { key1: 'value', key2: 'value'} }.
        ObjectMapper mapper = new ObjectMapper();
        ThingBean thingBean = mapper.readValue(body, ThingBean.class);
        return thingBean;
    }

}
