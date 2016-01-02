/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.builder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.automation.Trigger;

import com.google.common.base.Preconditions;

public class TriggerBuilder {

    private Map<String, Object> configurations = new HashMap<>();
    private String id;
    private String typeUID;

    public Trigger build() {
        validate();
        return new Trigger(id, typeUID, configurations);
    }

    private void validate() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(id), "id may not be empty");
        Preconditions.checkArgument(StringUtils.isNotEmpty(typeUID), "typeUID may not be empty");
    }

    public String getTypeUID() {
        return typeUID;
    }

    public TriggerBuilder setTypeUID(String typeUID) {
        this.typeUID = typeUID;
        return this;
    }

    public String getId() {
        return id;
    }

    public TriggerBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public TriggerBuilder setConfigurations(Map<String, Object> configs) {
        this.configurations.putAll(configs);
        return this;
    }

    public TriggerBuilder addConfiguration(String key, Object value) {
        this.configurations.put(key, value);
        return this;
    }
}
