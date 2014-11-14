/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ThingStatus;

/**
 * This is a java bean that is used with JAXB to serialize things to XML or
 * JSON.
 * 
 * @author Dennis Nobel - Initial contribution
 *
 */
public class ThingBean {

    public String bridgeUID;
    public Map<String, Object> configuration;
    public ThingStatus status;
    public String UID;
    public List<ChannelBean> channels;

    public ThingBean() {
    }

    public ThingBean(String UID, String bridgeUID, ThingStatus status, List<ChannelBean> channels,
            Configuration configuration) {
        this.UID = UID;
        this.bridgeUID = bridgeUID;
        this.status = status;
        this.channels = channels;
        this.configuration = toMap(configuration);
    }

    private Map<String, Object> toMap(Configuration configuration) {

        if (configuration == null) {
            return null;
        }

        Map<String, Object> configurationMap = new HashMap<>(configuration.keySet().size());
        for (String key : configuration.keySet()) {
            configurationMap.put(key, configuration.get(key));
        }
        return configurationMap;
    }

}
