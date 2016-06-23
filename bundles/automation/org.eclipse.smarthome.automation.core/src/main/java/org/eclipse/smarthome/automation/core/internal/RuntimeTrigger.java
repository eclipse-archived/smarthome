/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This class is implementation of {@link Trigger} modules used in the {@link RuleEngine}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class RuntimeTrigger extends Trigger {

    private TriggerHandler triggerHandler;

    public RuntimeTrigger(String id, String typeUID, Configuration configuration) {
        super(id, typeUID, configuration);
    }

    public RuntimeTrigger(Trigger trigger) {
        super(trigger.getId(), trigger.getTypeUID(), trigger.getConfiguration());
        setLabel(trigger.getLabel());
        setDescription(trigger.getDescription());
    }

    /**
     * This method gets handler which is responsible for handling of this module.
     *
     * @return handler of the module or null.
     */
    TriggerHandler getModuleHandler() {
        return triggerHandler;
    }

    /**
     * This method sets handler of the module.
     *
     * @param triggerHandler
     */
    public void setModuleHandler(TriggerHandler triggerHandler) {
        this.triggerHandler = triggerHandler;
    }

}
