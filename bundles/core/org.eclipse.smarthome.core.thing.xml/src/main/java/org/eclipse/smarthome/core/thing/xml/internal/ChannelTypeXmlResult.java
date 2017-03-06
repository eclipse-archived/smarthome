/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.core.thing.type.ChannelType;

/**
 * The {@link ChannelTypeXmlResult} is an intermediate XML conversion result object which
 * contains a {@link ChannelType} object.
 * <p>
 * If a {@link ConfigDescription} object exists, it must be added to the according {@link ConfigDescriptionProvider}.
 * 
 * @author Michael Grammling - Initial Contribution
 * @author Ivan Iliev - Added support for system wide channel types
 */
public class ChannelTypeXmlResult {

    private ChannelType channelType;
    private ConfigDescription configDescription;
    private boolean system;

    public ChannelTypeXmlResult(ChannelType channelType, ConfigDescription configDescription) {
        this(channelType, configDescription, false);
    }

    public ChannelTypeXmlResult(ChannelType channelType, ConfigDescription configDescription, boolean system) {
        this.channelType = channelType;
        this.configDescription = configDescription;
        this.system = system;
    }

    public ChannelType toChannelType() {
        return this.channelType;
    }

    public ConfigDescription getConfigDescription() {
        return this.configDescription;
    }

    public boolean isSystem() {
        return system;
    }

    @Override
    public String toString() {
        return "ChannelTypeXmlResult [channelType=" + channelType + ", configDescription=" + configDescription + "]";
    }

}
