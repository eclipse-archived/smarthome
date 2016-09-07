/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.events;

import org.eclipse.smarthome.automation.dto.RuleDTO;
import org.eclipse.smarthome.core.events.AbstractEvent;

/**
 * abstract class for rule events
 *
 * @author Benedikt Niehues - initial contribution
 * @author Markus Rathgeb - Use the DTO for the Rule representation
 *
 */
public abstract class AbstractRuleRegistryEvent extends AbstractEvent {

    private final RuleDTO rule;

    /**
     * Must be called in subclass constructor to create a new rule registry event.
     *
     * @param topic
     * @param payload
     * @param source
     * @param ruleDTO
     */
    public AbstractRuleRegistryEvent(String topic, String payload, String source, RuleDTO rule) {
        super(topic, payload, source);
        this.rule = rule;
    }

    /**
     * returns the RuleDTO which caused the Event
     *
     * @return
     */
    public RuleDTO getRule() {
        return this.rule;
    }

}
