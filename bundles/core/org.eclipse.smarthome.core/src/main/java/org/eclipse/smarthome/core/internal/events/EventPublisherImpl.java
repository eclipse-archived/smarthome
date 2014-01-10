/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschränkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.events;

import static org.eclipse.smarthome.core.events.EventConstants.TOPIC_PREFIX;
import static org.eclipse.smarthome.core.events.EventConstants.TOPIC_SEPERATOR;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.EventType;
import org.eclipse.smarthome.core.types.State;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main implementation of the {@link EventPublisher} interface.
 * Through it, openHAB events can be sent to the OSGi EventAdmin service
 * in order to broadcast them.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class EventPublisherImpl implements EventPublisher {

	private static final Logger logger = 
		LoggerFactory.getLogger(EventPublisherImpl.class);
		
	private EventAdmin eventAdmin;
	
	
	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	public void unsetEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = null;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.smarthome.core.internal.events.EventPublisher#sendCommand(org.eclipse.smarthome.core.items.GenericItem, org.eclipse.smarthome.core.datatypes.DataType)
	 */
	public void sendCommand(String itemName, Command command) {
		if (command != null) {
			if(eventAdmin!=null) eventAdmin.sendEvent(createCommandEvent(itemName, command));
		} else {
			logger.warn("given command is NULL, couldn't send command to '{}'", itemName);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.smarthome.core.internal.events.EventPublisher#postCommand(org.eclipse.smarthome.core.items.GenericItem, org.eclipse.smarthome.core.datatypes.DataType)
	 */
	public void postCommand(String itemName, Command command) {
		if (command != null) {
			if(eventAdmin!=null) eventAdmin.postEvent(createCommandEvent(itemName, command));
		} else {
			logger.warn("given command is NULL, couldn't post command to '{}'", itemName);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.smarthome.core.internal.events.EventPublisher#postUpdate(org.eclipse.smarthome.core.items.GenericItem, org.eclipse.smarthome.core.datatypes.DataType)
	 */
	public void postUpdate(String itemName, State newState) {
		if (newState != null) {
			if(eventAdmin!=null) eventAdmin.postEvent(createUpdateEvent(itemName, newState));
		} else {
			logger.warn("given new state is NULL, couldn't post update for '{}'", itemName);
		}
	}
	
	private Event createUpdateEvent(String itemName, State newState) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("item", itemName);
		properties.put("state", newState);
		return new Event(createTopic(EventType.UPDATE, itemName), properties);
	}

	private Event createCommandEvent(String itemName, Command command) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("item", itemName);
		properties.put("command", command);
		return new Event(createTopic(EventType.COMMAND, itemName) , properties);
	}

	private String createTopic(EventType type, String itemName) {
		return TOPIC_PREFIX + TOPIC_SEPERATOR + type + TOPIC_SEPERATOR + itemName;
	}
	
	
}
