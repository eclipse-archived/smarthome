/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
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

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.ConfigUtil;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusInfo;
import org.eclipse.smarthome.config.core.status.ConfigStatusService;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.auth.Role;
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.dto.ChannelDTO;
import org.eclipse.smarthome.core.thing.dto.ChannelDTOMapper;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.core.thing.dto.ThingDTOMapper;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.core.thing.util.ThingHelper;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.SatisfiableRESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class acts as a REST resource for things and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector and
 *         refactored create and update methods
 * @author Thomas Höfer - added validation of configuration
 * @author Yordan Zhelev - Added Swagger annotations
 * @author Jörg Plewe - refactoring, error handling
 * @author Chris Jackson - added channel configuration updates
 *         return empty set for config/status if no status available
 */
@Path(ThingResource.PATH_THINGS)
@Api(value = ThingResource.PATH_THINGS)
public class ThingResource implements SatisfiableRESTResource {

    private final Logger logger = LoggerFactory.getLogger(ThingResource.class);

    /** The URI path to this resource */
    public static final String PATH_THINGS = "things";

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ItemFactory itemFactory;
    private ItemRegistry itemRegistry;
    private ManagedItemChannelLinkProvider managedItemChannelLinkProvider;
    private ManagedItemProvider managedItemProvider;
    private ManagedThingProvider managedThingProvider;
    private ThingRegistry thingRegistry;
    private ConfigStatusService configStatusService;
    private ConfigDescriptionRegistry configDescRegistry;
    private ThingTypeRegistry thingTypeRegistry;

    @Context
    private UriInfo uriInfo;

