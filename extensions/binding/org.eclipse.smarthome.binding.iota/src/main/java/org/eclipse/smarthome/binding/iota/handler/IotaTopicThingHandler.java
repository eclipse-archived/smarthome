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

import static org.eclipse.smarthome.binding.iota.handler.IotaConfiguration.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.iota.IotaBindingConstants;
import org.eclipse.smarthome.binding.iota.internal.NumberValue;
import org.eclipse.smarthome.binding.iota.internal.OnOffValue;
import org.eclipse.smarthome.binding.iota.internal.PercentValue;
import org.eclipse.smarthome.binding.iota.internal.TextValue;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.iota.utils.IotaUtilsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link IotaTopicThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class IotaTopicThingHandler extends BaseThingHandler implements ChannelStateUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(IotaTopicThingHandler.class);
    private TransformationServiceProvider transformationServiceProvider;
    private final Map<ChannelUID, ChannelConfig> channelDataByChannelUID = new HashMap<>();
    private String root;
    private JsonArray data = new JsonArray();
    private int refresh = 60; // default value in second
    private String mode;
    private String key;
    private IotaUtilsImpl utils;
    private ScheduledFuture<?> refreshJob;

    public IotaTopicThingHandler(Thing thing, TransformationServiceProvider transformationServiceProvider) {
        super(thing);
        this.transformationServiceProvider = transformationServiceProvider;
    }

    public IotaTopicThingHandler(Thing thing) {
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

    @Override
    public void initialize() {
        logger.debug("Initializing Iota handler");
        Configuration configuration = getThing().getConfiguration();
        setRoot((String) configuration.get(ROOT_ADDRESS));
        int refresh = configuration.get(REFRESH_INTERVAL) == null ? 60
                : ((BigDecimal) configuration.get(REFRESH_INTERVAL)).intValue();
        setRefresh(refresh);
        setMode((String) configuration.get(MODE));
        setKey((String) configuration.get(PRIVATE_KEY));

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
                config.transformationServiceProvider = transformationServiceProvider;
                config.channelStateUpdateListener = this;

                if (StringUtils.isNotBlank(config.transformationPattern)) {
                    int index = config.transformationPattern.indexOf(':');
                    if (index == -1) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "The transformation pattern must consist of the type and the pattern separated by a colon");
                        return;
                    }
                    String type = config.transformationPattern.substring(0, index).toUpperCase();
                    config.transformationPattern = config.transformationPattern.substring(index + 1);
                    config.transformationServiceName = type;
                }

                ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID != null) {
                    switch (channelTypeUID.getId()) {
                        case IotaBindingConstants.TEXT_CHANNEL:
                            config.value = new TextValue();
                            break;
                        case IotaBindingConstants.NUMBER_CHANNEL:
                            config.value = new NumberValue(config.isFloat);
                            break;
                        case IotaBindingConstants.PERCENTAGE_CHANNEL:
                            config.value = new PercentValue(config.isFloat);
                            break;
                        case IotaBindingConstants.ONOFF_CHANNEL:
                            config.value = new OnOffValue(config.on, config.off, config.inverse);
                            break;
                        default:
                            throw new IllegalArgumentException("ThingTypeUID not recognised");
                    }
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
     *
     * listens on the tangle until data is retrieved
     */
    private synchronized void startAutomaticRefresh() {
        logger.debug("Binding will refresh every {} seconds", refresh);
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                boolean success = fetchItemState();
                if (success) {
                    updateAllStates(data);
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 0, refresh, TimeUnit.SECONDS);
    }

    /**
     *
     * @return success if any data is found in the MAM transaction
     *
     */
    private synchronized boolean fetchItemState() {
        boolean success = false;
        if (!root.isEmpty()) {
            JsonParser parser = new JsonParser();
            if (this.utils != null) {
                JsonObject resp = parser.parse(utils.fetchFromTangle(refresh, root, mode, key)).getAsJsonObject();
                if (resp != null) {
                    root = resp.get("NEXTROOT").getAsString();
                    data = resp.entrySet().iterator().next().getValue().getAsJsonArray();
                    success = true;
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Could not fetch data");
        }
        return success;
    }

    /**
     * Updates all item states with the fetched data
     *
     * @param data
     */
    public synchronized void updateAllStates(JsonArray data) {
        String str = null;
        if (data.size() != 0) {
            for (Channel channel : thing.getChannels()) {
                ChannelConfig config = channelDataByChannelUID.get(channel.getUID());
                if (config.transformationPattern != null) {
                    TransformationServiceProvider provider = config.transformationServiceProvider;
                    TransformationService service = provider.getTransformationService(config.transformationServiceName);
                    if (service == null) {
                        logger.warn("Transformation service '{}' not found", config.transformationServiceName);
                        return;
                    }
                    try {
                        /**
                         * A path to the value has been directly inserted by the user.
                         * For instance, for a json { "device": { "status": { "value": "73" } } },
                         * one can extract the state through $.device.status.value
                         */
                        str = service.transform(config.transformationPattern, data.toString());
                        if (str != null && !str.isEmpty()) {
                            config.processMessage(str);
                        }

                    } catch (TransformationException e) {
                        logger.error("Error executing the transformation {}", str);
                        return;
                    }
                } else {
                    logger.warn("Transformation pattern not provided");
                }
            }
        }
    }

    protected void setRoot(String root) {
        this.root = root;
    }

    protected void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    protected void setMode(String mode) {
        this.mode = mode;
    }

    protected void setKey(String key) {
        this.key = key;
    }

    protected void setUtils(IotaUtilsImpl utils) {
        this.utils = utils;
    }

    @Override
    public void channelStateUpdated(ChannelUID channelUID, State value) {
        updateState(channelUID.getId(), value);
    }

    public void addChannelDataByChannelUID(@NonNull ChannelUID uid, ChannelConfig config) {
        channelDataByChannelUID.put(uid, config);
    }
}
