/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.events;

import org.eclipse.smarthome.automation.dto.RuleDTO;

/**
 * An {@link RuleAddedEvent} notifies subscribers that an item has been added.
 * Rule added events must be created with the {@link RuleEventFactory}.
 *
 * @author Benedikt Niehues - initial contribution
 *
 */
public class RuleAddedEvent extends AbstractRuleRegistryEvent {

    public static final String TYPE = RuleAddedEvent.class.getSimpleName();

    /**
     * constructs a new rule added event
     *
     * @param topic the topic of the event
     * @param payload the payload of the event
     * @param source the source of the event
     * @param ruleDTO the rule for which this event is created
     */
    public RuleAddedEvent(String topic, String payload, String source, RuleDTO rule) {
        super(topic, payload, source, rule);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "Rule '" + getRule().uid + "' has been added.";
    }

}