    /**
     * create a new Thing
     *
     * @param thingBean
     * @return Response holding the newly created Thing or error information
     */
    @POST
    @RolesAllowed({ Role.ADMIN })
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new thing and adds it to the registry.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "A uid must be provided, if no binding can create a thing of this type."),
            @ApiResponse(code = 409, message = "A thing with the same uid already exists.") })
    public Response create(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language,
            @ApiParam(value = "thing data", required = true) ThingDTO thingBean) {
        final Locale locale = LocaleUtil.getLocale(language);

        ThingUID thingUID = thingBean.UID == null ? null : new ThingUID(thingBean.UID);
        ThingTypeUID thingTypeUID = new ThingTypeUID(thingBean.thingTypeUID);

        if (thingUID != null) {
            // check if a thing with this UID already exists
            Thing thing = thingRegistry.get(thingUID);
            if (thing != null) {
                // report a conflict
                return getThingResponse(Status.CONFLICT, thing, locale,
                        "Thing " + thingUID.toString() + " already exists!");
            }
        }

        ThingUID bridgeUID = null;

        if (thingBean.bridgeUID != null) {
            bridgeUID = new ThingUID(thingBean.bridgeUID);
        }

        // turn the ThingDTO's configuration into a Configuration
        Configuration configuration = new Configuration(normalizeConfiguration(thingBean.configuration, thingTypeUID));

        Thing thing = thingRegistry.createThingOfType(thingTypeUID, thingUID, bridgeUID, thingBean.label,
                configuration);

        if (thing != null) {
            if (thingBean.properties != null) {
                for (Entry<String, String> entry : thingBean.properties.entrySet()) {
                    thing.setProperty(entry.getKey(), entry.getValue());
                }
            }
            if (thingBean.channels != null) {
                List<Channel> channels = new ArrayList<>();
                for (ChannelDTO channelDTO : thingBean.channels) {
                    channels.add(ChannelDTOMapper.map(channelDTO));
                }
                ThingHelper.addChannelsToThing(thing, channels);
            }
            if (thingBean.location != null) {
                thing.setLocation(thingBean.location);
            }
        } else if (thingUID != null) {
            // if there wasn't any ThingFactory capable of creating the thing,
            // we create the Thing exactly the way we received it, i.e. we
            // cannot take its thing type into account for automatically
            // populating channels and properties.
            thing = ThingDTOMapper.map(thingBean);
        } else {
            return getThingResponse(Status.BAD_REQUEST, thing, locale,
                    "A UID must be provided, since no binding can create the thing!");
        }

        thingRegistry.add(thing);
        return getThingResponse(Status.CREATED, thing, locale, null);
    }

    @GET
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all available things.", response = EnrichedThingDTO.class, responseContainer = "Set")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getAll(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language) {
        final Locale locale = LocaleUtil.getLocale(language);

        Collection<Thing> things = thingRegistry.getAll();
        Set<EnrichedThingDTO> thingBeans = convertToListBean(things, locale);
        return Response.ok(thingBeans).build();
    }

    @GET
    @RolesAllowed({ Role.ADMIN })
    @Path("/{thingUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets thing by UID.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Thing with provided thingUID does not exist.") })
    public Response getByUID(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language,
            @PathParam("thingUID") @ApiParam(value = "thingUID") String thingUID) {
        final Locale locale = LocaleUtil.getLocale(language);

        Thing thing = thingRegistry.get((new ThingUID(thingUID)));

        // return Thing data if it does exist
        if (thing != null) {
            return getThingResponse(Status.OK, thing, locale, null);
        } else {
            return getThingNotFoundResponse(thingUID);
        }
    }

    /**
     * link a Channel of a Thing to an Item
     *
     * @param thingUID
     * @param channelId
     * @param itemName
     * @return Response with status/error information
     */
    @POST
    @RolesAllowed({ Role.ADMIN })
    @Path("/{thingUID}/channels/{channelId}/link")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Links item to a channel. Creates item if such does not exist yet.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Thing not found or channel not found") })
    public Response link(@PathParam("thingUID") @ApiParam(value = "thingUID") String thingUID,
            @PathParam("channelId") @ApiParam(value = "channelId") String channelId,
            @ApiParam(value = "item name") String itemName) {

        Thing thing = thingRegistry.get(new ThingUID(thingUID));
        if (thing == null) {
            logger.warn("Received HTTP POST request at '{}' for the unknown thing '{}'.", uriInfo.getPath(), thingUID);
            return getThingNotFoundResponse(thingUID);
        }

        Channel channel = findChannel(channelId, thing);
        if (channel == null) {
            logger.info("Received HTTP POST request at '{}' for the unknown channel '{}' of the thing '{}'",
                    uriInfo.getPath(), channel, thingUID);
            String message = "Channel " + channelId + " for Thing " + thingUID + " does not exist!";
            return JSONResponse.createResponse(Status.NOT_FOUND, null, message);
        }
        if (channel.getKind() != ChannelKind.STATE) {
            logger.info("Tried to link channel '{}' of thing '{}', which is not of kind 'state'", channel, thingUID);
            String message = "Channel " + channelId + " for Thing " + thingUID
                    + " is not linkable, as it is not of kind 'state'!";
            return JSONResponse.createResponse(Status.FORBIDDEN, null, message);
        }

        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException ex) {
            GenericItem item = itemFactory.createItem(channel.getAcceptedItemType(), itemName);
            managedItemProvider.add(item);
        }

        ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);

        unlinkChannelIfAlreadyLinked(channelUID);

        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, channelUID));

        return Response.ok().build();
    }

    /**
     * Delete a Thing, if possible. Thing deletion might be impossible if the
     * Thing is not managed, will return CONFLICT. Thing deletion might happen
     * delayed, will return ACCEPTED.
     *
     * @param thingUID
     * @param force
     * @return Response with status/error information
     */
    @DELETE
    @RolesAllowed({ Role.ADMIN })
    @Path("/{thingUID}")
    @ApiOperation(value = "Removes a thing from the registry. Set \'force\' to __true__ if you want the thing te be removed immediately.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Thing not found.") })
    public Response remove(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language,
            @PathParam("thingUID") @ApiParam(value = "thingUID") String thingUID,
            @DefaultValue("false") @QueryParam("force") @ApiParam(value = "force") boolean force) {
        final Locale locale = LocaleUtil.getLocale(language);

        ThingUID thingUIDObject = new ThingUID(thingUID);

        // check whether thing exists and throw 404 if not
        Thing thing = thingRegistry.get(thingUIDObject);
        if (thing == null) {
            logger.info("Received HTTP DELETE request for update at '{}' for the unknown thing '{}'.",
                    uriInfo.getPath(), thingUID);
            return getThingNotFoundResponse(thingUID);
        }

        // ask whether the Thing exists as a managed thing, so it can get
        // updated, 409 otherwise
        Thing managed = managedThingProvider.get(thingUIDObject);
        if (null == managed) {
            logger.info("Received HTTP DELETE request for update at '{}' for an unmanaged thing '{}'.",
                    uriInfo.getPath(), thingUID);
            return getThingResponse(Status.CONFLICT, thing, locale,
                    "Cannot delete Thing " + thingUID + ". Maybe it is not managed.");
        }

        // only move on if Thing is known to be managed, so it can get updated
        if (force) {
            if (null == thingRegistry.forceRemove(thingUIDObject)) {
                return getThingResponse(Status.INTERNAL_SERVER_ERROR, thing, locale,
                        "Cannot delete Thing " + thingUID + " for unknown reasons.");
            }
        } else {
            if (null != thingRegistry.remove(thingUIDObject)) {
                return getThingResponse(Status.ACCEPTED, thing, locale, null);
            }
        }

        return Response.ok().build();
    }

    /**
     * Unlink a Channel of a Thing from an Item.
     *
     * @param thingUID
     * @param channelId
     * @param itemName
     * @return Response with status/error information
     */
    @DELETE
    @RolesAllowed({ Role.ADMIN })
    @Path("/{thingUID}/channels/{channelId}/link")
    @ApiOperation(value = "Unlinks item from a channel.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response unlink(@PathParam("thingUID") @ApiParam(value = "thingUID") String thingUID,
            @PathParam("channelId") @ApiParam(value = "channelId") String channelId,
            @ApiParam(value = "channelId") String itemName) {

        Thing thing = thingRegistry.get(new ThingUID(thingUID));
        if (thing == null) {
            logger.warn("Received HTTP POST request at '{}' for the unknown thing '{}'.", uriInfo.getPath(), thingUID);
            return getThingNotFoundResponse(thingUID);
        }

        ChannelUID channelUID = new ChannelUID(new ThingUID(thingUID), channelId);

        if (itemChannelLinkRegistry.isLinked(itemName, channelUID)) {
            managedItemChannelLinkProvider.remove(new ItemChannelLink(itemName, channelUID).getID());
        }

        return Response.ok().build();
    }

    /**
     * Update Thing.
     *
     * @param thingUID
     * @param thingBean
     * @return Response with the updated Thing or error information
     * @throws IOException
     */
    @PUT
    @RolesAllowed({ Role.ADMIN })
    @Path("/{thingUID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a thing.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Thing not found") })
    public Response update(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language,
            @PathParam("thingUID") @ApiParam(value = "thingUID") String thingUID,
            @ApiParam(value = "thing", required = true) ThingDTO thingBean) throws IOException {
        final Locale locale = LocaleUtil.getLocale(language);

        ThingUID thingUIDObject = new ThingUID(thingUID);

        // ask whether the Thing exists at all, 404 otherwise
        Thing thing = thingRegistry.get(thingUIDObject);
        if (null == thing) {
            logger.info("Received HTTP PUT request for update at '{}' for the unknown thing '{}'.", uriInfo.getPath(),
                    thingUID);
            return getThingNotFoundResponse(thingUID);
        }

        // ask whether the Thing exists as a managed thing, so it can get
        // updated, 409 otherwise
        Thing managed = managedThingProvider.get(thingUIDObject);
        if (null == managed) {
            logger.info("Received HTTP PUT request for update at '{}' for an unmanaged thing '{}'.", uriInfo.getPath(),
                    thingUID);
            return getThingResponse(Status.CONFLICT, thing, locale,
                    "Cannot update Thing " + thingUID + ". Maybe it is not managed.");
        }

        // check configuration
        thingBean.configuration = normalizeConfiguration(thingBean.configuration, thing.getThingTypeUID());

        thing = ThingHelper.merge(thing, thingBean);

        // update, returns null in case Thing cannot be found
        Thing oldthing = managedThingProvider.update(thing);
        if (null == oldthing) {
            return getThingNotFoundResponse(thingUID);
        }

        // everything went well
        return getThingResponse(Status.OK, thing, locale, null);
    }

    /**
     * Updates Thing configuration.
     *
     * @param thingUID
     * @param configurationParameters
     * @return Response with the updated Thing or error information
     * @throws IOException
     */
    @PUT
    @RolesAllowed({ Role.ADMIN })
    @Path("/{thingUID}/config")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates thing's configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Thing not found") })
    public Response updateConfiguration(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) String language,
            @PathParam("thingUID") @ApiParam(value = "thing") String thingUID,
            @ApiParam(value = "configuration parameters") Map<String, Object> configurationParameters)
            throws IOException {
        final Locale locale = LocaleUtil.getLocale(language);

        ThingUID thingUIDObject = new ThingUID(thingUID);

        // ask whether the Thing exists at all, 404 otherwise
        Thing thing = thingRegistry.get(thingUIDObject);
        if (null == thing) {
            logger.info("Received HTTP PUT request for update configuration at '{}' for the unknown thing '{}'.",
                    uriInfo.getPath(), thingUID);
            return getThingNotFoundResponse(thingUID);
        }

        // ask whether the Thing exists as a managed thing, so it can get
        // updated, 409 otherwise
        Thing managed = managedThingProvider.get(thingUIDObject);
        if (null == managed) {
            logger.info("Received HTTP PUT request for update configuration at '{}' for an unmanaged thing '{}'.",
                    uriInfo.getPath(), thingUID);
            return getThingResponse(Status.CONFLICT, thing, locale,
                    "Cannot update Thing " + thingUID + ". Maybe it is not managed.");
        }

        // only move on if Thing is known to be managed, so it can get updated
        try {
            // note that we create a Configuration instance here in order to
            // have normalized types
            thingRegistry.updateConfiguration(thingUIDObject,
                    new Configuration(normalizeConfiguration(configurationParameters, thing.getThingTypeUID()))
                            .getProperties());
        } catch (ConfigValidationException ex) {
            logger.debug("Config description validation exception occured for thingUID {} - Messages: {}", thingUID,
                    ex.getValidationMessages());
            return Response.status(Status.BAD_REQUEST).entity(ex.getValidationMessages(locale)).build();
        } catch (IllegalArgumentException ex) {
            logger.info("Received HTTP PUT request for update config at '{}' for the unknown thing '{}'.",
                    uriInfo.getPath(), thingUID);
            return getThingNotFoundResponse(thingUID);
        }

        return getThingResponse(Status.OK, thing, locale, null);
    }

    @GET
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/{thingUID}/config/status")
    @ApiOperation(value = "Gets thing's config status.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Thing not found.") })
    public Response getConfigStatus(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) String language,
            @PathParam("thingUID") @ApiParam(value = "thing") String thingUID) throws IOException {
        ThingUID thingUIDObject = new ThingUID(thingUID);

        // Check if the Thing exists, 404 if not
        Thing thing = thingRegistry.get(thingUIDObject);
        if (null == thing) {
            logger.info("Received HTTP GET request for thing config status at '{}' for the unknown thing '{}'.",
                    uriInfo.getPath(), thingUID);
            return getThingNotFoundResponse(thingUID);
        }

        ConfigStatusInfo info = configStatusService.getConfigStatus(thingUID, LocaleUtil.getLocale(language));
        if (info != null) {
            return Response.ok().entity(info.getConfigStatusMessages()).build();
        }
        return Response.ok().entity(Collections.EMPTY_SET).build();
    }

    /**
     * helper: Response to be sent to client if a Thing cannot be found
     *
     * @param thingUID
     * @return Response configured for NOT_FOUND
     */
    private static Response getThingNotFoundResponse(String thingUID) {
        String message = "Thing " + thingUID + " does not exist!";
        return JSONResponse.createResponse(Status.NOT_FOUND, null, message);
    }

    /**
     * helper: create a Response holding a Thing and/or error information.
     *
     * @param status
     * @param thing
     * @param errormessage an optional error message (may be null), ignored if the status family is successful
     * @return Response
     */
    private Response getThingResponse(Status status, Thing thing, Locale locale, String errormessage) {
        Object entity = null != thing
                ? EnrichedThingDTOMapper.map(thing, uriInfo.getBaseUri(), locale, getLinkedItemsMap(thing)) : null;
        return JSONResponse.createResponse(status, entity, errormessage);
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

    protected void setConfigStatusService(ConfigStatusService configStatusService) {
        this.configStatusService = configStatusService;
    }

    protected void unsetConfigStatusService(ConfigStatusService configStatusService) {
        this.configStatusService = null;
    }

    private Set<EnrichedThingDTO> convertToListBean(Collection<Thing> things, Locale locale) {
        Set<EnrichedThingDTO> thingBeans = new LinkedHashSet<>();
        for (Thing thing : things) {
            EnrichedThingDTO thingBean = EnrichedThingDTOMapper.map(thing, uriInfo.getBaseUri(), locale,
                    getLinkedItemsMap(thing));
            thingBeans.add(thingBean);
        }
        return thingBeans;
    }

    private Map<String, Set<String>> getLinkedItemsMap(Thing thing) {
        Map<String, Set<String>> linkedItemsMap = new HashMap<>();
        for (Channel channel : thing.getChannels()) {
            Set<String> linkedItems = itemChannelLinkRegistry.getLinkedItemNames(channel.getUID());
            linkedItemsMap.put(channel.getUID().getId(), linkedItems);
        }
        return linkedItemsMap;
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

    public static void updateConfiguration(Thing thing, Configuration configuration) {
        for (String parameterName : configuration.keySet()) {
            thing.getConfiguration().put(parameterName, configuration.get(parameterName));
        }
    }

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescRegistry = null;
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    private Map<String, Object> normalizeConfiguration(Map<String, Object> properties, ThingTypeUID thingTypeUID) {
        if (properties == null || properties.isEmpty()) {
            return properties;
        }

        ThingType thingType = thingTypeRegistry.getThingType(thingTypeUID);
        if (thingType == null) {
            return properties;
        }

        ConfigDescription configDesc = configDescRegistry.getConfigDescription(thingType.getConfigDescriptionURI());
        if (configDesc == null) {
            return properties;
        }

        return ConfigUtil.normalizeTypes(properties, configDesc);
    }

    @Override
    public boolean isSatisfied() {
        return itemChannelLinkRegistry != null && itemFactory != null && itemRegistry != null
                && managedItemChannelLinkProvider != null && managedItemProvider != null && managedThingProvider != null
                && thingRegistry != null && configStatusService != null && configDescRegistry != null
                && thingTypeRegistry != null;

    }

}
