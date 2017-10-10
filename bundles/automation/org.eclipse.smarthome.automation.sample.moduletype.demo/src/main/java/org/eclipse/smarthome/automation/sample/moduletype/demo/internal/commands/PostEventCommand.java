/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Plamen Peev - Bosch Software Innovations GmbH - Please refer to git log
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.sample.moduletype.demo.internal.commands;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * This class implements a command that posts {@link Event} via the {@link EventAdmin} service.
 *
 * <pre>
 * Example:
 *   <b>atmdemo postEvent test/demo/topic test 10</b>
 *
 * The event will be with topic: test/demo/topic
 * and entry with key: test and value 10
 * </pre>
 * 
 * @author Plamen Peev - Initial contribution
 */
public class PostEventCommand extends DemoCommand {

    /**
     * This field contains the name of the command.
     */
    protected final static String POST_EVENT = "postEvent";
    /**
     * This field contains a alias for the command.
     */
    protected final static String POST_EVENT_SHORT = "pe";

    /**
     * Template for the usage of the command.
     */
    protected final static String SYNTAX = POST_EVENT + " <topic> <key> <value>.";

    /**
     * Contains the description of the command.
     */
    protected final static String DESCRIPTION = "Posts event with 'topic' that contains entry <'key', 'value'>";

    /**
     * Contains the position of the topic into the incoming {@link String[]}
     */
    private final static int TOPIC_POSITION = 0;
    /**
     * Contains the position of the entry's key into the incoming {@link String[]}
     */
    private final static int KEY_POSITION = 1;

    /**
     * Contains the position of the entry's value into the incoming {@link String[]}
     */
    private final static int VALUE_POSITION = 2;

    /**
     * If parsing of the command is successful this field will contain a reference the event that the command posts.
     */
    private Event event;

    public PostEventCommand(String[] params) {
        super(params);
    }

    @Override
    public String execute() {
        if (event == null) {
            return parsingResult;
        } else if (DemoCommandsPluggable.eventAdmin != null) {
            DemoCommandsPluggable.eventAdmin.postEvent(event);
            return SUCCESS;
        } else {
            return FAIL;
        }
    }

    @Override
    protected void parseOptionsAndParameters(String[] params) {
        if (params.length != 3) {
            parsingResult = "ERROR: Usage: <topic> <key> <value>";
        } else {
            try {
                Integer value = Integer.valueOf(params[VALUE_POSITION]);
                final Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(params[KEY_POSITION], value);
                event = new Event(params[TOPIC_POSITION], properties);
            } catch (NumberFormatException e) {
                parsingResult = "The value parameter must be an Integer.";
            }
        }
    }
}
