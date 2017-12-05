/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.*;
import static org.eclipse.smarthome.binding.lifx.internal.util.LifxMessageUtil.randomSourceId;
import static org.eclipse.smarthome.binding.lifx.internal.util.LifxSelectorUtil.*;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetLabelRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetServiceRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetVersionRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Products;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateLabelResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateServiceResponse;
import org.eclipse.smarthome.binding.lifx.internal.protocol.StateVersionResponse;
import org.eclipse.smarthome.binding.lifx.internal.util.LifxSelectorUtil;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightDiscovery} provides support for auto-discovery of LIFX
 * lights.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Karel Goderis - Rewrite for Firmware V2, and remove dependency on external libraries
 * @author Wouter Born - Discover light labels, improve locking, optimize packet handling
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.lifx")
@NonNullByDefault
public class LifxLightDiscovery extends AbstractDiscoveryService {

    private static final String LOG_ID = "Discovery";
    private static final long REFRESH_INTERVAL = TimeUnit.MINUTES.toSeconds(1);
    private static final long SELECTOR_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

    private final Logger logger = LoggerFactory.getLogger(LifxLightDiscovery.class);

    private final Map<MACAddress, @Nullable DiscoveredLight> discoveredLights = new HashMap<>();
    private final long sourceId = randomSourceId();
    private final Supplier<Integer> sequenceNumberSupplier = new LifxSequenceNumberSupplier();

    private @Nullable Selector selector;
    private @Nullable SelectionKey broadcastKey;

    private @Nullable ScheduledFuture<?> discoveryJob;
    private @Nullable ScheduledFuture<?> networkJob;

    private boolean isScanning = false;

    private class DiscoveredLight {

        private MACAddress macAddress;
        private InetSocketAddress socketAddress;
        private String logId;
        private @Nullable String label;
        private @Nullable Products product;
        private long productVersion;
        private boolean supportedProduct = true;
        private LifxSelectorContext selectorContext;

        private long lastRequestTimeMillis;

        public DiscoveredLight(Selector lightSelector, MACAddress macAddress, InetSocketAddress socketAddress,
                String logId, @Nullable SelectionKey unicastKey) {
            this.macAddress = macAddress;
            this.logId = logId;
            this.socketAddress = socketAddress;
            this.selectorContext = new LifxSelectorContext(lightSelector, sourceId, sequenceNumberSupplier, logId,
                    socketAddress, macAddress, broadcastKey, unicastKey);
        }

        public boolean isDataComplete() {
            return label != null && product != null;
        }

        public void cancelUnicastKey() {
            SelectionKey unicastKey = selectorContext.getUnicastKey();
            if (unicastKey != null) {
                cancelKey(unicastKey, selectorContext.getLogId());
            }
        }
    }

    public LifxLightDiscovery() throws IllegalArgumentException {
        super(LifxBindingConstants.SUPPORTED_THING_TYPES, 1, true);
    }

    @Activate
    @Override
    protected void activate(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Modified
    @Override
    protected void modified(@Nullable Map<String, @Nullable Object> configProperties) {
        super.modified(configProperties);
    }

    @Deactivate
    @Override
    protected void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting the LIFX device background discovery");

        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::doScan, 0, REFRESH_INTERVAL, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping LIFX device background discovery");

        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null && !localDiscoveryJob.isCancelled()) {
            localDiscoveryJob.cancel(true);
            discoveryJob = null;
        }

