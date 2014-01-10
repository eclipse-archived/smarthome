/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschränkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.item.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.rest.RESTApplication;
import org.eclipse.smarthome.io.rest.ResourceStateChangeListener;
import org.eclipse.smarthome.io.rest.ResponseTypeHelper;
import org.eclipse.smarthome.io.rest.item.ItemResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the {@link ResourceStateChangeListener} implementation for item REST requests
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Oliver Mazur
 */
public class ItemStateChangeListener extends ResourceStateChangeListener {

	static final Logger logger = LoggerFactory.getLogger(ItemStateChangeListener.class);

	private Set<String> relevantItems = null;
	private StateChangeListener stateChangeListener;

	public void registerItems() {
		
		stateChangeListener = new StateChangeListener() {
			// don't react on update events
			public void stateUpdated(Item item, State state) {
				// if the group has a base item and thus might calculate its state
				// as a DecimalType or other, we also consider it to be necessary to
				// send an update to the client as the label of the item might have changed,
				// even though its state is yet the same.
				if(item instanceof GroupItem) {
					GroupItem gItem = (GroupItem) item;
					if(gItem.getBaseItem()!=null) {
					}
				}
			}
			
			public void stateChanged(final Item item, State oldState, State newState) {	
			}
		};
		// TODO: get pathInfo and register listeners
		// registerStateChangeListenerOnRelevantItems(pathInfo, stateChangeListener);
	}
	
	public void unregisterItems() {
		unregisterStateChangeListenerOnRelevantItems();
	}
    

	protected void registerStateChangeListenerOnRelevantItems(String pathInfo, StateChangeListener stateChangeListener ) {
		relevantItems = getRelevantItemNames(pathInfo);
		for(String itemName : relevantItems) {
			registerChangeListenerOnItem(stateChangeListener, itemName);
		}
	}

	protected void unregisterStateChangeListenerOnRelevantItems() {
		
		if(relevantItems!=null) {
			for(String itemName : relevantItems) {
				unregisterChangeListenerOnItem(stateChangeListener, itemName);
			}
		}
	}

	private void registerChangeListenerOnItem(
			StateChangeListener stateChangeListener, String itemName) {
		Item item = ItemResource.getItem(itemName);
		if (item instanceof GenericItem) {
			GenericItem genericItem = (GenericItem) item;
			genericItem.addStateChangeListener(stateChangeListener);
			
		}
	}

	private void unregisterChangeListenerOnItem(
			StateChangeListener stateChangeListener, String itemName) {
		Item item = ItemResource.getItem(itemName);
		if (item instanceof GenericItem) {
			GenericItem genericItem = (GenericItem) item;
			genericItem.removeStateChangeListener(stateChangeListener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object getResponseObject(HttpServletRequest request) {	
		String pathInfo = request.getPathInfo();

		if(pathInfo.endsWith("/state")) {
			// we need to return the plain value
			if (pathInfo.startsWith("/" + ItemResource.PATH_ITEMS)) {
	        	String[] pathSegments = pathInfo.substring(1).split("/");
	            if(pathSegments.length>=2) {
	            	String itemName = pathSegments[1];
					Item item = ItemResource.getItem(itemName);
					if(item!=null) {
						return item.getState().toString();
					}
	            }
			}
		} else {		
			// we want the full item data (as xml or json(p))
			String responseType = (new ResponseTypeHelper()).getResponseType(request);
			if(responseType!=null) {
				String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+(request.getContextPath().equals("null")?"":request.getContextPath())+ RESTApplication.REST_SERVLET_ALIAS +"/";
				if (pathInfo.startsWith("/" + ItemResource.PATH_ITEMS)) {
		        	String[] pathSegments = pathInfo.substring(1).split("/");
		            if(pathSegments.length>=2) {
		            	String itemName = pathSegments[1];
						Item item = ItemResource.getItem(itemName);
						if(item!=null) {
			            	Object itemBean = ItemResource.createItemBean(item, true, basePath);	    	
			            	return itemBean;
						}
		            }
		        }
			}
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object getSingleResponseObject(Item item, HttpServletRequest request) {
		return getResponseObject(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<String> getRelevantItemNames(String pathInfo) {       
        // check, if it is a request for items 
        if (pathInfo.startsWith("/" + ItemResource.PATH_ITEMS)) {
        	String[] pathSegments = pathInfo.substring(1).split("/");

            if(pathSegments.length>=2) {
            	return Collections.singleton(pathSegments[1]);
            }
        }
        return new HashSet<String>();
	}




}
