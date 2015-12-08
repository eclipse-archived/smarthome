/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.events;

import org.eclipse.smarthome.automation.RuleStatusInfo;
import org.eclipse.smarthome.core.events.AbstractEvent;

/**
 * An {@link RuleStatusInfoEvent} notifies subscribers that an item has been
 * added. Rule added events must be created with the {@link RuleEventFactory}.
 *
 * @author Benedikt Niehues - initial contribution
 * @author Kai Kreuzer - added toString method
 *
 */
public class RuleStatusInfoEvent extends AbstractEvent {

    public static final String TYPE = RuleStatusInfoEvent.class.getSimpleName();

    private RuleStatusInfo statusInfo;
    private String ruleId;

    /**
     * constructs a new rule status event
     *
     * @param topic
     * @param payload
     * @param source
     * @param ruleDTO
     */
    public RuleStatusInfoEvent(String topic, String payload, String source, RuleStatusInfo statusInfo, String ruleId) {
        super(topic, payload, source);
        this.statusInfo = statusInfo;
        this.ruleId = ruleId;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * @return the statusInfo
     */
    public RuleStatusInfo getStatusInfo() {
        return statusInfo;
    }

    /**
     * @return the ruleId
     */
    public String getRuleId() {
        return ruleId;
    }

    @Override
    public String toString() {
        return ruleId + " updated: " + statusInfo.toString();
    }

}
