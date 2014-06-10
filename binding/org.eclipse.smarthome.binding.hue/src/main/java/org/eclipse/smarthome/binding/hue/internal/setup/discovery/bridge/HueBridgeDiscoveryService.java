/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.setup.discovery.bridge;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.binding.hue.config.HueBridgeConfiguration;
import org.eclipse.smarthome.binding.hue.internal.HueThingTypeProvider;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueBridgeDiscoveryService} is responsible for discovering new and
 * removed hue bridges. The {@link HueBridgeDiscoveryService} notified the
 * registered {@link DiscoveryListener}s if a new or a removed device is
 * detected.
 * 
 * @author Oliver Libutzki - Initial contribution
 * 
 */
public class HueBridgeDiscoveryService extends AbstractDiscoveryService implements
        UPnPDiscoveryListener {

    private static final int FORCE_DISCOVERY_TIMEOUT_IN_SECONDS = 20;

    private final static Logger logger = LoggerFactory.getLogger(HueBridgeDiscoveryService.class);

    private final static String IP_ADDRESS = "UPnP.device.IP";

    private List<DiscoveredBridge> discoveredBridges = new CopyOnWriteArrayList<DiscoveredBridge>();

    private boolean forceMode = false;

    private HueBridgeServiceTracker hueBridgeServiceTracker = null;

    private Thread forceDiscoveryThread;

    public HueBridgeDiscoveryService(BundleContext bundleContext) throws InvalidSyntaxException {
        hueBridgeServiceTracker = new HueBridgeServiceTracker(bundleContext, this);
    }

    @Override
    public void onDeviceAdded(ServiceReference<?> uPnPDeviceRef) {
        String serialNumber = getSerialNumber(uPnPDeviceRef);
        String ipAddress = getIpAddress(uPnPDeviceRef);
        DiscoveredBridge discoveredBridge = new DiscoveredBridge(serialNumber, ipAddress);
        discoveredBridges.add(discoveredBridge);
        logger.debug("New hue bridge detected with Serial number {} at IP address {}.",
                serialNumber, ipAddress);
        notifyListenersOnBridgeDiscovered(discoveredBridge);
    }

    @Override
    public void onDeviceRemoved(ServiceReference<?> uPnPDeviceRef) {
        String serialNumber = getSerialNumber(uPnPDeviceRef);
        String ipAddress = getIpAddress(uPnPDeviceRef);
        for (Iterator<DiscoveredBridge> discoveredBridgeIter = discoveredBridges.iterator(); discoveredBridgeIter
                .hasNext();) {
            DiscoveredBridge discoveredBridge = discoveredBridgeIter.next();
            if (discoveredBridge.serialNumber.equals(serialNumber)) {
                discoveredBridges.remove(discoveredBridge);
            }

        }
        logger.debug("Hue bridge removed with Serial number {} at IP address {}", serialNumber,
                ipAddress);
        notifyListenersOnBridgeRemoved(serialNumber);
    }

    protected String getIpAddress(ServiceReference<?> uPnPDeviceRef) {
        String ipAddress = (String) uPnPDeviceRef.getProperty(IP_ADDRESS);
        return ipAddress;
    }

    protected String getSerialNumber(ServiceReference<?> uPnPDeviceRef) {
        String serialNumber = (String) uPnPDeviceRef.getProperty(UPnPDevice.SERIAL_NUMBER);
        return serialNumber;
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

    protected void notifyListenersOnBridgeDiscovered(DiscoveredBridge discoveredBridge) {
        ThingTypeUID bridgeTypeUID = HueThingTypeProvider.BRIDGE_THING_TYPE.getUID();

        DiscoveryResult discoveryResult = new DiscoveryResult(bridgeTypeUID, new ThingUID(
                bridgeTypeUID, discoveredBridge.serialNumber));
        Map<String, Object> properties = discoveryResult.getProperties();
        properties.put(HueBridgeConfiguration.IP_ADDRESS, discoveredBridge.ipAddress);
        properties.put(HueBridgeConfiguration.BRIDGE_SERIAL_NUMBER, discoveredBridge.serialNumber);
        discoveryResult.setLabel(discoveredBridge.serialNumber + " (" + discoveredBridge.ipAddress
                + ")");
        thingDiscovered(discoveryResult);
    }

    protected void notifyListenersOnBridgeRemoved(String serialNumber) {
        ThingUID bridgeUID = new ThingUID(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID(),
                serialNumber);
        thingRemoved(bridgeUID);
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
        discoveredBridges.clear();
        hueBridgeServiceTracker.open();
    }

    private void stopTracking() {
        hueBridgeServiceTracker.close();
    }

    private boolean isTracking() {
        return hueBridgeServiceTracker.getTrackingCount() > -1;
    }

    private final class ForceDiscoveryProcess implements Runnable {
        @Override
        public void run() {
            try {
                if (isTracking()) {
                    for (DiscoveredBridge discoveredBridge : discoveredBridges) {
                        notifyListenersOnBridgeDiscovered(discoveredBridge);
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

    private class DiscoveredBridge {

        private String serialNumber;
        private String ipAddress;

        public DiscoveredBridge(String serialNumber, String ipAddress) {
            this.serialNumber = serialNumber;
            this.ipAddress = ipAddress;
        }

    }

    @Override
    public DiscoveryServiceInfo getInfo() {
        return new DiscoveryServiceInfo(
                Collections.singletonList(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID()),
                FORCE_DISCOVERY_TIMEOUT_IN_SECONDS);
    }

}
