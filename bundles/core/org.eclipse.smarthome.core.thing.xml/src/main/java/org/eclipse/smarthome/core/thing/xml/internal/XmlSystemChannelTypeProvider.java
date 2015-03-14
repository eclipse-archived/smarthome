/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.SystemChannelTypeProvider;

/**
 * Contributes all {@link SystemChannelTypeProvider}s that are found while
 * parsing bundle XMLs.
 * 
 * @author Ivan Iliev - Initial contribution
 * 
 */
public class XmlSystemChannelTypeProvider implements SystemChannelTypeProvider {

    private List<ChannelType> channelTypes = new CopyOnWriteArrayList<>();

    @Override
    public Collection<ChannelType> getSystemChannelTypes() {
        return Collections.unmodifiableCollection(channelTypes);
    }

    public void addChannelType(ChannelType type) {
        channelTypes.add(type);
    }

    public boolean removeChannelType(ChannelType type) {
        return channelTypes.remove(type);
    }

}
