
/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.documentation.models;

import org.eclipse.smarthome.documentation.data.ParameterList;

public class ConfigDescription implements Model<org.eclipse.smarthome.documentation.schemas.config_description.v1_0.ConfigDescription> {
    /**
     * The original instance from the XML parser.
     */
    protected org.eclipse.smarthome.documentation.schemas.config_description.v1_0.ConfigDescription config;

    /**
     * Default constructor.
     */
    public ConfigDescription() {
    }

    /**
     * Constructor.
     *
     * @param config The instance from the XML parser.
     */
    public ConfigDescription(org.eclipse.smarthome.documentation.schemas.config_description.v1_0.ConfigDescription config) {
        setModel(config);
    }

    /**
     * @return The instance from the XML parser.
     */
    public org.eclipse.smarthome.documentation.schemas.config_description.v1_0.ConfigDescription getRealImpl() {
        return config;
    }

    /**
     * Set the model.
     *
     * @param config The instance from the XML parser.
     */
    public void setModel(org.eclipse.smarthome.documentation.schemas.config_description.v1_0.ConfigDescription config) {
        this.config = config;
    }

    /**
     * @return The URI of the configuration.
     */
    public String uri() {
        return config.getUri();
    }

    /**
     * @return A list of parameters.
     */
    public ParameterList parameter() {
        ParameterList parameterList = new ParameterList();
        for (org.eclipse.smarthome.documentation.schemas.config_description.v1_0.Parameter param : config.getParameter()) {
            parameterList.put(param);
        }
        return parameterList;
    }
}
