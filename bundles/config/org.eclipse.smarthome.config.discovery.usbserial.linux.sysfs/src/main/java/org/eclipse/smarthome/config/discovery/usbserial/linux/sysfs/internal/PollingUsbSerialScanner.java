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
package org.eclipse.smarthome.config.discovery.usbserial.linux.sysfs.internal;

import static java.lang.Long.parseLong;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDiscovery;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDiscoveryListener;
import org.eclipse.smarthome.config.discovery.usbserial.linux.sysfs.internal.DeltaUsbSerialScanner.Delta;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link UsbSerialDiscovery} that implements background discovery by doing repetitive scans using a
 * {@link UsbSerialScanner}, pausing a configurable amount of time between subsequent scans.
 *
 * @author Henning Sudbrock - initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "discovery.usbserial.linux.sysfs.pollingscanner")
public class PollingUsbSerialScanner implements UsbSerialDiscovery {

    private final Logger logger = LoggerFactory.getLogger(PollingUsbSerialScanner.class);

    private static final String THREAD_POOL_NAME = "usb-serial-discovery-linux-sysfs";

    public static final String PAUSE_BETWEEN_SCANS_IN_SECONDS_ATTRIBUTE = "pauseBetweenScansInSeconds";
    private static final Duration DEFAULT_PAUSE_BETWEEN_SCANS = Duration.ofSeconds(5);
    private Duration pauseBetweenScans = DEFAULT_PAUSE_BETWEEN_SCANS;

    @NonNullByDefault({})
    private DeltaUsbSerialScanner deltaUsbSerialScanner;

    private final Set<UsbSerialDiscoveryListener> discoveryListeners = new CopyOnWriteArraySet<>();

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(THREAD_POOL_NAME);
    @Nullable
    private ScheduledFuture<?> backgroundScanningJob;

    @Reference
    protected void setUsbSerialScanner(UsbSerialScanner usbSerialScanner) {
        deltaUsbSerialScanner = new DeltaUsbSerialScanner(usbSerialScanner);
    }

    protected void unsetUsbSerialScanner(UsbSerialScanner usbSerialScanner) {
        deltaUsbSerialScanner = null;
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        if (config.containsKey(PAUSE_BETWEEN_SCANS_IN_SECONDS_ATTRIBUTE)) {
            pauseBetweenScans = Duration
                    .ofSeconds(parseLong(config.get(PAUSE_BETWEEN_SCANS_IN_SECONDS_ATTRIBUTE).toString()));
        }
    }

    @Modified
    protected synchronized void modified(Map<String, Object> config) {
        if (config.containsKey(PAUSE_BETWEEN_SCANS_IN_SECONDS_ATTRIBUTE)) {
            pauseBetweenScans = Duration
                    .ofSeconds(parseLong(config.get(PAUSE_BETWEEN_SCANS_IN_SECONDS_ATTRIBUTE).toString()));

            if (backgroundScanningJob != null) {
                stopBackgroundScanning();
                startBackgroundScanning();
            }
        }
    }

    /**
     * Performs a single scan for newly added and removed devices.
     */
    @Override
    public void doSingleScan() {
        singleScanInternal(true);
    }

    /**
     * Starts repeatedly scanning for newly added and removed USB devices in the usbserial devices folder (where the
     * duration between two subsequent scans is configurable).
     * <p/>
     * This repeated scanning can be stopped using {@link #stopBackgroundScanning()}.
     */
    @Override
    public synchronized void startBackgroundScanning() {
        if (backgroundScanningJob == null) {
            backgroundScanningJob = scheduler.scheduleWithFixedDelay(() -> {
                singleScanInternal(false);
            }, 0, pauseBetweenScans.getSeconds(), TimeUnit.SECONDS);
            logger.debug("Scheduled usb-serial background discovery every {} seconds", pauseBetweenScans.getSeconds());
        }
    }

    /**
     * Stops repeatedly scanning for newly added and removed USB devices. This can be restarted using
     * {@link #startBackgroundScanning()}.
     */
    @Override
    public synchronized void stopBackgroundScanning() {
        logger.debug("Stopping usb-serial background discovery");
        ScheduledFuture<?> currentBackgroundScanningJob = backgroundScanningJob;
        if (currentBackgroundScanningJob != null && !currentBackgroundScanningJob.isCancelled()) {
            if (currentBackgroundScanningJob.cancel(true)) {
                backgroundScanningJob = null;
                logger.debug("Stopped usb-serial background discovery");
            }
        }
    }

    @Override
    public void registerDiscoveryListener(UsbSerialDiscoveryListener listener) {
        discoveryListeners.add(listener);
    }

    @Override
    public void unregisterDiscoveryListener(UsbSerialDiscoveryListener listener) {
        discoveryListeners.remove(listener);
    }

    private void singleScanInternal(boolean announceUnchangedDevices) {
        try {
            Delta<UsbSerialDeviceInformation> delta = deltaUsbSerialScanner.scan();
            announceAddedDevices(delta.getAdded());
            announceRemovedDevices(delta.getRemoved());
            if (announceUnchangedDevices) {
                announceAddedDevices(delta.getUnchanged());
            }
        } catch (IOException e) {
            logger.warn("An IOException prevented a scan for USB serial devices: {}, message {}", e.getClass(),
                    e.getMessage());
        }
    }

    private void announceAddedDevices(Set<UsbSerialDeviceInformation> deviceInfos) {
        for (UsbSerialDeviceInformation deviceInfo : deviceInfos) {
            for (UsbSerialDiscoveryListener listener : discoveryListeners) {
                listener.usbSerialDeviceDiscovered(deviceInfo);
            }
        }
    }

    private void announceRemovedDevices(Set<UsbSerialDeviceInformation> deviceInfos) {
        for (UsbSerialDeviceInformation deviceInfo : deviceInfos) {
            for (UsbSerialDiscoveryListener listener : discoveryListeners) {
                listener.usbSerialDeviceRemoved(deviceInfo);
            }
        }
    }

}
