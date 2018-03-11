/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.io.rest.core.internal.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.auth.Role;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ActiveItem;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.items.dto.GroupItemDTO;
import org.eclipse.smarthome.core.items.dto.ItemDTOMapper;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.io.rest.DTOMapper;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.Stream2JSONInputStream;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTO;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTOMapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * <p>
 * This class acts as a REST resource for items and provides different methods to interact with them, like retrieving
 * lists of items, sending commands to them or checking a single status.
 *
 * <p>
 * The typical content types are plain text for status values and XML or JSON(P) for more complex data structures
 *
 * <p>
 * This resource is registered with the Jersey servlet.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Dennis Nobel - Added methods for item management
 * @author Andre Fuechsel - Added tag support
 * @author Chris Jackson - Added method to write complete item bean
 * @author Stefan Bußweiler - Migration to new ESH event concept
 * @author Yordan Zhelev - Added Swagger annotations
 * @author Jörg Plewe - refactoring, error handling
 * @author Franck Dechavanne - Added DTOs to ApiResponses
 * @author Stefan Triller - Added bulk item add method
 */
@NonNullByDefault
@Path(ItemResource.PATH_ITEMS)
@Api(value = ItemResource.PATH_ITEMS)
@Component(service = { RESTResource.class, ItemResource.class })
public class ItemResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(ItemResource.class);

    /** The URI path to this resource */
    public static final String PATH_ITEMS = "items";

    @NonNullByDefault({})
    @Context
    UriInfo uriInfo;

    @NonNullByDefault({})
    private ItemRegistry itemRegistry;
    @NonNullByDefault({})
    private EventPublisher eventPublisher;
    @NonNullByDefault({})
    private ManagedItemProvider managedItemProvider;
    @NonNullByDefault({})
    private DTOMapper dtoMapper;
    private final Set<ItemFactory> itemFactories = new HashSet<>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setManagedItemProvider(ManagedItemProvider managedItemProvider) {
        this.managedItemProvider = managedItemProvider;
    }

    protected void unsetManagedItemProvider(ManagedItemProvider managedItemProvider) {
        this.managedItemProvider = null;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addItemFactory(ItemFactory itemFactory) {
        this.itemFactories.add(itemFactory);
    }

    protected void removeItemFactory(ItemFactory itemFactory) {
        this.itemFactories.remove(itemFactory);
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setDTOMapper(DTOMapper dtoMapper) {
        this.dtoMapper = dtoMapper;
    }

    protected void unsetDTOMapper(DTOMapper dtoMapper) {
        this.dtoMapper = dtoMapper;
    }

    @GET
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all available items.", response = EnrichedItemDTO.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = EnrichedItemDTO.class, responseContainer = "List") })
    public Response getItems(
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") @Nullable String language,
            @QueryParam("type") @ApiParam(value = "item type filter", required = false) @Nullable String type,
            @QueryParam("tags") @ApiParam(value = "item tag filter", required = false) @Nullable String tags,
            @DefaultValue("false") @QueryParam("recursive") @ApiParam(value = "get member items recursivly", required = false) boolean recursive,
            @QueryParam("fields") @ApiParam(value = "limit output to the given fields (comma separated)", required = false) @Nullable String fields) {
        final Locale locale = LocaleUtil.getLocale(language);
        logger.debug("Received HTTP GET request at '{}'", uriInfo.getPath());

        Stream<EnrichedItemDTO> itemStream = getItems(type, tags).stream()
                .map(item -> EnrichedItemDTOMapper.map(item, recursive, null, uriInfo.getBaseUri(), locale));
        itemStream = dtoMapper.limitToFields(itemStream, fields);
        return Response.ok(new Stream2JSONInputStream(itemStream)).build();
    }

    @GET
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/{itemname: [a-zA-Z_0-9]*}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Gets a single item.", response = EnrichedItemDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = EnrichedItemDTO.class),
            @ApiResponse(code = 404, message = "Item not found") })
    public Response getItemData(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language,
            @PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname) {
        final Locale locale = LocaleUtil.getLocale(language);
        logger.debug("Received HTTP GET request at '{}'", uriInfo.getPath());

        // get item
        Item item = getItem(itemname);

        // if it exists
        if (item != null) {
            logger.debug("Received HTTP GET request at '{}'.", uriInfo.getPath());
            return getItemResponse(Status.OK, item, locale, null);
        } else {
            logger.info("Received HTTP GET request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            return getItemNotFoundResponse(itemname);
        }
    }

    /**
     *
     * @param itemname
     * @return
     */
    @GET
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/{itemname: [a-zA-Z_0-9]*}/state")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Gets the state of an item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 404, message = "Item not found") })
    public Response getPlainItemState(
            @PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname) {
        // get item
        Item item = getItem(itemname);

        // if it exists
        if (item != null) {
            logger.debug("Received HTTP GET request at '{}'.", uriInfo.getPath());

            // we cannot use JSONResponse.createResponse() bc. MediaType.TEXT_PLAIN
            // return JSONResponse.createResponse(Status.OK, item.getState().toString(), null);
            return Response.ok(item.getState().toFullString()).build();
        } else {
            logger.info("Received HTTP GET request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            return getItemNotFoundResponse(itemname);
        }
    }

    @PUT
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/{itemname: [a-zA-Z_0-9]*}/state")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Updates the state of an item.")
    @ApiResponses(value = { @ApiResponse(code = 202, message = "Accepted"),
            @ApiResponse(code = 404, message = "Item not found"),
            @ApiResponse(code = 400, message = "Item state null") })
    public Response putItemState(
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language,
            @PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname,
            @ApiParam(value = "valid item state (e.g. ON, OFF)", required = true) String value) {
        final Locale locale = LocaleUtil.getLocale(language);

        // get Item
        Item item = getItem(itemname);

        // if Item exists
        if (item != null) {
            // try to parse a State from the input
            State state = TypeParser.parseState(item.getAcceptedDataTypes(), value);

            if (state != null) {
                // set State and report OK
                logger.debug("Received HTTP PUT request at '{}' with value '{}'.", uriInfo.getPath(), value);
                eventPublisher.post(ItemEventFactory.createStateEvent(itemname, state));
                return getItemResponse(Status.ACCEPTED, null, locale, null);
            } else {
                // State could not be parsed
                logger.warn("Received HTTP PUT request at '{}' with an invalid status value '{}'.", uriInfo.getPath(),
                        value);
                return JSONResponse.createErrorResponse(Status.BAD_REQUEST, "State could not be parsed: " + value);
            }
        } else {
            // Item does not exist
            logger.info("Received HTTP PUT request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            return getItemNotFoundResponse(itemname);
        }
    }

    @POST
    @RolesAllowed({ Role.USER, Role.ADMIN })
    @Path("/{itemname: [a-zA-Z_0-9]*}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Sends a command to an item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item not found"),
            @ApiResponse(code = 400, message = "Item command null") })
    public Response postItemCommand(
            @PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname,
            @ApiParam(value = "valid item command (e.g. ON, OFF, UP, DOWN, REFRESH)", required = true) String value) {
        Item item = getItem(itemname);
        Command command = null;
        if (item != null) {
            if ("toggle".equalsIgnoreCase(value) && (item instanceof SwitchItem || item instanceof RollershutterItem)) {
                if (OnOffType.ON.equals(item.getStateAs(OnOffType.class))) {
                    command = OnOffType.OFF;
                }
                if (OnOffType.OFF.equals(item.getStateAs(OnOffType.class))) {
                    command = OnOffType.ON;
                }
                if (UpDownType.UP.equals(item.getStateAs(UpDownType.class))) {
                    command = UpDownType.DOWN;
                }
                if (UpDownType.DOWN.equals(item.getStateAs(UpDownType.class))) {
                    command = UpDownType.UP;
                }
            } else {
                command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), value);
            }
            if (command != null) {
                logger.debug("Received HTTP POST request at '{}' with value '{}'.", uriInfo.getPath(), value);
                eventPublisher.post(ItemEventFactory.createCommandEvent(itemname, command));
                ResponseBuilder resbuilder = Response.ok();
                resbuilder.type(MediaType.TEXT_PLAIN);
                return resbuilder.build();
            } else {
                logger.warn("Received HTTP POST request at '{}' with an invalid status value '{}'.", uriInfo.getPath(),
                        value);
                return Response.status(Status.BAD_REQUEST).build();
            }
        } else {
            logger.info("Received HTTP POST request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            throw new WebApplicationException(404);
        }
    }

    @PUT
    @RolesAllowed({ Role.ADMIN })
    @Path("/{itemName: [a-zA-Z_0-9]*}/members/{memberItemName: [a-zA-Z_0-9]*}")
    @ApiOperation(value = "Adds a new member to a group item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item or member item not found or item is not of type group item."),
            @ApiResponse(code = 405, message = "Member item is not editable.") })
    public Response addMember(@PathParam("itemName") @ApiParam(value = "item name", required = true) String itemName,
            @PathParam("memberItemName") @ApiParam(value = "member item name", required = true) String memberItemName) {
        try {
            Item item = itemRegistry.getItem(itemName);

            if (!(item instanceof GroupItem)) {
                return Response.status(Status.NOT_FOUND).build();
            }

            GroupItem groupItem = (GroupItem) item;

            Item memberItem = itemRegistry.getItem(memberItemName);

            if (!(memberItem instanceof GenericItem)) {
                return Response.status(Status.NOT_FOUND).build();
            }

            if (managedItemProvider.get(memberItemName) == null) {
                return Response.status(Status.METHOD_NOT_ALLOWED).build();
            }

            GenericItem genericMemberItem = (GenericItem) memberItem;
            genericMemberItem.addGroupName(groupItem.getName());
            managedItemProvider.update(genericMemberItem);

            return Response.ok(null, MediaType.TEXT_PLAIN).build();
        } catch (ItemNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @RolesAllowed({ Role.ADMIN })
    @Path("/{itemName: [a-zA-Z_0-9]*}/members/{memberItemName: [a-zA-Z_0-9]*}")
    @ApiOperation(value = "Removes an existing member from a group item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item or member item not found or item is not of type group item."),
            @ApiResponse(code = 405, message = "Member item is not editable.") })
    public Response removeMember(@PathParam("itemName") @ApiParam(value = "item name", required = true) String itemName,
            @PathParam("memberItemName") @ApiParam(value = "member item name", required = true) String memberItemName) {
        try {
            Item item = itemRegistry.getItem(itemName);

            if (!(item instanceof GroupItem)) {
                return Response.status(Status.NOT_FOUND).build();
            }

            GroupItem groupItem = (GroupItem) item;

            Item memberItem = itemRegistry.getItem(memberItemName);

            if (!(memberItem instanceof GenericItem)) {
                return Response.status(Status.NOT_FOUND).build();
            }

            if (managedItemProvider.get(memberItemName) == null) {
                return Response.status(Status.METHOD_NOT_ALLOWED).build();
            }

            GenericItem genericMemberItem = (GenericItem) memberItem;
            genericMemberItem.removeGroupName(groupItem.getName());
            managedItemProvider.update(genericMemberItem);

            return Response.ok(null, MediaType.TEXT_PLAIN).build();
        } catch (ItemNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @RolesAllowed({ Role.ADMIN })
    @Path("/{itemname: [a-zA-Z_0-9]*}")
    @ApiOperation(value = "Removes an item from the registry.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item not found or item is not editable.") })
    public Response removeItem(@PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname) {
        if (managedItemProvider.remove(itemname) == null) {
            logger.info("Received HTTP DELETE request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(null, MediaType.TEXT_PLAIN).build();
    }

    @PUT
    @RolesAllowed({ Role.ADMIN })
    @Path("/{itemname: [a-zA-Z_0-9]*}/tags/{tag}")
    @ApiOperation(value = "Adds a tag to an item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item not found."),
            @ApiResponse(code = 405, message = "Item not editable.") })
    public Response addTag(@PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname,
            @PathParam("tag") @ApiParam(value = "tag", required = true) String tag) {
        Item item = getItem(itemname);

        if (item == null) {
            logger.info("Received HTTP PUT request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            return Response.status(Status.NOT_FOUND).build();
        }

        if (managedItemProvider.get(itemname) == null) {
            return Response.status(Status.METHOD_NOT_ALLOWED).build();
        }

        ((ActiveItem) item).addTag(tag);
        managedItemProvider.update(item);

        return Response.ok(null, MediaType.TEXT_PLAIN).build();
    }

    @DELETE
    @RolesAllowed({ Role.ADMIN })
    @Path("/{itemname: [a-zA-Z_0-9]*}/tags/{tag}")
    @ApiOperation(value = "Removes a tag from an item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item not found."),
            @ApiResponse(code = 405, message = "Item not editable.") })
    public Response removeTag(@PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname,
            @PathParam("tag") @ApiParam(value = "tag", required = true) String tag) {
        Item item = getItem(itemname);

        if (item == null) {
            logger.info("Received HTTP DELETE request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            return Response.status(Status.NOT_FOUND).build();
        }

        if (managedItemProvider.get(itemname) == null) {
            return Response.status(Status.METHOD_NOT_ALLOWED).build();
        }

        ((ActiveItem) item).removeTag(tag);
        managedItemProvider.update(item);

        return Response.ok(null, MediaType.TEXT_PLAIN).build();
    }

    /**
     * Create or Update an item by supplying an item bean.
     *
     * @param itemname
     * @param item the item bean.
     * @return
     */
    @PUT
    @RolesAllowed({ Role.ADMIN })
    @Path("/{itemname: [a-zA-Z_0-9]*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Adds a new item to the registry or updates the existing item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 201, message = "Item created."), @ApiResponse(code = 400, message = "Item null."),
            @ApiResponse(code = 404, message = "Item not found."),
            @ApiResponse(code = 405, message = "Item not editable.") })
    public Response createOrUpdateItem(
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language,
            @PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname,
            @ApiParam(value = "item data", required = true) GroupItemDTO item) {
        final Locale locale = LocaleUtil.getLocale(language);

        // If we didn't get an item bean, then return!
        if (item == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        ActiveItem newItem = createActiveItem(item);

        if (newItem == null) {
            logger.warn("Received HTTP PUT request at '{}' with an invalid item type '{}'.", uriInfo.getPath(),
                    item.type);
            return Response.status(Status.BAD_REQUEST).build();
        }

        // Save the item
        if (getItem(itemname) == null) {
            // item does not yet exist, create it
            managedItemProvider.add(newItem);
            return getItemResponse(Status.CREATED, newItem, locale, null);
        } else if (managedItemProvider.get(itemname) != null) {
            // item already exists as a managed item, update it
            managedItemProvider.update(newItem);
            return getItemResponse(Status.OK, newItem, locale, null);
        } else {
            // Item exists but cannot be updated
            logger.warn("Cannot update existing item '{}', because is not managed.", itemname);
            return JSONResponse.createErrorResponse(Status.METHOD_NOT_ALLOWED,
                    "Cannot update non-managed Item " + itemname);
        }
    }

    /**
     * Create or Update a list of items by supplying a list of item beans.
     *
     * @param items the list of item beans.
     * @return array of status information for each item bean
     */
    @PUT
    @RolesAllowed({ Role.ADMIN })
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Adds a list of items to the registry or updates the existing items.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 400, message = "Item list is null.") })
    public Response createOrUpdateItems(@ApiParam(value = "array of item data", required = true) GroupItemDTO[] items) {
        // If we didn't get an item list bean, then return!
        if (items == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        List<GroupItemDTO> wrongTypes = new ArrayList<>();
        List<ActiveItem> activeItems = new ArrayList<>();

        for (GroupItemDTO item : items) {
            ActiveItem newItem = createActiveItem(item);
            if (newItem == null) {
                wrongTypes.add(item);
            } else {
                activeItems.add(newItem);
            }
        }

        List<ActiveItem> createdItems = new ArrayList<>();
        List<ActiveItem> updatedItems = new ArrayList<>();
        List<ActiveItem> failedItems = new ArrayList<>();

        for (ActiveItem activeItem : activeItems) {
            String itemName = activeItem.getName();
            if (getItem(itemName) == null) {
                // item does not yet exist, create it
                managedItemProvider.add(activeItem);
                createdItems.add(activeItem);
            } else if (managedItemProvider.get(itemName) != null) {
                // item already exists as a managed item, update it
                managedItemProvider.update(activeItem);
                updatedItems.add(activeItem);
            } else {
                // Item exists but cannot be updated
                logger.warn("Cannot update existing item '{}', because it is not managed.", itemName);
                failedItems.add(activeItem);
            }
        }

        // build response
        List<JsonObject> responseList = new ArrayList<>();

        for (GroupItemDTO item : wrongTypes) {
            responseList.add(buildStatusObject(item.name, "error", "Received HTTP PUT request at '" + uriInfo.getPath()
                    + "' with an invalid item type '" + item.type + "'."));
        }
        for (ActiveItem item : failedItems) {
            responseList.add(buildStatusObject(item.getName(), "error", "Cannot update non-managed item"));
        }
        for (ActiveItem item : createdItems) {
            responseList.add(buildStatusObject(item.getName(), "created", null));
        }
        for (ActiveItem item : updatedItems) {
            responseList.add(buildStatusObject(item.getName(), "updated", null));
        }

        return JSONResponse.createResponse(Status.OK, responseList, null);
    }

    private @Nullable ActiveItem createActiveItem(GroupItemDTO item) {
        ActiveItem activeItem = ItemDTOMapper.map(item, itemFactories);
        if (activeItem != null) {
            activeItem.setLabel(item.label);
            if (item.category != null) {
                activeItem.setCategory(item.category);
            }
            if (item.groupNames != null) {
                activeItem.addGroupNames(item.groupNames);
            }
            if (item.tags != null) {
                activeItem.addTags(item.tags);
            }
        }
        return activeItem;
    }

    private JsonObject buildStatusObject(String itemName, String status, @Nullable String message) {
        JsonObject jo = new JsonObject();
        jo.addProperty("name", itemName);
        jo.addProperty("status", status);
        jo.addProperty("message", message);
        return jo;
    }

    /**
     * helper: Response to be sent to client if a Thing cannot be found
     *
     * @param thingUID
     * @return Response configured for 'item not found'
     */
    private static Response getItemNotFoundResponse(String itemname) {
        String message = "Item " + itemname + " does not exist!";
        return JSONResponse.createResponse(Status.NOT_FOUND, null, message);
    }

    /**
     * Prepare a response representing the Item depending in the status.
     *
     * @param status
     * @param item can be null
     * @param locale the locale
     * @param errormessage optional message in case of error
     * @return Response configured to represent the Item in depending on the status
     */
    private Response getItemResponse(Status status, @Nullable Item item, Locale locale, @Nullable String errormessage) {
        Object entity = null != item ? EnrichedItemDTOMapper.map(item, true, null, uriInfo.getBaseUri(), locale) : null;
        return JSONResponse.createResponse(status, entity, errormessage);
    }

    /**
     * convenience shortcut
     *
     * @param itemname
     * @return Item addressed by itemname
     */
    private @Nullable Item getItem(String itemname) {
        return itemRegistry.get(itemname);
    }

    private Collection<Item> getItems(@Nullable String type, @Nullable String tags) {
        Collection<Item> items;
        if (tags == null) {
            if (type == null) {
                items = itemRegistry.getItems();
            } else {
                items = itemRegistry.getItemsOfType(type);
            }
        } else {
            String[] tagList = tags.split(",");
            if (type == null) {
                items = itemRegistry.getItemsByTag(tagList);
            } else {
                items = itemRegistry.getItemsByTagAndType(type, tagList);
            }
        }

        return items;
    }

    @Override
    public boolean isSatisfied() {
        return itemRegistry != null && managedItemProvider != null && eventPublisher != null && !itemFactories.isEmpty()
                && dtoMapper != null;
    }
}
