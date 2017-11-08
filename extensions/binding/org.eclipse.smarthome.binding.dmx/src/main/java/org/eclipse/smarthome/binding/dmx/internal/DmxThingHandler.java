/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.internal;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link DmxThingHandler} is an abstract class with base functions
 * for DMX Things
 *
 * @author Jan N. Klug - Initial contribution
 */

public abstract class DmxThingHandler extends BaseThingHandler {

    protected ThingStatusDetail dmxHandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    public DmxThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (ThingStatus.ONLINE.equals(bridgeStatusInfo.getStatus())
                && ThingStatus.OFFLINE.equals(getThing().getStatusInfo().getStatus())
                && ThingStatusDetail.BRIDGE_OFFLINE.equals(getThing().getStatusInfo().getStatusDetail())) {
            if (ThingStatusDetail.NONE.equals(dmxHandlerStatus)) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            } else {
                updateStatus(ThingStatus.OFFLINE, dmxHandlerStatus);
            }
        }
    }
}
