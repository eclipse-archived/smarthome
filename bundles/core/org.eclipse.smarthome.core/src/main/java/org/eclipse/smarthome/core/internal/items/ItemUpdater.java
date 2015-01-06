/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.items;

import org.eclipse.smarthome.core.events.AbstractEventSubscriber;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ItemUpdater listens on the event bus and passes any received status update
 * to the item registry.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ItemUpdater extends AbstractEventSubscriber {

    public ItemUpdater() {
        super();
		// remove the filtering of the autoupdate events
        getSourceFilterList().clear();
    }

	private final Logger logger = LoggerFactory.getLogger(ItemUpdater.class);
	
	protected ItemRegistry itemRegistry;
	
	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
	}

	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receiveUpdate(String itemName, State newStatus) {
		if (itemRegistry != null) {
			try {
				GenericItem item = (GenericItem) itemRegistry.getItem(itemName);
				boolean isAccepted = false;
				if (item.getAcceptedDataTypes().contains(newStatus.getClass())) {
					isAccepted = true;
				} else {
					// Look for class hierarchy
					for (Class<? extends State> state : item.getAcceptedDataTypes()) {
						try {
							if (!state.isEnum() && state.newInstance().getClass().isAssignableFrom(newStatus.getClass())) {
								isAccepted = true;
								break;
							}
						} catch (InstantiationException e) {
							logger.warn("InstantiationException on {}", e.getMessage()); // Should never happen
						} catch (IllegalAccessException e) {
							logger.warn("IllegalAccessException on {}", e.getMessage()); // Should never happen
						}
					}
				}				
				if (isAccepted) {
					item.setState(newStatus);
				} else {
					logger.debug("Received update of a not accepted type ("	+ newStatus.getClass().getSimpleName() + ") for item " + itemName);
				}
			} catch (ItemNotFoundException e) {
				logger.debug("Received update for non-existing item: {}", e.getMessage());
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receiveCommand(String itemName, Command command) {	
		// if the item is a group, we have to pass the command to it as it needs to pass the command to its members
		if(itemRegistry!=null) {
			try {
				Item item = itemRegistry.getItem(itemName);
				if (item instanceof GroupItem) {
					GroupItem groupItem = (GroupItem) item;
					groupItem.send(command);
				}
			} catch (ItemNotFoundException e) {
				logger.debug("Received command for non-existing item: {}", e.getMessage());
			}
		}
	}

}
