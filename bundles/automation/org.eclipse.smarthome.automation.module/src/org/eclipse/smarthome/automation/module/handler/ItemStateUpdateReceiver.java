/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.handler;

import org.eclipse.smarthome.core.items.events.AbstractItemEventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an EventSubscriber implementation used for
 * ItemStateChangeTriggerHandler
 * 
 * @author Benedikt Niehues
 *
 */
public class ItemStateUpdateReceiver extends AbstractItemEventSubscriber {

	private static Logger logger = LoggerFactory.getLogger(ItemStateUpdateReceiver.class);
	private ItemStateChangeTriggerHandler trigger;

	public ItemStateUpdateReceiver(ItemStateChangeTriggerHandler trigger) {
		super();
		this.trigger = trigger;
	}

	@Override
	protected void receiveUpdate(ItemStateEvent event) {
		logger.debug("update received for " + event.getItemName());
		trigger.updateReceived(event);
	}

}
