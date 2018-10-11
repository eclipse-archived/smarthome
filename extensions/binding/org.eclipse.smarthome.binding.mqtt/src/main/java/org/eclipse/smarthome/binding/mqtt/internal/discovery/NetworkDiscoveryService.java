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
package org.eclipse.smarthome.binding.mqtt.internal.discovery;

import java.net.Inet4Address;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.internal.MqttThingID;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.CidrAddress;
import org.eclipse.smarthome.core.net.NetUtil;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkDiscoveryService} is responsible for discovering brokers on
 * the current Network. It uses every Network Interface available.
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.networkmqttbroker")
@NonNullByDefault
public class NetworkDiscoveryService extends AbstractDiscoveryService {
    private static final int MAX_IPS_PER_INTERFACE = 255;

    private final Logger logger = LoggerFactory.getLogger(NetworkDiscoveryService.class);

    protected MqttBrokerConnection testConnections[] = new MqttBrokerConnection[0];
    protected int foundBrokers = 0;

    public NetworkDiscoveryService() {
        super(Collections.singleton(MqttBindingConstants.BRIDGE_TYPE_BROKER), 7, false);
    }

    @Override
    @Activate
    protected void activate(@Nullable Map<String, @Nullable Object> config) {
        super.activate(config);
    };

    @Override
    @Deactivate
    protected void deactivate() {
        super.deactivate();
    }

    /**
     * Creates all required {@link MqttBrokerConnection} for performing connection attempts
     * on the given URLs.
     *
     * Package internal, for testing.
     *
     * @param networkIPs The network IP addresses.
     * @return Returns an array of broker connections with the names "test".
     * @throws ConfigurationException If the URL is invalid, this exception is thrown
     */
    protected MqttBrokerConnection[] createTestConnections(List<String> networkIPs) throws ConfigurationException {

        MqttBrokerConnection o[] = new MqttBrokerConnection[networkIPs.size() * 2];
        for (int i = 0; i < networkIPs.size(); ++i) {
            o[i * 2] = new MqttBrokerConnection(networkIPs.get(i), 8883, true, "testssl");
            o[i * 2].setTimeoutExecutor(scheduler, 100);
            o[i * 2 + 1] = new MqttBrokerConnection(networkIPs.get(i), 1883, false, "test");
            o[i * 2 + 1].setTimeoutExecutor(scheduler, 100);
        }
        return o;
    }

    /**
     * Scans the target host/IP by trying to connect as an Mqtt client. Calls
     * newDevice() if a broker is found. Increments scannedIPcount by one. If
     * the total subnet size is equal scannedIPcount, stopScan() will be called,
     * to signal the scan has finished.
     *
     * @param testConnection The destination to test
     * @return A future that completes as soon as the connection attempt succeeded or timed out
     */
    protected CompletableFuture<Void> scanTarget(MqttBrokerConnection testConnection) {
        return testConnection.start().thenAccept(v -> {
            testConnection.stop();
            if (v == false) {
                return;
            }
            logger.trace("Found service device at {}:{}", testConnection.getHost(),
                    String.valueOf(testConnection.getPort()));

            Map<String, Object> properties = new HashMap<>();
            properties.put("host", testConnection.getHost());
            properties.put("port", testConnection.getPort());

            ++foundBrokers;
            ThingUID thingUID = MqttThingID.getThingUID(testConnection.getHost(), testConnection.getPort());
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withTTL(120).withProperties(properties)
                    .withRepresentationProperty("host").withLabel("MQTT Broker").build());
        });
    }

    private List<String> addressesForInterface(CidrAddress interfaceIP) {
        String[] addresses = new SubnetUtils(interfaceIP.getAddress().getHostAddress(),
                NetUtil.networkPrefixLengthToNetmask(interfaceIP.getPrefix())).getInfo().getAllAddresses();
        return Stream.of(addresses).limit(MAX_IPS_PER_INTERFACE).collect(Collectors.toList());
    }

    /**
     * Return a list of IP v4 addresses to be scanned for the discovery
     */
    protected List<String> getScannableIPs() {
        return NetUtil.getAllInterfaceAddresses().stream().filter(i -> i.getAddress() instanceof Inet4Address)
                .map(interfaceIP -> addressesForInterface(interfaceIP)).flatMap(List::stream)
                .collect(Collectors.toList());
    }

    protected void finished() {
        logger.info("Found {} Mqtt Broker Connections", foundBrokers);
        stopScan();
        foundBrokers = 0;
    }

    /**
     * Starts the DiscoveryThread for each IP on each interface on the network
     */
    @Override
    protected void startScan() {
        foundBrokers = 0;
        // If a scan is started, while another run is not completed yet, stop all broker connections of the old one
        // first
        if (this.testConnections.length > 0) {
            for (MqttBrokerConnection c : testConnections) {
                c.stop();
            }
            this.testConnections = new MqttBrokerConnection[0];
        }

        removeOlderResults(getTimestampOfLastScan(), null);
        logger.trace("Starting Discovery");

        List<String> networkIPs = getScannableIPs();
        if (networkIPs.isEmpty()) {
            logger.warn("Could not determine subnet IP addresses for MQTT Broker Network discovery");
        }

        // Check localhost first
        networkIPs.add(0, "127.0.0.1");

        // Create necessary objects to test
        try {
            testConnections = createTestConnections(networkIPs);
        } catch (ConfigurationException e) {
            logger.debug("Could not create MqttBrokerConnection object", e);
            stopScan();
            return;
        }

        CompletableFuture.allOf(Stream.of(testConnections).map(c -> scanTarget(c)).toArray(CompletableFuture[]::new))
                .thenRun(this::finished);
    }
}
