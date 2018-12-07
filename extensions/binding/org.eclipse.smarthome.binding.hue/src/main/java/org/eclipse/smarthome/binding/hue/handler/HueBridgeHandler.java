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
package org.eclipse.smarthome.binding.hue.handler;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.THING_TYPE_BRIDGE;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.binding.hue.internal.discovery.HueLightDiscoveryService;
import org.eclipse.smarthome.binding.hue.internal.dto.HueConfig;
import org.eclipse.smarthome.binding.hue.internal.exceptions.ApiException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.LinkButtonException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.UnauthorizedException;
import org.eclipse.smarthome.binding.hue.internal.utils.AsyncHttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueBridgeHandler} is the handler for a hue bridge and connects it to
 * the framework. All {@link HueLightHandler}s use the {@link HueBridgeHandler} to execute the actual commands.
 *
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Oliver Libutzki
 * @author Kai Kreuzer - improved state handling
 * @author Andre Fuechsel - implemented getFullLights(), startSearch()
 * @author Thomas Höfer - added thing properties
 * @author Stefan Bußweiler - Added new thing status handling
 * @author Jochen Hiller - fixed status updates, use reachable=true/false for state compare
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 */
@NonNullByDefault
public class HueBridgeHandler extends ConfigStatusBridgeHandler implements Runnable {
    public static final String IP_ADDRESS_MISSING = "missing-ip-address-configuration";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    public static final String DEVICE_TYPE = "EclipseSmartHome";

    private final Logger logger = LoggerFactory.getLogger(HueBridgeHandler.class);
    private boolean lastBridgeConnectionState = false;
    protected @Nullable ScheduledFuture<?> pollingJob;
    protected HueBridge hueBridge; // not final for test injections
    protected final AsyncHttpClient httpClient;
    protected HueBridgeHandlerConfig config = new HueBridgeHandlerConfig();

    public HueBridgeHandler(Bridge bridge, AsyncHttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        hueBridge = new HueBridge(httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not needed
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(HueLightDiscoveryService.class);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(HueBridgeHandlerConfig.class);
        hueBridge.initialize(config.ipAddress, config.userName);

        ScheduledFuture<?> job = pollingJob;
        if (job == null || job.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(this, 100, config.pollingInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * This method is called whenever the connection to the {@link HueBridge} is lost.
     */
    public void onConnectionLost() {
        if (lastBridgeConnectionState) {
            lastBridgeConnectionState = false;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.bridge-connection-lost");
        }
    }

    /**
     * New authentication credentials will be requested from the bridge.
     *
     * If it was not successful, the Thing status will be set to offline.
     * If it was successful, the Thing configuration will be updated, which results
     * in a new dispose/initialize cycle.
     *
     * @return returns {@code true} if re-authentication was successful, {@code false} otherwise
     */
    protected boolean createUser(@Nullable String proposedUsername) {
        try {
            String newUser = hueBridge.createApiKey(proposedUsername, DEVICE_TYPE);
            Configuration config = editConfiguration();
            config.put(HueBridgeHandlerConfig.USER_NAME, newUser);
            updateConfiguration(config);
            return true;
        } catch (ApiException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-press-pairing-button");
        } catch (IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
        }
        return false;
    }

    @Override
    public void run() {
        try {
            if (thing.getStatus() != ThingStatus.ONLINE) {
                // When going from offline -> online update thing properties
                HueConfig config = hueBridge.updateConfig();
                Map<String, String> properties = editProperties();
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.bridgeid);
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.swversion);
                updateProperties(properties);

                updateStatus(ThingStatus.ONLINE);
            }
            hueBridge.updateLights();
            lastBridgeConnectionState = true;

        } catch (UnauthorizedException e) { // No user yet
            if (createUser(null)) {
                run();
            }
        } catch (LinkButtonException e) { // User not on whitelist
            if (createUser(hueBridge.getUsername())) {
                run();
            }
        } catch (ApiException e) {
            if (hueBridge.isHueBridgeAndReachable()) {
                logger.warn("An API error occured: {}", e.getMessage());
            } else { // Not a hue bridge
                onConnectionLost();
            }
        } catch (IOException e) {
            onConnectionLost();
        }
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        if (config.ipAddress.isEmpty()) {
            return Collections.singletonList(ConfigStatusMessage.Builder.error(HueBridgeHandlerConfig.HOST)
                    .withMessageKeySuffix(IP_ADDRESS_MISSING).withArguments(HueBridgeHandlerConfig.HOST).build());
        }

        return Collections.emptyList();
    }

    public HueBridge getHueBridge() {
        return hueBridge;
    }
}
