/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of an ActionHandler. It posts command events on
 * items to change their state.
 *
 * @author Benedikt Niehues - Initial contribution and API
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
public class ItemPostCommandActionHandler extends BaseModuleHandler<Action>implements ActionHandler {

    private final Logger logger = LoggerFactory.getLogger(ItemPostCommandActionHandler.class);

    public static final String ITEM_POST_COMMAND_ACTION = "ItemPostCommandAction";
    private static final String ITEM_NAME = "itemName";
    private static final String COMMAND = "command";

    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;

    /**
     * constructs a new ItemPostCommandActionHandler
     *
     * @param module
     * @param moduleTypes
     */
    public ItemPostCommandActionHandler(Action module) {
        super(module);
    }

    /**
     * setter for itemRegistry, used by DS
     *
     * @param itemRegistry
     */
    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    /**
     * unsetter for itemRegistry, used by DS
     *
     * @param itemRegistry
     */
    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    /**
     * setter for eventPublisher used by DS
     *
     * @param eventPublisher
     */
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * unsetter for eventPublisher used by DS
     *
     * @param eventPublisher
     */
    public void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Override
    public void dispose() {
        this.eventPublisher = null;
        this.itemRegistry = null;
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> inputs) {
        String itemName = (String) module.getConfiguration().get(ITEM_NAME);
        String command = (String) module.getConfiguration().get(COMMAND);
        if (itemName != null && command != null && eventPublisher != null && itemRegistry != null) {
            try {
                Item item = itemRegistry.getItem(itemName);
                Command commandObj = TypeParser.parseCommand(item.getAcceptedCommandTypes(), command);
                ItemCommandEvent itemCommandEvent = ItemEventFactory.createCommandEvent(itemName, commandObj);
                logger.debug("Executing ItemPostCommandAction on Item {} with Command {}",
                        itemCommandEvent.getItemName(), itemCommandEvent.getItemCommand());
                eventPublisher.post(itemCommandEvent);
            } catch (ItemNotFoundException e) {
                logger.error("Item with name {} not found in ItemRegistry.", itemName);
            }
        } else {
            logger.error(
                    "Command was not posted because either the configuration was not correct or a Service was missing: ItemName: {}, Command: {}, eventPublisher: {}, ItemRegistry: {}",
                    itemName, command, eventPublisher, itemRegistry);
        }
        return null;
    }

}
