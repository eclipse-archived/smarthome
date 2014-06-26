/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkProvider;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinksChangeListener;
import org.eclipse.smarthome.model.item.BindingConfigParseException;
import org.eclipse.smarthome.model.item.BindingConfigReader;

/**
 * {@link GenericItemChannelLinkProvider} link items to channel by reading bindings with type "channel".
 * 
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class GenericItemChannelLinkProvider implements BindingConfigReader, ItemChannelLinkProvider {

	private List<ItemChannelLinksChangeListener> itemChannelLinksChangeListeners = new CopyOnWriteArrayList<>();
	
	/** caches binding configurations. maps itemNames to {@link BindingConfig}s */
	protected Map<String, ItemChannelLink> itemChannelLinkMap = new ConcurrentHashMap<>(new WeakHashMap<String, ItemChannelLink>());

	
	/** 
	 * stores information about the context of items. The map has this content
	 * structure: context -> Set of Item names
	 */ 
	protected Map<String, Set<String>> contextMap = new ConcurrentHashMap<>();
	
	@Override
	public String getBindingType() {
		return "channel";
	}

	@Override
	public void validateItemType(String itemType, String bindingConfig)
			throws BindingConfigParseException {
		// all item types are allowed
	}

	@Override
	public void processBindingConfiguration(String context, String itemType,
			String itemName, String bindingConfig)
			throws BindingConfigParseException {
		ChannelUID channelUID = null;
		try {
			channelUID = new ChannelUID(bindingConfig);
		} catch (IllegalArgumentException e) {
			throw new BindingConfigParseException(e.getMessage());
		}
		ItemChannelLink itemChannelLink = new ItemChannelLink(itemName, channelUID);
		
		
		Set<String> itemNames = contextMap.get(context);
		if (itemNames == null) {
			itemNames = new HashSet<>();
			contextMap.put(context, itemNames);
		}
		itemNames.add(itemName);
		ItemChannelLink oldItemChannelLink = itemChannelLinkMap.put(itemName, itemChannelLink);
		if (oldItemChannelLink == null) {
			notifyItemChannelLinksChangeListenerAboutAddedItemChannelLink(itemChannelLink);
		} else {
			notifyItemChannelLinksChangeListenerAboutUpdatedItemChannelLink(oldItemChannelLink, itemChannelLink);
		}
	}

	@Override
	public void removeConfigurations(String context) {
		Set<String> itemNames = contextMap.get(context);
		if(itemNames!=null) {
			for(String itemName : itemNames) {
				// we remove all binding configurations for all items
				ItemChannelLink removedItemChannelLink = itemChannelLinkMap.remove(itemName);
				notifyItemChannelLinksChangeListenerAboutRemovedItemChannelLink(removedItemChannelLink);
			}
			contextMap.remove(context);
		}
	}

	@Override
	public Collection<ItemChannelLink> getItemChannelLinks() {
		return itemChannelLinkMap.values();
	}

	@Override
	public void addItemChannelLinksChangeListener(
			ItemChannelLinksChangeListener listener) {
		itemChannelLinksChangeListeners.add(listener);
	}

	@Override
	public void removeItemChannelLinksChangeListener(
			ItemChannelLinksChangeListener listener) {
		itemChannelLinksChangeListeners.remove(listener);
	}
	
	protected void notifyItemChannelLinksChangeListenerAboutAddedItemChannelLink(ItemChannelLink itemChannelLink) {
	    for (ItemChannelLinksChangeListener itemChannelLinksChangeListener : this.itemChannelLinksChangeListeners) {
	    	itemChannelLinksChangeListener.itemChannelLinkAdded(this, itemChannelLink);
	    }
	}

	protected void notifyItemChannelLinksChangeListenerAboutRemovedItemChannelLink(ItemChannelLink itemChannelLink) {
	    for (ItemChannelLinksChangeListener itemChannelLinksChangeListener : this.itemChannelLinksChangeListeners) {
	    	itemChannelLinksChangeListener.itemChannelLinkRemoved(this, itemChannelLink);
	    }
	}
	
	protected void notifyItemChannelLinksChangeListenerAboutUpdatedItemChannelLink(ItemChannelLink oldItemChannelLink, ItemChannelLink newItemChannelLink) {
		for (ItemChannelLinksChangeListener itemChannelLinksChangeListener : this.itemChannelLinksChangeListeners) {
			itemChannelLinksChangeListener.itemChannelLinkUpdated(this, oldItemChannelLink, newItemChannelLink);
		}
	}
}
