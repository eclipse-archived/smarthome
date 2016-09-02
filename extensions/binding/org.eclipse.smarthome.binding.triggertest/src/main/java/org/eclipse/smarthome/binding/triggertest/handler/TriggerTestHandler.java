/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.triggertest.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.smarthome.binding.triggertest.TriggerTestBindingConstants;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TriggerTestHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Moritz Kammerer - Initial contribution
 */
public class TriggerTestHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(TriggerTestHandler.class);

    public TriggerTestHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private ScheduledFuture<?> timer;

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);

        timer = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateState(TriggerTestBindingConstants.CHANNEL_1,
                        new StringType(Long.toString(System.currentTimeMillis())));
                updateState(TriggerTestBindingConstants.CHANNEL_2,
                        new StringType(Long.toString(System.currentTimeMillis())));
                triggerChannel(TriggerTestBindingConstants.TRIGGER_1, "PRESSED");
                triggerChannel(TriggerTestBindingConstants.TRIGGER_2);
            }

        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        timer.cancel(true);
    }
}