        ScheduledFuture<?> localNetworkJob = networkJob;
        if (localNetworkJob != null && !localNetworkJob.isCancelled()) {
            localNetworkJob.cancel(true);
            networkJob = null;
        }
    }

    @Override
    protected void startScan() {
        doScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    protected void doScan() {
        try {
            if (!isScanning) {
                isScanning = true;
                if (selector != null) {
                    closeSelector(selector, LOG_ID);
                }

                logger.debug("The LIFX discovery service will use '{}' as source identifier",
                        Long.toString(sourceId, 16));

                Selector localSelector = Selector.open();
                selector = localSelector;

                broadcastKey = openBroadcastChannel(localSelector, LOG_ID, BROADCAST_PORT);
                networkJob = scheduler.schedule(this::receiveAndHandlePackets, 0, TimeUnit.MILLISECONDS);

                LifxSelectorContext selectorContext = new LifxSelectorContext(localSelector, sourceId,
                        sequenceNumberSupplier, LOG_ID, broadcastKey);
                broadcastPacket(selectorContext, new GetServiceRequest());
            } else {
                logger.info("A discovery scan for LIFX lights is already underway");
            }
        } catch (Exception e) {
            logger.debug("{} while discovering LIFX lights : {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public void receiveAndHandlePackets() {
        Selector localSelector = selector;

        try {
            if (localSelector == null || !localSelector.isOpen()) {
                logger.debug("Unable to receive and handle packets with null or closed selector");
                return;
            }

            discoveredLights.clear();
            logger.trace("Entering read loop");
            long startStamp = System.currentTimeMillis();

            while (System.currentTimeMillis() - startStamp < SELECTOR_TIMEOUT) {
                int lightCount = discoveredLights.size();
                long selectStamp = System.currentTimeMillis();

                LifxSelectorUtil.receiveAndHandlePackets(localSelector, LOG_ID,
                        (packet, address) -> handlePacket(packet, address));
                requestAdditionalLightData();

                boolean discoveredNewLights = lightCount < discoveredLights.size();
                if (!discoveredNewLights) {
                    boolean preventBusyWaiting = System.currentTimeMillis() - selectStamp < PACKET_INTERVAL;
                    if (preventBusyWaiting) {
                        Thread.sleep(PACKET_INTERVAL);
                    }
                }
            }
            logger.trace("Exited read loop");
        } catch (Exception e) {
            logger.debug("{} while receiving and handling discovery packets: {}", e.getClass().getSimpleName(),
                    e.getMessage(), e);
        } finally {
            LifxSelectorUtil.closeSelector(localSelector, LOG_ID);
            selector = null;
            isScanning = false;
        }
    }

    private void requestAdditionalLightData() {
        // Iterate through the discovered lights that have to be set up, and the packets that have to be sent
        // Workaround to avoid a ConcurrentModifictionException on the selector.SelectedKeys() Set
        for (DiscoveredLight light : discoveredLights.values()) {
            if (light == null) {
                continue;
            }
            boolean waitingForLightResponse = System.currentTimeMillis() - light.lastRequestTimeMillis < 200;

            if (light.supportedProduct && !light.isDataComplete() && !waitingForLightResponse) {
                if (light.product == null) {
                    sendPacket(light.selectorContext, new GetVersionRequest());
                }
                if (light.label == null) {
                    sendPacket(light.selectorContext, new GetLabelRequest());
                }
                light.lastRequestTimeMillis = System.currentTimeMillis();
            }
        }
    }

    private void handlePacket(Packet packet, InetSocketAddress address) {
        logger.trace("Discovery : Packet type '{}' received from '{}' for '{}' with sequence '{}' and source '{}'",
                new Object[] { packet.getClass().getSimpleName(), address.toString(), packet.getTarget().getHex(),
                        packet.getSequence(), Long.toString(packet.getSource(), 16) });

        if (packet.getSource() == sourceId || packet.getSource() == 0) {
            MACAddress macAddress = packet.getTarget();
            DiscoveredLight light = discoveredLights.get(macAddress);

            if (packet instanceof StateServiceResponse) {
                int port = (int) ((StateServiceResponse) packet).getPort();
                if (port != 0) {
                    try {
                        InetSocketAddress socketAddress = new InetSocketAddress(address.getAddress(), port);
                        if (light == null || (!socketAddress.equals(light.socketAddress))) {
                            if (light != null) {
                                light.cancelUnicastKey();
                            }

                            Selector lightSelector = selector;
                            if (lightSelector != null) {
                                String logId = getLogId(macAddress, socketAddress);
                                light = new DiscoveredLight(lightSelector, macAddress, socketAddress, logId,
                                        openUnicastChannel(lightSelector, logId, socketAddress));
                                discoveredLights.put(macAddress, light);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("{} while connecting to IP address: {}", e.getClass().getSimpleName(),
                                e.getMessage());
                        return;
                    }
                }
            } else if (light != null) {
                if (packet instanceof StateLabelResponse) {
                    light.label = ((StateLabelResponse) packet).getLabel().trim();
                } else if (packet instanceof StateVersionResponse) {
                    try {
                        light.product = Products.getProductFromProductID(((StateVersionResponse) packet).getProduct());
                        light.productVersion = ((StateVersionResponse) packet).getVersion();
                    } catch (IllegalArgumentException e) {
                        logger.debug("Discovered an unsupported light ({}): {}", light.macAddress.getAsLabel(),
                                e.getMessage());
                        light.supportedProduct = false;
                    }
                }
            }

            if (light != null && light.isDataComplete()) {
                try {
                    thingDiscovered(createDiscoveryResult(light));
                } catch (IllegalArgumentException e) {
                    logger.trace("{} while creating discovery result of light ({})", e.getClass().getSimpleName(),
                            light.logId, e);
                }
            }
        }
    }

    private DiscoveryResult createDiscoveryResult(DiscoveredLight light) throws IllegalArgumentException {
        Products product = light.product;
        if (product == null) {
            throw new IllegalArgumentException("Product of discovered light is null");
        }

        String macAsLabel = light.macAddress.getAsLabel();
        ThingUID thingUID = new ThingUID(product.getThingTypeUID(), macAsLabel);

        String label = light.label;
        if (StringUtils.isBlank(label)) {
            label = product.getName();
        }

        logger.trace("Discovered a LIFX light: {}", label);

        DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID);
        builder.withRepresentationProperty(LifxBindingConstants.PROPERTY_MAC_ADDRESS);
        builder.withLabel(label);

        builder.withProperty(LifxBindingConstants.CONFIG_PROPERTY_DEVICE_ID, macAsLabel);
        builder.withProperty(LifxBindingConstants.PROPERTY_MAC_ADDRESS, macAsLabel);
        builder.withProperty(LifxBindingConstants.PROPERTY_PRODUCT_ID, product.getProduct());
        builder.withProperty(LifxBindingConstants.PROPERTY_PRODUCT_NAME, product.getName());
        builder.withProperty(LifxBindingConstants.PROPERTY_PRODUCT_VERSION, light.productVersion);
        builder.withProperty(LifxBindingConstants.PROPERTY_VENDOR_ID, product.getVendor());
        builder.withProperty(LifxBindingConstants.PROPERTY_VENDOR_NAME, product.getVendorName());

        return builder.build();
    }

}
