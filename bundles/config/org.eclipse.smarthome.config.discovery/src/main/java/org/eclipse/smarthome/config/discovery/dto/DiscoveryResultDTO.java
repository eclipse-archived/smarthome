/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.dto;

import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;

/**
 * This is a data transfer object that is used to serialize discovery results.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Thomas Höfer - Added representation
 */
public class DiscoveryResultDTO {

    public String bridgeUID;
    public DiscoveryResultFlag flag;
    public String label;
    public Map<String, Object> properties;
    public String representationProperty;
    public String thingUID;
    public String thingTypeUID;

    public DiscoveryResultDTO() {
    }

    public DiscoveryResultDTO(String thingUID, String bridgeUID, String thingTypeUID, String label,
            DiscoveryResultFlag flag, Map<String, Object> properties, String representationProperty) {
        this.thingUID = thingUID;
        this.thingTypeUID = thingTypeUID;
        this.bridgeUID = bridgeUID;
        this.label = label;
        this.flag = flag;
        this.properties = properties;
        this.representationProperty = representationProperty;
    }

}
