/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.test;

import static org.eclipse.smarthome.binding.dmx.DmxBindingConstants.BINDING_ID;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.binding.dmx.internal.DmxBridgeHandler;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.Universe;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TestBridgeHandler} is only for testing
 *
 * @author Jan N. Klug - Initial contribution
 */

public class TestBridgeHandler extends DmxBridgeHandler {
    public final static ThingTypeUID THING_TYPE_TEST_BRIDGE = new ThingTypeUID(BINDING_ID, "test-bridge");
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_TEST_BRIDGE);
    public static final int MIN_UNIVERSE_ID = 0;
    public static final int MAX_UNIVERSE_ID = 0;

    private final Logger logger = LoggerFactory.getLogger(TestBridgeHandler.class);

    public TestBridgeHandler(Bridge testBridge) {
        super(testBridge);
    }

    @Override
    protected void openConnection() {

    }

    @Override
    protected void closeConnection() {

    }

    @Override
    protected void sendDmxData() {

    }

    @Override
    protected void updateConfiguration() {
        universe = new Universe(MIN_UNIVERSE_ID);

        super.updateConfiguration();

        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);

        logger.debug("updated configuration for Test bridge {}", this.thing.getUID());
    }

    @Override
    public void initialize() {
        logger.debug("initializing Test bridge {}", this.thing.getUID());

        updateConfiguration();
    }

    /**
     *
     * get single channel data
     *
     * @param time UNIX timestamp of calculation time
     *
     * @param index channel number
     *
     * @return channel value
     */
    public byte getBufferValue(long time, int index) {
        universe.calculateBuffer(time);
        byte[] buffer = universe.getBuffer();
        if ((index > 1) && (index - 1) < buffer.length) {
            return buffer[index - 1];
        } else {
            throw new IllegalArgumentException("index not available");
        }
    }

    /**
     * update briudge status manually
     *
     * @param status a ThingStatus to set the bridge to
     */
    public void updateBridgeStatus(ThingStatus status) {
        updateStatus(status);
    }
}
