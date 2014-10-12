/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.item;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.io.rest.AbstractRESTResource;
import org.eclipse.smarthome.io.rest.MediaTypeHelper;
import org.eclipse.smarthome.io.rest.RESTApplication;
import org.eclipse.smarthome.io.rest.core.item.beans.GroupItemBean;
import org.eclipse.smarthome.io.rest.core.item.beans.ItemBean;
import org.eclipse.smarthome.io.rest.core.item.beans.ItemListBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.json.JSONWithPadding;

/**
 * <p>This class acts as a REST resource for items and provides different methods to interact with them,
 * like retrieving lists of items, sending commands to them or checking a single status.</p>
 * 
 * <p>The typical content types are plain text for status values and XML or JSON(P) for more complex data
 * structures</p>
 * 
 * <p>This resource is registered with the Jersey servlet.</p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Dennis Nobel - Added methods for item management
 */
@Path(ItemResource.PATH_ITEMS)
public class ItemResource extends AbstractRESTResource {

	private static final Logger logger = LoggerFactory.getLogger(ItemResource.class); 
	
	/** The URI path to this resource */
    public static final String PATH_ITEMS = "items";
    
	@Context UriInfo uriInfo;
	@GET
    @Produces( { MediaType.WILDCARD })
    public Response getItems(
    		@Context HttpHeaders headers,
    		@QueryParam("type") String type, 
    		@QueryParam("jsoncallback") @DefaultValue("callback") String callback) {
		logger.debug("Received HTTP GET request at '{}' for media type '{}'.", new Object[] { uriInfo.getPath(), type });

		String responseType = MediaTypeHelper.getResponseMediaType(headers.getAcceptableMediaTypes(), type);
		if(responseType!=null) {
	    	Object responseObject = responseType.equals(MediaTypeHelper.APPLICATION_X_JAVASCRIPT) ?
	    			new JSONWithPadding(new ItemListBean(getItemBeans()), callback) : new ItemListBean(getItemBeans());
	    	return Response.ok(responseObject, responseType).build();
		} else {
			return Response.notAcceptable(null).build();
		}
    }

