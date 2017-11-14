/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

/**
 * {@link Event} objects are delivered by the {@link EventPublisher} through the Eclipse SmartHome event bus.
 * The callback interface {@link EventSubscriber} can be implemented in order to receive such events.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public interface Event {

    /**
     * Gets the event type.
     * 
     * @return the event type
     */
    String getType();

    /**
     * Gets the topic of an event.
     * 
     * @return the event topic
     */
    String getTopic();

    /**
     * Gets the payload as a serialized string.
     * 
     * @return the serialized event
     */
    String getPayload();

    /**
     * Gets the name of the source identifying the sender.
     * 
     * @return the name of the source
     */
    String getSource();

}
