/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bluetooth.handler;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.manager.AdapterGovernor;
import org.sputnikdev.bluetooth.manager.AdapterListener;
import org.sputnikdev.bluetooth.manager.BluetoothManager;
import org.sputnikdev.bluetooth.manager.GovernorListener;

/**
 *
 * 
 * @author Vlad Kolotov - Initial contribution
 */
public class AdapterHandler extends BluetoothHandler<AdapterGovernor>
        implements BridgeHandler, AdapterListener, GovernorListener {

    private Logger logger = LoggerFactory.getLogger(AdapterHandler.class);

    private final SingleChannelHandler<Boolean, OnOffType> readyHandler = new BooleanTypeChannelHandler(
            AdapterHandler.this, BluetoothBindingConstants.CHANNEL_READY) {
        @Override Boolean getValue() {
            return getGovernor().isReady();
        }
    };

    private final SingleChannelHandler<Boolean, OnOffType> poweredHandler = new BooleanTypeChannelHandler(
            AdapterHandler.this, BluetoothBindingConstants.CHANNEL_POWERED) {
        @Override Boolean getValue() {
            return getGovernor().isPowered();
        }
    };

    private final SingleChannelHandler<Boolean, OnOffType> poweredControlHandler = new BooleanTypeChannelHandler(
            AdapterHandler.this, BluetoothBindingConstants.CHANNEL_POWERED_CONTROL, true) {
        @Override Boolean getValue() {
            return getGovernor().getPoweredControl();
        }
        @Override void updateThing(Boolean value) {
            getGovernor().setPoweredControl(value);
        }
    };

    private final SingleChannelHandler<Boolean, OnOffType> discoveringHandler = new BooleanTypeChannelHandler(
            AdapterHandler.this, BluetoothBindingConstants.CHANNEL_DISCOVERING) {
        @Override Boolean getValue() {
            return getGovernor().isDiscovering();
        }
    };

    private final SingleChannelHandler<Boolean, OnOffType> discoveringControlHandler = new BooleanTypeChannelHandler(
            AdapterHandler.this, BluetoothBindingConstants.CHANNEL_DISCOVERING_CONTROL, true) {
        @Override Boolean getValue() {
            return getGovernor().getDiscoveringControl();
        }
        @Override void updateThing(Boolean value) {
            getGovernor().setDiscoveringControl(value);
        }
    };

    public AdapterHandler(Thing thing, ItemRegistry itemRegistry,
            BluetoothManager bluetoothManager, BluetoothGattParser parser) {
        super(thing, itemRegistry, bluetoothManager, parser);
        addChannelHandlers(Arrays.asList(
                readyHandler, discoveringHandler, discoveringControlHandler, poweredHandler, poweredControlHandler));
    }

    @Override
    public void initialize() {
        super.initialize();
        AdapterGovernor adapterGovernor = getGovernor();
        adapterGovernor.addGovernorListener(this);
        adapterGovernor.addAdapterListener(this);
        if (adapterGovernor.isReady()) {
            adapterGovernor.setAlias(thing.getLabel());
        }

        lastUpdatedChanged(new Date());

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void powered(boolean powered) {
        poweredHandler.updateChannel(powered);
    }

    @Override
    public void discovering(boolean discovering) {
        discoveringHandler.updateChannel(discovering);
    }

    @Override
    public void ready(boolean ready) {
        readyHandler.updateChannel(ready);
    }

    @Override
    public void lastUpdatedChanged(Date lastActivity) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastActivity);
        updateState(BluetoothBindingConstants.CHANNEL_LAST_UPDATED, new DateTimeType(calendar));
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) { }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) { }
}