    @GET @Path("/{itemname: [a-zA-Z_0-9]*}/state") 
	@Produces( { MediaType.TEXT_PLAIN })
    public Response getPlainItemState(
		@PathParam("itemname") String itemname) {
    	Item item = getItem(itemname);
    	if(item!=null) {
			logger.debug("Received HTTP GET request at '{}'.", uriInfo.getPath());
			throw new WebApplicationException(Response.ok(item.getState().toString()).build());
    	} else {
    		logger.info("Received HTTP GET request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
    		throw new WebApplicationException(404);
    	}
    }

    @GET @Path("/{itemname: [a-zA-Z_0-9]*}")
    @Produces( { MediaType.WILDCARD })
    public Response  getItemData(
    		@Context HttpHeaders headers,
    		@PathParam("itemname") String itemname, 
    		@QueryParam("type") String type, 
    		@QueryParam("jsoncallback") @DefaultValue("callback") String callback) {
		logger.debug("Received HTTP GET request at '{}' for media type '{}'.", new Object[] { uriInfo.getPath(), type });

		final String responseType = MediaTypeHelper.getResponseMediaType(headers.getAcceptableMediaTypes(), type);
		if(responseType!=null) {
	    	final Object responseObject = responseType.equals(MediaTypeHelper.APPLICATION_X_JAVASCRIPT) ?
	    			new JSONWithPadding(getItemDataBean(itemname), callback) : getItemDataBean(itemname);
	    	throw new WebApplicationException(Response.ok(responseObject, responseType).build());  
	
		} else {
			throw new WebApplicationException(Response.notAcceptable(null).build());
		}
    }
    
    @PUT @Path("/{itemname: [a-zA-Z_0-9]*}/state")
	@Consumes(MediaType.TEXT_PLAIN)	
	public Response putItemState(@PathParam("itemname") String itemname, String value) {
    	Item item = getItem(itemname);
    	if(item!=null) {
    		State state = TypeParser.parseState(item.getAcceptedDataTypes(), value);
    		if(state!=null) {
    			logger.debug("Received HTTP PUT request at '{}' with value '{}'.", uriInfo.getPath(), value);
    			RESTApplication.getEventPublisher().postUpdate(itemname, state);
    			return Response.ok().build();
    		} else {
    			logger.warn("Received HTTP PUT request at '{}' with an invalid status value '{}'.", uriInfo.getPath(), value);
    			return Response.status(Status.BAD_REQUEST).build();
    		}
    	} else {
    		logger.info("Received HTTP PUT request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
    		throw new WebApplicationException(404);
    	}
	}

	@Context UriInfo localUriInfo;
    @POST @Path("/{itemname: [a-zA-Z_0-9]*}")
	@Consumes(MediaType.TEXT_PLAIN)	
	public Response postItemCommand(@PathParam("itemname") String itemname, String value) {
    	Item item = getItem(itemname);
    	Command command = null;
    	if(item!=null) {
    		if("toggle".equalsIgnoreCase(value) && 
    				(item instanceof SwitchItem || 
    				 item instanceof RollershutterItem)) {
    			if(OnOffType.ON.equals(item.getStateAs(OnOffType.class))) command = OnOffType.OFF;
    			if(OnOffType.OFF.equals(item.getStateAs(OnOffType.class))) command = OnOffType.ON;
    			if(UpDownType.UP.equals(item.getStateAs(UpDownType.class))) command = UpDownType.DOWN;
    			if(UpDownType.DOWN.equals(item.getStateAs(UpDownType.class))) command = UpDownType.UP;
    		} else {
    			command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), value);
    		}
    		if(command!=null) {
    			logger.debug("Received HTTP POST request at '{}' with value '{}'.", uriInfo.getPath(), value);
    			RESTApplication.getEventPublisher().postCommand(itemname, command);
    			return Response.created(localUriInfo.getAbsolutePathBuilder().path("state").build()).build();
    		} else {
    			logger.warn("Received HTTP POST request at '{}' with an invalid status value '{}'.", uriInfo.getPath(), value);
    			return Response.status(Status.BAD_REQUEST).build();
    		}
    	} else {
    		logger.info("Received HTTP POST request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
    		throw new WebApplicationException(404);
    	}
	}
    
    @PUT @Path("/{itemname: [a-zA-Z_0-9]*}")
	@Consumes(MediaType.TEXT_PLAIN)	
	public Response createItem(@PathParam("itemname") String itemname, String itemType) {
    	
        ManagedItemProvider managedItemProvider = getService(ManagedItemProvider.class);
        ItemFactory itemFactory = getService(ItemFactory.class);

        GenericItem newItem = itemFactory.createItem(itemType, itemname);
        if (newItem == null) {
            logger.warn("Received HTTP PUT request at '{}' with an invalid item type '{}'.", uriInfo.getPath(),
                    itemType);
            return Response.status(Status.BAD_REQUEST).build();
        }

        Item existingItem = getItem(itemname);

        if (existingItem != null) {
            managedItemProvider.update(newItem);
        } else {
            managedItemProvider.add(newItem);
        }

        return Response.ok().build();
	}
    
    @DELETE
    @Path("/{itemname: [a-zA-Z_0-9]*}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response removeItem(@PathParam("itemname") String itemname) {

        ManagedItemProvider managedItemProvider = getService(ManagedItemProvider.class);

        if (managedItemProvider.remove(itemname) == null) {
            logger.info("Received HTTP DELETE request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
            throw new WebApplicationException(404);
        }

        return Response.ok().build();
    }

    public static ItemBean createItemBean(Item item, boolean drillDown, String uriPath) {
    	ItemBean bean;
    	if(item instanceof GroupItem && drillDown) {
    		GroupItem groupItem = (GroupItem) item;
    		GroupItemBean groupBean = new GroupItemBean();
    		Collection<ItemBean> members = new HashSet<ItemBean>();
    		for(Item member : groupItem.getMembers()) {
    			members.add(createItemBean(member, false, uriPath));
    		}
    		groupBean.members = members.toArray(new ItemBean[members.size()]);
    		bean = groupBean;
    	} else {
    		 bean = new ItemBean();
    	}
    	bean.name = item.getName();
    	bean.state = item.getState().toString();
    	bean.type = item.getClass().getSimpleName();
    	bean.link = UriBuilder.fromUri(uriPath).path(ItemResource.PATH_ITEMS).path(bean.name).build().toASCIIString();
    	
    	return bean;
    }
    
    static public Item getItem(String itemname) {
        ItemRegistry registry = RESTApplication.getItemRegistry();
        if(registry!=null) {
        	try {
				Item item = registry.getItem(itemname);
				return item;
			} catch (ItemNotFoundException e) {
				logger.debug(e.getMessage());
			}
        }
        return null;
    }

	private List<ItemBean> getItemBeans() {
		List<ItemBean> beans = new LinkedList<ItemBean>();
		ItemRegistry registry = RESTApplication.getItemRegistry();
		for(Item item : registry.getItems()) {
			beans.add(createItemBean(item, false, uriInfo.getBaseUri().toASCIIString()));
		}
		return beans;
	}

	private ItemBean getItemDataBean(String itemname) {
		Item item = getItem(itemname);
		if(item!=null) {
			return createItemBean(item, true, uriInfo.getBaseUri().toASCIIString());
		} else {
			logger.info("Received HTTP GET request at '{}' for the unknown item '{}'.", uriInfo.getPath(), itemname);
			throw new WebApplicationException(404);
		}
	}
}
