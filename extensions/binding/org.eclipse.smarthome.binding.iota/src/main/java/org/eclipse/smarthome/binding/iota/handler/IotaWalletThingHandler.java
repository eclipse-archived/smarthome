/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.iota.handler;

import static org.eclipse.smarthome.binding.iota.handler.IotaConfiguration.REFRESH_INTERVAL;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.iota.IotaBindingConstants;
import org.eclipse.smarthome.binding.iota.internal.NumberValue;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.iota.utils.IotaUtilsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IotaWalletThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class IotaWalletThingHandler extends BaseThingHandler implements ChannelStateUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(IotaWalletThingHandler.class);

    private final Map<ChannelUID, ChannelConfig> channelDataByChannelUID = new HashMap<>();
    private int refresh = 60; // default value in second
    private IotaUtilsImpl utils;
    private ScheduledFuture<?> refreshJob;

    public IotaWalletThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ChannelConfig data = channelDataByChannelUID.get(channelUID);
        if (data == null) {
            logger.warn("Channel {} not supported", channelUID.getId());
            return;
        }
        if (command instanceof RefreshType) {
            if (data.value.getValue() != null) {
                updateState(channelUID.getId(), data.value.getValue());
            }
        }
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {

        logger.debug("Initializing Iota Wallet Handler.");
        Configuration configuration = getThing().getConfiguration();
        int refresh = configuration.get(REFRESH_INTERVAL) == null ? 60
                : ((BigDecimal) configuration.get(REFRESH_INTERVAL)).intValue();
        setRefresh(refresh);

        Bridge bridge = this.getBridge();
        if (bridge != null) {
            IotaBridgeHandler bridgeHandler = (IotaBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                logger.debug("Bridge initialized");
                this.utils = bridgeHandler.getUtils();

            } else {
                logger.debug("Could not initialize Iota Utils for thing {}", this.getThing().getUID());
            }
        } else {
            logger.debug("Could not initialize Iota Utils for thing {}", this.getThing().getUID());
        }

        for (Channel channel : thing.getChannels()) {
            if (!channelDataByChannelUID.containsKey(channel.getUID())) {
                ChannelConfig config = channel.getConfiguration().as(ChannelConfig.class);
                config.channelUID = channel.getUID();
                config.channelStateUpdateListener = this;
                switch (channel.getChannelTypeUID().getId()) {
                    case IotaBindingConstants.CHANNEL_BALANCE:
                        config.value = new NumberValue(true);
                        break;
                    default:
                        throw new IllegalArgumentException("ThingTypeUID not recognised");
                }
                channelDataByChannelUID.put(channel.getUID(), config);
            }
        }

        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        if (this.getThing().getChannels().size() != 0) {
            startAutomaticRefresh();
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        for (ChannelConfig channelConfig : channelDataByChannelUID.values()) {
            channelConfig.dispose();
        }
        channelDataByChannelUID.clear();
    }

    /**
     * Refresh the balance of each channel every {refresh} seconds
     */
    private synchronized void startAutomaticRefresh() {
        logger.debug("Balance will refresh every {} seconds", refresh);
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                getBalance();
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 0, refresh, TimeUnit.SECONDS);
    }

    /**
     * Fetch the balance of a given wallet address
     */
    public void getBalance() {
        if (this.utils != null) {
            for (Channel channel : thing.getChannels()) {
                ChannelConfig config = channelDataByChannelUID.get(channel.getUID());
                logger.debug("Refreshing balance for wallet address: {}", config.address);
                config.processMessage(
                        String.valueOf(this.utils.getBalances(100, Collections.singletonList(config.address))));
            }
        }
    }

    public void addChannelDataByChannelUID(ChannelUID channelUID, ChannelConfig config) {
        channelDataByChannelUID.put(channelUID, config);
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    @Override
    public void channelStateUpdated(ChannelUID channelUID, State value) {
        updateState(channelUID.getId(), value);
    }
}
