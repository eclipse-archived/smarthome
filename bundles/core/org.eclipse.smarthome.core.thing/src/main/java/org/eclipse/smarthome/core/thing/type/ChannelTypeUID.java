/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import org.eclipse.smarthome.core.thing.UID;

public class ChannelTypeUID extends UID {

    public ChannelTypeUID(String channelUid) {
        super(channelUid);
    }

    public ChannelTypeUID(String bindingId, String id) {
        super(bindingId, id);
    }

    @Override
    protected int getNumberOfSegments() {
        return 2;
    }

    public String getId() {
        return getSegment(1);
    }

}
