/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;

public class ConfigDescriptionParameterBean {

    public String context;
    public String defaultValue;
    public String description;
    public String label;
    public String name;
    public boolean required;
    public Type type;

    public ConfigDescriptionParameterBean() {
    }

    public ConfigDescriptionParameterBean(String name, Type type, String context, boolean required,
            String defaultValue, String label, String description) {
        this.name = name;
        this.type = type;
        this.context = context;
        this.required = required;
        this.defaultValue = defaultValue;
        this.label = label;
        this.description = description;
    }

}
