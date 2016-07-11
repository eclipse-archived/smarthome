/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution.
 */

package org.eclipse.smarthome.tools.docgenerator.models;

import org.eclipse.smarthome.tools.docgenerator.data.ParameterList;

public class ConfigDescription implements Model<org.eclipse.smarthome.tools.docgenerator.schemas.ConfigDescription> {
    /**
     * The original instance from the XML parser.
     */
    private org.eclipse.smarthome.tools.docgenerator.schemas.ConfigDescription delegtae;

    /**
     * Default constructor.
     */
    public ConfigDescription() {
    }

    /**
     * Constructor.
     *
     * @param delegtae The instance from the XML parser.
     */
    public ConfigDescription(org.eclipse.smarthome.tools.docgenerator.schemas.ConfigDescription delegtae) {
        this.delegtae = delegtae;
    }

    /**
     * @return The instance from the XML parser.
     */
    @Override
    public org.eclipse.smarthome.tools.docgenerator.schemas.ConfigDescription getRealImpl() {
        return delegtae;
    }

    /**
     * Set the model.
     *
     * @param config The instance from the XML parser.
     */
    @Override
    public void setModel(org.eclipse.smarthome.tools.docgenerator.schemas.ConfigDescription config) {
        this.delegtae = config;
    }

    /**
     * @return The URI of the configuration.
     */
    public String uri() {
        return delegtae.getUri();
    }

    /**
     * @return A list of parameters.
     */
    public ParameterList parameter() {
        ParameterList parameterList = new ParameterList();
        for (org.eclipse.smarthome.tools.docgenerator.schemas.Parameter param : delegtae.getParameter()) {
            parameterList.put(param);
        }
        return parameterList;
    }
}
