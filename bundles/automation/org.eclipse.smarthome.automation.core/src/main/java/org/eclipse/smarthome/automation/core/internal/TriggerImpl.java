/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.core.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This class is implementation of {@link Trigger} modules used in the {@link RuleEngineImpl}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
@NonNullByDefault
public class TriggerImpl extends ModuleImpl implements Trigger {

    @Nullable
    private TriggerHandler triggerHandler;

    public TriggerImpl() {
    }

    public TriggerImpl(String id, String typeUID, Configuration configuration) {
        super(id, typeUID, configuration);
    }

    public TriggerImpl(Trigger trigger) {
        super(trigger.getId(), trigger.getTypeUID(), trigger.getConfiguration());
        setLabel(trigger.getLabel());
        setDescription(trigger.getDescription());
    }

    /**
     * This method gets handler which is responsible for handling of this module.
     *
     * @return handler of the module or null.
     */
    @Nullable
    TriggerHandler getModuleHandler() {
        return triggerHandler;
    }

    /**
     * This method sets handler of the module.
     *
     * @param triggerHandler
     */
    public void setModuleHandler(@Nullable TriggerHandler triggerHandler) {
        this.triggerHandler = triggerHandler;
    }

}
