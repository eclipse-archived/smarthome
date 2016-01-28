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
import org.eclipse.smarthome.automation.Condition;

import com.google.common.base.Preconditions;

public class ConditionBuilder {

    private Map<String, Object> configurations = new HashMap<>();
    private Map<String, String> inputs = new HashMap<>();
    private String id;
    private String typeUID;

    public ConditionBuilder addInput(String key, String value) {
        this.inputs.put(key, value);
        return this;
    }

    public Condition build() {
        validate();
        return new Condition(id, typeUID, configurations, inputs);
    }

    private void validate() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(id), "id may not be empty");
        Preconditions.checkArgument(StringUtils.isNotEmpty(typeUID), "typeUID may not be empty");
        Preconditions.checkArgument(!inputs.isEmpty(), "inputs may not be empty");
    }

    public String getTypeUID() {
        return typeUID;
    }

    public ConditionBuilder setTypeUID(String typeUID) {
        this.typeUID = typeUID;
        return this;
    }

    public String getId() {
        return id;
    }

    public ConditionBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public ConditionBuilder addConfiguration(String key, Object value) {
        this.configurations.put(key, value);
        return this;
    }
}
