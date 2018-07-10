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
package org.eclipse.smarthome.automation;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * Trigger modules are used in the 'ON' section of {@link Rule} definition. They defines what fires the {@link Rule}
 * (what starts execution of the {@link Rule}). The triggers don't have {@link Input} elements. They only have:
 * {@link ConfigDescriptionParameter}s and {@link Output}s defined by {@link TriggerType}.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Markus Rathgeb - Remove interface and implementation split
 */
public class Trigger extends Module {

    private transient @Nullable TriggerHandler triggerHandler;

    // Gson
    Trigger() {
    }

    public Trigger(String id, String typeUID, Configuration configuration, @Nullable String label,
            @Nullable String description) {
        super(id, typeUID, configuration, label, description);
    }

    /**
     * This method gets handler which is responsible for handling of this module.
     *
     * @return handler of the module or null.
     */
    public @Nullable TriggerHandler getModuleHandler() {
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
