/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.io.rest.core.item.beans.GroupItemBean;

/**
 * This is a java bean that is used with JAX-RS to serialize things to JSON.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Thomas Höfer - Added thing and thing type properties
 * @author Stefan Bußweiler - Added new thing status handling 
 *
 */
public class ThingBean {

    public String bridgeUID;
    public Map<String, Object> configuration;
    public Map<String, String> properties;
    public ThingStatusInfo statusInfo;
    public String UID;
    public List<ChannelBean> channels;
    public GroupItemBean item;

    public ThingBean() {
    }

    public ThingBean(String UID, String bridgeUID, ThingStatusInfo status, List<ChannelBean> channels,
            Configuration configuration, Map<String, String> properties, GroupItemBean item) {
        this.UID = UID;
        this.bridgeUID = bridgeUID;
        this.statusInfo = status;
        this.channels = channels;
        this.configuration = toMap(configuration);
        this.properties = properties;
        this.item = item;
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
