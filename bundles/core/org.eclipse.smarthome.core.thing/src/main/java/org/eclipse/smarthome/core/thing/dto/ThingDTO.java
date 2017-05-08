/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.dto;

import java.util.List;
import java.util.Map;

/**
 * This is a data transfer object that is used to serialize things.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Thomas Höfer - Added thing and thing type properties
 * @author Stefan Bußweiler - Added new thing status handling
 * @author Simon Kaufmann - Added label
 *
 */
public class ThingDTO {

    public String label;
    public String bridgeUID;
    public Map<String, Object> configuration;
    public Map<String, String> properties;
    public String UID;
    public String thingTypeUID;
    public List<ChannelDTO> channels;
    public String location;

    public ThingDTO() {
    }

    protected ThingDTO(String thingTypeUID, String UID, String label, String bridgeUID, List<ChannelDTO> channels,
            Map<String, Object> configuration, Map<String, String> properties, String location) {
        this.thingTypeUID = thingTypeUID;
        this.UID = UID;
        this.label = label;
        this.bridgeUID = bridgeUID;
        this.channels = channels;
        this.configuration = configuration;
        this.properties = properties;
        this.location = location;
    }

}
