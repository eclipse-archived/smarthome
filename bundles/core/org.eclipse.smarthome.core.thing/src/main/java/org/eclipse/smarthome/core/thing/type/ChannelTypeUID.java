/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import org.eclipse.smarthome.core.thing.UID;

/**
 * {@link ChannelTypeUID} represents a unique identifier for channel types.
 * 
 * @author Dennis Nobel - Initial contribution
 * @author Jochen Hiller - Bugfix 455434: added default constructor
 */
public class ChannelTypeUID extends UID {

    /**
     * Default constructor in package scope only. Will allow to instantiate this
     * class by reflection. Not intended to be used for normal instantiation.
     */
    ChannelTypeUID() {
        super();
    }

    public ChannelTypeUID(String channelUid) {
        super(channelUid);
    }

    public ChannelTypeUID(String bindingId, String id) {
        super(bindingId, id);
    }

    @Override
    protected int getMinimalNumberOfSegments() {
        return 2;
    }

    public String getId() {
        return getSegment(1);
    }

}
