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
        if (!this.thing.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    protected void closeConnection() {

    }

    @Override
    protected void sendDmxData() {
        if (!this.thing.getStatus().equals(ThingStatus.ONLINE)) {
            openConnection();
        }
    }

    @Override
    protected void updateConfiguration() {
        universe = new Universe(MIN_UNIVERSE_ID);

        super.updateConfiguration();

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);

        logger.debug("updated configuration for Test bridge {}", this.thing.getUID());
    }

    @Override
    public void initialize() {
        logger.debug("initializing Test bridge {}", this.thing.getUID());

        updateConfiguration();
    }

}
