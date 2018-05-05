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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.internal.MqttThingID;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.NetUtil;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
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
    private static final int TIMEOUT_IN_MS = 700;

    private final Logger logger = LoggerFactory.getLogger(NetworkDiscoveryService.class);

    private int scannedIPcount = 0;

    public NetworkDiscoveryService() {
        super(Collections.singleton(MqttBindingConstants.BRIDGE_TYPE_BROKER), 0);
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
     * Explicitly override getScanTimeout() instead of using the constructor.
     * This allows to mock the timeout for tests.
     */
    @Override
    public int getScanTimeout() {
        return TIMEOUT_IN_MS;
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
            o[i * 2] = new MqttBrokerConnection("ssl://" + networkIPs.get(i), 8883, false, "testssl");
            o[i * 2].setTimeoutExecutor(scheduler, 100);
            o[i * 2 + 1] = new MqttBrokerConnection("tcp://" + networkIPs.get(i), 1883, false, "test");
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
     * @throws ConfigurationException If the IP cannot be used by the {@link MqttBrokerConnection}, this exception will
     *             be thrown.
     * @throws MqttException Throws if the connection to the target is not a valid Mqtt connection.
     * @throws InterruptedException Throws if interrupted while waiting for the Mqtt connection to establish.
     */
    protected void scanTarget(MqttBrokerConnection testConnection) {
        testConnection.start().thenAccept(v -> {
            if (v == false) {
                return;
            }
            logger.trace("Found service device at {}:{}", testConnection.getHost(),
                    String.valueOf(testConnection.getPort()));

            Map<String, Object> properties = new HashMap<>();
            properties.put("host", testConnection.getHost());
            properties.put("port", testConnection.getPort());

            ThingUID thingUID = MqttThingID.getThingUID(testConnection.getHost(), testConnection.getPort());
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withTTL(120).withProperties(properties)
                    .withRepresentationProperty("host").withLabel("MQTT Broker").build());
            testConnection.stop();
        });
    }

    /**
     * Increase a counter and calls stopScan() if counter equals total.
     *
     * @param total The total size
     */
    synchronized protected void stopScanIfAllScanned(int total) {
        scannedIPcount += 1;
        if (scannedIPcount == total) {
            logger.trace("Scan of {} IPs successful", scannedIPcount);
            stopScan();
        }
    }

    /**
     * Return a list of IP v4 addresses to be scanned for the discovery
     */
    protected List<String> getScannableIPs() {
        return NetUtil.getAllInterfaceAddresses().stream().filter(i -> i.getAddress() instanceof Inet4Address)
                .map(interfaceIP -> Arrays
                        .asList(new SubnetUtils(interfaceIP.getAddress().getHostAddress()).getInfo().getAllAddresses())
                        .subList(0, MAX_IPS_PER_INTERFACE))
                .flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Starts the DiscoveryThread for each IP on each interface on the network
     */
    @Override
    protected void startScan() {
        removeOlderResults(getTimestampOfLastScan(), null);
        logger.trace("Starting Discovery");

        List<String> networkIPs = getScannableIPs();

        // We perform two scans per IP (plain/secure)
        scannedIPcount = 0;

        // Check localhost first
        networkIPs.add(0, "127.0.0.1");
        final int totalScans = networkIPs.size() * 2;

        // Create necessary objects to test
        MqttBrokerConnection o[];
        try {
            o = createTestConnections(networkIPs);
        } catch (ConfigurationException e) {
            logger.debug("Could not create MqttBrokerConnection object", e);
            stopScan();
            return;
        }

        for (MqttBrokerConnection c : o) {
            scanTarget(c);
            stopScanIfAllScanned(totalScans);
        }
    }
}
