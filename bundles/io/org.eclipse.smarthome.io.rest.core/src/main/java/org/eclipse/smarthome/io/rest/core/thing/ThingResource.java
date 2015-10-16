/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

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
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.io.rest.ConfigUtil;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a REST resource for things and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector
 */
@Path("things")
public class ThingResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(ThingResource.class);

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ItemFactory itemFactory;
    private ItemRegistry itemRegistry;
    private ManagedItemChannelLinkProvider managedItemChannelLinkProvider;
    private ManagedItemProvider managedItemProvider;
    private ManagedThingProvider managedThingProvider;
    private ThingRegistry thingRegistry;

    @Context
    private UriInfo uriInfo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(ThingDTO thingBean) throws IOException {

        ThingUID thingUIDObject = new ThingUID(thingBean.UID);
        ThingUID bridgeUID = null;

        if (thingBean.bridgeUID != null) {
            bridgeUID = new ThingUID(thingBean.bridgeUID);
        }

        Configuration configuration = getConfiguration(thingBean);

        managedThingProvider.createThing(thingUIDObject.getThingTypeUID(), thingUIDObject, bridgeUID, configuration);

        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {

        Collection<Thing> things = thingRegistry.getAll();
        Set<EnrichedThingDTO> thingBeans = convertToListBean(things);

        return Response.ok(thingBeans).build();
    }

    @GET
    @Path("/{thingUID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByUID(@PathParam("thingUID") String thingUID) {
        Thing thing = thingRegistry.get((new ThingUID(thingUID)));
        if (thing != null) {
            return Response.ok(EnrichedThingDTOMapper.map(thing, uriInfo.getBaseUri())).build();
        } else {
            return Response.noContent().build();
        }
    }

    @POST
    @Path("/{thingUID}/channels/{channelId}/link")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response link(@PathParam("thingUID") String thingUID, @PathParam("channelId") String channelId,
            String itemName) {

        Thing thing = thingRegistry.get(new ThingUID(thingUID));
        if (thing == null) {
            logger.warn("Received HTTP POST request at '{}' for the unknown thing '{}'.", uriInfo.getPath(), thingUID);
            return Response.status(Status.NOT_FOUND).build();
        }

        Channel channel = findChannel(channelId, thing);
        if (channel == null) {
            logger.info("Received HTTP POST request at '{}' for the unknown channel '{}' of the thing '{}'",
                    uriInfo.getPath(), channel, thingUID);
            return Response.status(Status.NOT_FOUND).build();
        }

        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException ex) {
            GenericItem item = itemFactory.createItem(channel.getAcceptedItemType(), itemName);
            managedItemProvider.add(item);
        }

        ChannelUID channelUID = new ChannelUID(new ThingUID(thingUID), channelId);

        unlinkChannelIfAlreadyLinked(channelUID);

        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, channelUID));

        return Response.ok().build();
    }

    @DELETE
    @Path("/{thingUID}")
    public Response remove(@PathParam("thingUID") String thingUID,
            @DefaultValue("false") @QueryParam("force") boolean force) {

        Thing removedThing = null;
        if (force) {
            removedThing = thingRegistry.forceRemove(new ThingUID(thingUID));
        } else {
            removedThing = thingRegistry.remove(new ThingUID(thingUID));
        }

        if (removedThing == null) {
            logger.info("Received HTTP DELETE request at '{}' for the unknown thing '{}'.", uriInfo.getPath(),
                    thingUID);
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("/{thingUID}/channels/{channelId}/link")
    public Response unlink(@PathParam("thingUID") String thingUID, @PathParam("channelId") String channelId,
            String itemName) {

        ChannelUID channelUID = new ChannelUID(new ThingUID(thingUID), channelId);

        if (itemChannelLinkRegistry.isLinked(itemName, channelUID)) {
            managedItemChannelLinkProvider.remove(new ItemChannelLink(itemName, channelUID).getID());
        }

        return Response.ok().build();
    }

    @PUT
    @Path("/{thingUID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("thingUID") String thingUID, ThingDTO thingBean) throws IOException {

        ThingUID thingUIDObject = new ThingUID(thingUID);
        ThingUID bridgeUID = null;

        if (thingBean.bridgeUID != null) {
            bridgeUID = new ThingUID(thingBean.bridgeUID);
        }

        Thing thing = managedThingProvider.get(thingUIDObject);
        if (thing == null) {
            logger.info("Received HTTP PUT request for update at '{}' for the unknown thing '{}'.", uriInfo.getPath(),
                    thingUID);
            return Response.status(Status.NOT_FOUND).build();
        }

        thing.setBridgeUID(bridgeUID);

        updateConfiguration(thing, getConfiguration(thingBean));

        managedThingProvider.update(thing);

        return Response.ok().build();
    }

    @PUT
    @Path("/{thingUID}/config")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateConfiguration(@PathParam("thingUID") String thingUID,
            Map<String, Object> configurationParameters) throws IOException {

        try {
            thingRegistry.updateConfiguration(new ThingUID(thingUID),
                    ConfigUtil.normalizeTypes(configurationParameters));
        } catch (IllegalArgumentException ex) {
            logger.info("Received HTTP PUT request for update config at '{}' for the unknown thing '{}'.",
                    uriInfo.getPath(), thingUID);
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok().build();
    }

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void setItemFactory(ItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void setManagedItemChannelLinkProvider(ManagedItemChannelLinkProvider managedItemChannelLinkProvider) {
        this.managedItemChannelLinkProvider = managedItemChannelLinkProvider;
    }

    protected void setManagedItemProvider(ManagedItemProvider managedItemProvider) {
        this.managedItemProvider = managedItemProvider;
    }

    protected void setManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = managedThingProvider;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void unsetItemFactory(ItemFactory itemFactory) {
        this.itemFactory = null;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void unsetManagedItemChannelLinkProvider(ManagedItemChannelLinkProvider managedItemChannelLinkProvider) {
        this.managedItemChannelLinkProvider = null;
    }

    protected void unsetManagedItemProvider(ManagedItemProvider managedItemProvider) {
        this.managedItemProvider = null;
    }

    protected void unsetManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = null;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    private Set<EnrichedThingDTO> convertToListBean(Collection<Thing> things) {
        Set<EnrichedThingDTO> thingBeans = new LinkedHashSet<>();
        for (Thing thing : things) {
            EnrichedThingDTO thingBean = EnrichedThingDTOMapper.map(thing, uriInfo.getBaseUri());
            thingBeans.add(thingBean);
        }
        return thingBeans;
    }

    private Channel findChannel(String channelId, Thing thing) {
        for (Channel channel : thing.getChannels()) {
            if (channel.getUID().getId().equals(channelId)) {
                return channel;
            }
        }
        return null;
    }

    private void unlinkChannelIfAlreadyLinked(ChannelUID channelUID) {
        Collection<ItemChannelLink> links = managedItemChannelLinkProvider.getAll();
        for (ItemChannelLink link : links) {
            if (link.getUID().equals(channelUID)) {
                logger.debug(
                        "Channel '{}' is already linked to item '{}' and will be unlinked before it will be linked to the new item.",
                        channelUID, link.getItemName());
                managedItemChannelLinkProvider.remove(link.getID());
            }
        }
    }

    public static Configuration getConfiguration(ThingDTO thingBean) {
        Configuration configuration = new Configuration();

        Map<String, Object> convertDoublesToBigDecimal = ConfigUtil.normalizeTypes(thingBean.configuration);
        configuration.setProperties(convertDoublesToBigDecimal);

        return configuration;
    }

    public static void updateConfiguration(Thing thing, Configuration configuration) {
        for (String parameterName : configuration.keySet()) {
            thing.getConfiguration().put(parameterName, configuration.get(parameterName));
        }
    }

}
