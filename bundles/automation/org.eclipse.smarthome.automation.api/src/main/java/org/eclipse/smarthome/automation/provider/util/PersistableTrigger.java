/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.provider.util;

import java.util.Map;

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Trigger;

public class PersistableTrigger {

    public String id;
    public String typeUID;
    public String label;
    public String description;
    public Map<String, ?> configurations;

    /**
     * This constructor is used for deserialization of the {@link Trigger}s.
     */
    public PersistableTrigger() {
    }

    public PersistableTrigger(Trigger trigger) {
        id = trigger.getId();
        typeUID = trigger.getTypeUID();
        label = trigger.getLabel();
        description = trigger.getDescription();
        configurations = trigger.getConfiguration();
    }

    public Trigger createTrigger(AutomationFactory factory) {
        Trigger trigger = factory.createTrigger(id, typeUID, configurations);
        trigger.setDescription(description);
        trigger.setLabel(label);
        return trigger;
    }
}
