/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.setup.discovery.bulb;

import java.util.Collections;

import org.eclipse.smarthome.binding.hue.config.HueLightConfiguration;
import org.eclipse.smarthome.binding.hue.internal.HueThingTypeProvider;
import org.eclipse.smarthome.binding.hue.internal.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.internal.setup.discovery.bridge.HueBridgeServiceTracker;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceInfo;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueBridgeServiceTracker} tracks for hue lights which are connected
 * to a paired hue bridge.
 * 
 */
public class HueLightDiscoveryService extends AbstractDiscoveryService implements
        HueLightDiscoveryListener {

    private final static Logger logger = LoggerFactory.getLogger(HueLightDiscoveryService.class);

    private static final int FORCE_DISCOVERY_TIMEOUT_IN_SECONDS = 20;

    private ServiceTracker<ThingHandler, ThingHandler> bridgeHandlerServiceTracker;

    private Thread forceDiscoveryThread;

    private boolean forceMode;

    private static String getFilter() {
        return "(&(" + Constants.OBJECTCLASS + "=" + ThingHandler.class.getName() + ")("
                + ThingHandler.SERVICE_PROPERTY_THING_TYPE + "="
                + HueThingTypeProvider.BRIDGE_THING_TYPE + "))";
    }

    public HueLightDiscoveryService(BundleContext bundleContext) throws InvalidSyntaxException {
        bridgeHandlerServiceTracker = new ServiceTracker<ThingHandler, ThingHandler>(bundleContext,
                bundleContext.createFilter(getFilter()), null) {
            @Override
            public ThingHandler addingService(ServiceReference<ThingHandler> reference) {
                HueBridgeHandler hueBridgeHandler = (HueBridgeHandler) super
                        .addingService(reference);
                hueBridgeHandler.registerHueLightDiscoveryListener(HueLightDiscoveryService.this);
                hueBridgeHandler.forceDiscovery();
                return hueBridgeHandler;
            }

            @Override
            public void removedService(ServiceReference<ThingHandler> reference,
                    ThingHandler service) {
                HueBridgeHandler hueBridgeHandler = (HueBridgeHandler) service;
                super.removedService(reference, service);
                hueBridgeHandler.unregisterLightStatusListener(HueLightDiscoveryService.this);
            }
        };
    }

    public void dispose() {
        bridgeHandlerServiceTracker.close();
    }

    @Override
    public DiscoveryServiceInfo getInfo() {
        return new DiscoveryServiceInfo(
                Collections.singletonList(HueThingTypeProvider.LIGHT_THING_TYPE.getUID()),
                FORCE_DISCOVERY_TIMEOUT_IN_SECONDS);
    }

    @Override
    public void setAutoDiscoveryEnabled(boolean enabled) {
        super.setAutoDiscoveryEnabled(enabled);
        if (isAutoDiscoveryEnabled()) {
            if (!isTracking()) {
                startTracking();
            }
        } else {
            if (isTracking()) {
                stopTracking();
            }
        }
    }

    private void startTracking() {
        bridgeHandlerServiceTracker.open();
    }

    private void stopTracking() {
        bridgeHandlerServiceTracker.close();
    }

    private boolean isTracking() {
        return bridgeHandlerServiceTracker.getTrackingCount() > -1;
    }

    @Override
    public void forceDiscovery() {

        if (forceMode == true) {
            logger.warn("Not starting force discovery, because it is already running.");
            return;
        }

        forceMode = true;
        forceDiscoveryThread = new Thread(new ForceDiscoveryProcess());
        forceDiscoveryThread.start();
    }

    private final class ForceDiscoveryProcess implements Runnable {
        @Override
        public void run() {
            try {
                if (isTracking()) {
                    HueBridgeHandler[] bridgeHandlers = (HueBridgeHandler[]) bridgeHandlerServiceTracker
                            .getServices();
                    if (bridgeHandlers != null) {
                        for (int i = 0; i < bridgeHandlers.length && !Thread.interrupted(); i++) {
                            HueBridgeHandler hueBridgeHandler = bridgeHandlers[i];
                            hueBridgeHandler.forceDiscovery();
                        }
                    }
                } else {
                    startTracking();
                    stopTracking();
                }
                Thread.sleep((FORCE_DISCOVERY_TIMEOUT_IN_SECONDS / 2) * 1000);
            } catch (InterruptedException ex) {
                logger.info("Force discovery was interrupted");
            } finally {
                forceMode = false;
                discoveryFinished();
            }
        }
    }

    @Override
    public void abortForceDiscovery() {
        if (forceDiscoveryThread != null) {
            forceDiscoveryThread.interrupt();
        }
    }

    @Override
    public boolean isForced() {
        return forceMode;
    }

    @Override
    public void onHueLightDiscovered(Bridge hueBridge, String lightId, String lightName) {
        ThingUID hueBridgeUID = hueBridge.getUID();
        String bridgeId = hueBridgeUID.getId();
        String thingLightId = bridgeId + "Light" + lightId;
        ThingTypeUID thingTypeUID = HueThingTypeProvider.LIGHT_THING_TYPE.getUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, thingLightId);
        DiscoveryResult discoveryResult = new DiscoveryResult(thingTypeUID, thingUID);
        discoveryResult.getProperties().put(HueLightConfiguration.LIGHT_ID, lightId);
        discoveryResult.setBridgeUID(hueBridgeUID);
        discoveryResult.setLabel(lightName);
        thingDiscovered(discoveryResult);
    }
}
