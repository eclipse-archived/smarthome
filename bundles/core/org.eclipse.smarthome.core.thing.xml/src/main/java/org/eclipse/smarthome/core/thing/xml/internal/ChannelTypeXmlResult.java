/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
 */
public class ChannelTypeXmlResult {

    private ChannelType channelType;
    private ConfigDescription configDescription;

    public ChannelTypeXmlResult(ChannelType channelType, ConfigDescription configDescription) {
        this.channelType = channelType;
        this.configDescription = configDescription;
    }

    public ChannelType getChannelType() {
        return this.channelType;
    }

    public ConfigDescription getConfigDescription() {
        return this.configDescription;
    }

    @Override
    public String toString() {
        return "ChannelTypeXmlResult [channelType=" + channelType + ", configDescription=" + configDescription + "]";
    }

}
