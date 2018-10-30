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
package org.eclipse.smarthome.binding.tradfri.handler;

import static org.eclipse.smarthome.core.thing.Thing.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.tradfri.internal.CoapCallback;
import org.eclipse.smarthome.binding.tradfri.internal.TradfriCoapClient;
import org.eclipse.smarthome.binding.tradfri.internal.config.TradfriDeviceConfig;
import org.eclipse.smarthome.binding.tradfri.internal.model.TradfriDeviceData;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TradfriThingHandler} is the abstract base class for individual device handlers.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Christoph Weitkamp - Restructuring and refactoring of the binding
 */
public abstract class TradfriThingHandler extends BaseThingHandler implements CoapCallback {

    private final Logger logger = LoggerFactory.getLogger(TradfriThingHandler.class);

    // the unique instance id of the device
    protected Integer id;

    // used to check whether we have already been disposed when receiving data asynchronously
    protected volatile boolean active;

    protected TradfriCoapClient coapClient;

    private CoapObserveRelation observeRelation;

    public TradfriThingHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void initialize() {
        Bridge tradfriGateway = getBridge();
        this.id = getConfigAs(TradfriDeviceConfig.class).id;
        TradfriGatewayHandler handler = (TradfriGatewayHandler) tradfriGateway.getHandler();

        String uriString = handler.getGatewayURI() + "/" + id;
        try {
            URI uri = new URI(uriString);
            coapClient = new TradfriCoapClient(uri);
            coapClient.setEndpoint(handler.getEndpoint());
        } catch (URISyntaxException e) {
            logger.debug("Illegal device URI `{}`: {}", uriString, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
        active = true;
        updateStatus(ThingStatus.UNKNOWN);
        switch (tradfriGateway.getStatus()) {
            case ONLINE:
                scheduler.schedule(() -> {
                    observeRelation = coapClient.startObserve(this);
                }, 3, TimeUnit.SECONDS);
                break;
            case OFFLINE:
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        String.format("Gateway offline '%s'", tradfriGateway.getStatusInfo()));
                break;
        }
    }

    @Override
    public synchronized void dispose() {
        active = false;
        if (observeRelation != null) {
            observeRelation.reactiveCancel();
            observeRelation = null;
        }
        if (coapClient != null) {
            coapClient.shutdown();
        }
        super.dispose();
    }

    @Override
    public void setStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        if (active && getBridge().getStatus() != ThingStatus.OFFLINE && status != ThingStatus.ONLINE) {
            updateStatus(status, statusDetail);
            // we are offline and lost our observe relation - let's try to establish the connection in 10 seconds again
            scheduler.schedule(() -> {
                if (observeRelation != null) {
                    observeRelation.reactiveCancel();
                    observeRelation = null;
                }
                observeRelation = coapClient.startObserve(this);
            }, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        // the status might have changed because the bridge is completely reconfigured - so we need to re-establish
        // our CoAP connection as well
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            dispose();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initialize();
        }
    }

    protected void set(String payload) {
        logger.debug("Sending payload: {}", payload);
        coapClient.asyncPut(payload, this, scheduler);
    }

    protected void updateDeviceProperties(TradfriDeviceData state) {
        String firmwareVersion = state.getFirmwareVersion();
        if (firmwareVersion != null) {
            getThing().setProperty(PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        }

        String modelId = state.getModelId();
        if (modelId != null) {
            getThing().setProperty(PROPERTY_MODEL_ID, modelId);
        }

        String vendor = state.getVendor();
        if (vendor != null) {
            getThing().setProperty(PROPERTY_VENDOR, vendor);
        }
    }
}
