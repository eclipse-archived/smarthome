/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

import static org.eclipse.smarthome.core.events.EventConstants.TOPIC_PREFIX;
import static org.eclipse.smarthome.core.events.EventConstants.TOPIC_SEPERATOR;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.EventType;
import org.eclipse.smarthome.core.types.State;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractEventSubscriber} is an abstract implementation of the {@link EventSubscriber}
 * event listener interface which belongs to the <i>Eclipse SmartHome</i> event bus.
 * <p>
 * This class is abstract and <i>must</i> be extended. It helps to easily extract the correct
 * objects out of an incoming event received by the event bus, and forwards incoming state updated
 * and command events to the according callback methods of the {@link EventSubscriber} interface.
 * <p>
 * To get notified about events, the concrete implementation of this class must be registered
 * as event listener with an according filter at the <i>Eclipse SmartHome</i> event bus.
 * Furthermore the method {@link #receiveUpdate(String, State)} and/or
 * {@link #receiveCommand(String, Command)} must be overridden.
 *
 * @see EventPublisher
 * @see EventSubscriber
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Michael Grammling - Javadoc extended, stability improved, Checkstyle compliance
 */
public abstract class AbstractEventSubscriber implements EventSubscriber, EventHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<String> sourceFilterList = new CopyOnWriteArrayList<String>();

    protected List<String> getSourceFilterList() {
    	return sourceFilterList;
    }
    
    public AbstractEventSubscriber() {
    	// to keep backward compatibility, we filter autoupdate events by default,
    	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=433368
    	sourceFilterList.add("org.eclipse.smarthome.core.autoupdate");
    }
    
    /**
     * {@inheritDoc}
     */
    public void handleEvent(Event event) {
        Object itemNameObj = event.getProperty("item");
        if (!(itemNameObj instanceof String)) {
            return;   // we have received an invalid item name
        }
        String itemName = (String) itemNameObj;
        if (itemName.isEmpty()) {
            return;   // we have received an empty item name
        }

        Object sourceObj = event.getProperty("source");
        if(sourceObj instanceof String) {
            String source = (String) sourceObj;
            if(sourceFilterList.contains(source)) {
            	// we are not supposed to process this event
            	return;
            }
        }
        
        String topic = event.getTopic();
        String[] topicParts = topic.split(TOPIC_SEPERATOR);
        if ((topicParts.length <= 2) || !TOPIC_PREFIX.equals(topicParts[0])) {
            return;   // we have received an event with an invalid topic
        }
        String operation = topicParts[1];

        if (EventType.UPDATE.toString().equals(operation)) {
            Object newStateObj = event.getProperty("state");
            if (newStateObj instanceof State) {
                State newState = (State) newStateObj;
                try {
                    receiveUpdate(itemName, newState);
                } catch (Exception ex) {
                    this.logger.error("An error occured within the 'receiveUpdate' method"
                            + " of the event subscriber!", ex);
                }
            }
        } else if (EventType.COMMAND.toString().equals(operation)) {
            Object commandObj = event.getProperty("command");
            if (commandObj instanceof Command) {
                Command command = (Command) commandObj;

                try {
                    receiveCommand(itemName, command);
                } catch (Exception ex) {
                    this.logger.error("An error occured within the 'receiveCommand' method"
                            + " of the event subscriber!", ex);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void receiveCommand(String itemName, Command command) {
        // default implementation: do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void receiveUpdate(String itemName, State newState) {
        // default implementation: do nothing
    }

}
