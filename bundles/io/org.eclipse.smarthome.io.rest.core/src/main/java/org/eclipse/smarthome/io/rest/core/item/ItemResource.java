/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.item;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ActiveItem;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class acts as a REST resource for items and provides different methods to interact with them, like retrieving
 * lists of items, sending commands to them or checking a single status.
 * </p>
 *
 * <p>
 * The typical content types are plain text for status values and XML or JSON(P) for more complex data structures
 * </p>
 *
 * <p>
 * This resource is registered with the Jersey servlet.
 * </p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Dennis Nobel - Added methods for item management
 * @author Andre Fuechsel - Added tag support
 * @author Chris Jackson - Added method to write complete item bean
 * @author Stefan Bußweiler - Migration to new ESH event concept
 * @author Yordan Zhelev - Added Swagger annotations
 *
 */
@Path(ItemResource.PATH_ITEMS)
@Api
public class ItemResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(ItemResource.class);

    /** The URI path to this resource */
    public static final String PATH_ITEMS = "items";

    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;
    private ManagedItemProvider managedItemProvider;
    private Set<ItemFactory> itemFactories = new HashSet<>();

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void setManagedItemProvider(ManagedItemProvider managedItemProvider) {
        this.managedItemProvider = managedItemProvider;
    }

    protected void unsetManagedItemProvider(ManagedItemProvider managedItemProvider) {
        this.managedItemProvider = null;
    }

    protected void addItemFactory(ItemFactory itemFactory) {
        this.itemFactories.add(itemFactory);
    }

    protected void removeItemFactory(ItemFactory itemFactory) {
        this.itemFactories.remove(itemFactory);
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all available items.", response = EnrichedItemDTO.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getItems(
            @QueryParam("type") @ApiParam(value = "item type filter", required = false) String type,
            @QueryParam("tags") @ApiParam(value = "item tag filter", required = false) String tags,
            @DefaultValue("false") @QueryParam("recursive") @ApiParam(value = "get member items recursivly", required = false) boolean recursive) {
        logger.debug("Received HTTP GET request at '{}'", uriInfo.getPath());

        Object responseObject = getItemBeans(type, tags, recursive);
        return Response.ok(responseObject).build();
    }

    @GET
    @Path("/{itemname: [a-zA-Z_0-9]*}/state")
    @Produces({ MediaType.TEXT_PLAIN })
    @ApiOperation(value = "Gets the state of an item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item not found") })
    public Response getPlainItemState(
            @PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname) {
        Item item = getItem(itemname);
        if (item != null) {
            logger.debug("Received HTTP GET request at '{}'.", uriInfo.getPath());
            throw new WebApplicationException(Response.ok(item.getState().toString()).build());
        } else {
            logger.info("Received HTTP GET request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            throw new WebApplicationException(404);
        }
    }

    @GET
    @Path("/{itemname: [a-zA-Z_0-9]*}")
    @Produces({ MediaType.WILDCARD })
    @ApiOperation(value = "Gets a single item.", response = EnrichedItemDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item not found") })
    public Response getItemData(@PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname) {
        logger.debug("Received HTTP GET request at '{}'", uriInfo.getPath());

        final Object responseObject = getItemDataBean(itemname);
        throw new WebApplicationException(Response.ok(responseObject).build());
    }

    @PUT
    @Path("/{itemname: [a-zA-Z_0-9]*}/state")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Updates the state of an item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item not found"), @ApiResponse(code = 400, message = "Item state null") })
    public Response putItemState(
            @PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname,
            @ApiParam(value = "valid item state (e.g. ON, OFF)", required = true) String value) {
        Item item = getItem(itemname);
        if (item != null) {
            State state = TypeParser.parseState(item.getAcceptedDataTypes(), value);
            if (state != null) {
                logger.debug("Received HTTP PUT request at '{}' with value '{}'.", uriInfo.getPath(), value);
                eventPublisher.post(ItemEventFactory.createStateEvent(itemname, state));
                return Response.ok().build();
            } else {
                logger.warn("Received HTTP PUT request at '{}' with an invalid status value '{}'.", uriInfo.getPath(),
                        value);
                return Response.status(Status.BAD_REQUEST).build();
            }
        } else {
            logger.info("Received HTTP PUT request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            throw new WebApplicationException(404);
        }
    }

    @Context
    UriInfo localUriInfo;

    @POST
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
                if (OnOffType.ON.equals(item.getStateAs(OnOffType.class)))
                    command = OnOffType.OFF;
                if (OnOffType.OFF.equals(item.getStateAs(OnOffType.class)))
                    command = OnOffType.ON;
                if (UpDownType.UP.equals(item.getStateAs(UpDownType.class)))
                    command = UpDownType.DOWN;
                if (UpDownType.DOWN.equals(item.getStateAs(UpDownType.class)))
                    command = UpDownType.UP;
            } else {
                command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), value);
            }
            if (command != null) {
                logger.debug("Received HTTP POST request at '{}' with value '{}'.", uriInfo.getPath(), value);
                eventPublisher.post(ItemEventFactory.createCommandEvent(itemname, command));
                return Response.created(localUriInfo.getAbsolutePathBuilder().path("state").build()).build();
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

            return Response.ok().build();
        } catch (ItemNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{itemName: [a-zA-Z_0-9]*}/members/{memberItemName: [a-zA-Z_0-9]*}")
    @ApiOperation(value = "Removes an existing member from a group item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item or member item not found or item is not of type group item."),
            @ApiResponse(code = 405, message = "Member item is not editable.") })
    public Response removeMember(
            @PathParam("itemName") @ApiParam(value = "item name", required = true) String itemName,
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

            return Response.ok().build();
        } catch (ItemNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{itemname: [a-zA-Z_0-9]*}")
    @ApiOperation(value = "Removes an item from the registry.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Item not found or item is not editable.") })
    public Response removeItem(@PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname) {

        if (managedItemProvider.remove(itemname) == null) {
            logger.info("Received HTTP DELETE request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok().build();
    }

    @PUT
    @Path("/{itemname: [a-zA-Z_0-9]*}/tags/{tag: [a-zA-Z_0-9]*}")
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

        return Response.ok().build();
    }

    @DELETE
    @Path("/{itemname: [a-zA-Z_0-9]*}/tags/{tag: [a-zA-Z_0-9]*}")
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

        return Response.ok().build();
    }

    /**
     * Create or Update an item by supplying an item bean.
     * 
     * @param itemname
     * @param item the item bean.
     * @return
     */
    @PUT
    @Path("/{itemname: [a-zA-Z_0-9]*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Adds a new item to the registry or updates the existing item.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 400, message = "Item null."),
            @ApiResponse(code = 404, message = "Item not found."),
            @ApiResponse(code = 405, message = "Item not editable.") })
    public Response createOrUpdateItem(
            @PathParam("itemname") @ApiParam(value = "item name", required = true) String itemname,
            @ApiParam(value = "item data", required = true) ItemDTO item) {

        // If we didn't get an item bean, then return!
        if (item == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        GenericItem newItem = null;

        if (item.type != null && item.type.equals("GroupItem")) {
            newItem = new GroupItem(itemname);
        } else {
            String itemType = item.type.substring(0, item.type.length() - 4);
            for (ItemFactory itemFactory : itemFactories) {
                newItem = itemFactory.createItem(itemType, itemname);
                if (newItem != null) {
                    break;
                }
            }
        }

        if (newItem == null) {
            logger.warn("Received HTTP PUT request at '{}' with an invalid item type '{}'.", uriInfo.getPath(),
                    item.type);
            return Response.status(Status.BAD_REQUEST).build();
        }

        // See if an existing item of this name exists.
        Item existingItem = getItem(itemname);

        // Update the label
        newItem.setLabel(item.label);
        newItem.setCategory(item.category);
        newItem.addGroupNames(item.groupNames);
        newItem.addTags(item.tags);

        // Save the item
        if (existingItem == null) {
            managedItemProvider.add(newItem);
        } else if (managedItemProvider.get(itemname) != null) {
            managedItemProvider.update(newItem);
        } else {
            logger.warn("Cannot update existing item '{}', because is not managed.", itemname);
            return Response.status(Status.METHOD_NOT_ALLOWED).build();
        }

        return Response.ok().build();
    }

    private Item getItem(String itemname) {
        try {
            Item item = itemRegistry.getItem(itemname);
            return item;
        } catch (ItemNotFoundException ignored) {
        }
        return null;
    }

    private List<EnrichedItemDTO> getItemBeans(String type, String tags, boolean recursive) {
        List<EnrichedItemDTO> beans = new LinkedList<>();
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
        if (items != null) {
            for (Item item : items) {
                beans.add(EnrichedItemDTOMapper.map(item, recursive, uriInfo.getBaseUri()));
            }
        }
        return beans;
    }

    private EnrichedItemDTO getItemDataBean(String itemname) {
        Item item = getItem(itemname);
        if (item != null) {
            return EnrichedItemDTOMapper.map(item, true, uriInfo.getBaseUri());
        } else {
            logger.info("Received HTTP GET request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            throw new WebApplicationException(404);
        }
    }
}
