/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.discovery.beans;

import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;

/**
 * This is a java bean that is used to serialize discovery results to JSON.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class DiscoveryResultBean {

    public String bridgeUID;
    public DiscoveryResultFlag flag;
    public String label;
    public Map<String, Object> properties;
    public String thingUID;

    public DiscoveryResultBean() {
    }

    public DiscoveryResultBean(String thingUID, String bridgeUID, String label, DiscoveryResultFlag flag,
            Map<String, Object> properties) {
        this.thingUID = thingUID;
        this.bridgeUID = bridgeUID;
        this.label = label;
        this.flag = flag;
        this.properties = properties;
    }

}
