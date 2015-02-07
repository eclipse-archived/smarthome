/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

/**
 * The {@link EventConstants} interface defines constants used by the <i>Eclipse SmartHome</i>
 * event bus. Events can be received and sent by using the {@link EventPublisher} service.
 *
 * @see EventPublisher
 * @see EventSubscriber
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Michael Grammling - Javadoc extended, Checkstyle compliancy
 */
public interface EventConstants {

    /**
     * The constant defining the topic prefix (name-space) for events associated with
     * <i>Eclipse SmartHome</i>. A topic is the name under which events are sent and
     * under which listeners can subscribe to.
     * <p>
     * Example: {@code smarthome/command/<item-name>}
     *
     * @see #TOPIC_SEPERATOR
     */
    String TOPIC_PREFIX = "smarthome";

    /**
     * The constant defining the separator for sub-topics. Each event of <i>Eclipse SmartHome</i>
     * is sent under the topic prefix (name-space) defined in {@link #TOPIC_PREFIX} and under
     * a specific topic name.
     * <p>
     * Example: {@code smarthome/command/<item-name>}
     *
     * @see #TOPIC_PREFIX
     */
    String TOPIC_SEPERATOR = "/";

}
