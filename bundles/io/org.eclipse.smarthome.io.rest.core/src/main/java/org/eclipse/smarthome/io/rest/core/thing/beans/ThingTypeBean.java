/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

import java.util.List;
import java.util.Map;

/**
 * This is a java bean that is used with JAX-RS to serialize thing types to JSON.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Thomas Höfer - Added thing and thing type properties
 * @author Chris Jackson - Added parameter groups
 *
 */
public class ThingTypeBean {

    public List<ChannelDefinitionBean> channels;
    public List<ChannelGroupDefinitionBean> channelGroups;
    public List<ConfigDescriptionParameterBean> configParameters;
    public List<String> supportedBridgeTypeUIDs;
    public List<ParameterGroupBean> parameterGroups;
    public Map<String, String> properties;
    public String description;
    public String label;

    public String UID;
    boolean bridge;

    public ThingTypeBean() {
    }

    public ThingTypeBean(String UID, String label, String description,
            List<ConfigDescriptionParameterBean> configParameters, List<ChannelDefinitionBean> channels,
            List<ChannelGroupDefinitionBean> channelGroups, List<String> supportedBridgeTypeUIDs,
            Map<String, String> properties, boolean bridge, List<ParameterGroupBean> parameterGroups) {
        this.UID = UID;
        this.label = label;
        this.description = description;
        this.configParameters = configParameters;
        this.channels = channels;
        this.channelGroups = channelGroups;
        this.supportedBridgeTypeUIDs = supportedBridgeTypeUIDs;
        this.properties = properties;
        this.bridge = bridge;
        this.parameterGroups = parameterGroups;
    }

}
