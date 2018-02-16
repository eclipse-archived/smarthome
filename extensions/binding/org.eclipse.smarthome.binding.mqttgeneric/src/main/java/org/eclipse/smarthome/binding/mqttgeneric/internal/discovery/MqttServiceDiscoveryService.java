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
package org.eclipse.smarthome.binding.mqttgeneric.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.mqttgeneric.MqttBrokerBindingConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokersObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MqttServiceDiscoveryService} is responsible for discovering devices on
 * the current Network. It uses every Network Interface which is connected to a network.
 * It tries common TCP ports to connect to, ICMP pings and ARP pings.
 *
 * @author David Graeff - Rewritten
 * @author Marc Mettke - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, name = "MqttServiceDiscoveryService")
public class MqttServiceDiscoveryService extends AbstractDiscoveryService implements MqttBrokersObserver {

    private final Logger logger = LoggerFactory.getLogger(MqttServiceDiscoveryService.class);

    private MqttService mqttService;

    public MqttServiceDiscoveryService() {
        super(Collections.singleton(MqttBrokerBindingConstants.BRIDGE_TYPE_CONNECTION), 500, true);
    }

    @Activate
    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Deactivate
    @Override
    protected void deactivate() {
        super.deactivate();
    }

    @Modified
    @Override
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setMqttService(MqttService service) {
        mqttService = service;
        if (isBackgroundDiscoveryEnabled()) {
            startBackgroundDiscovery();
        }
    }

    public void unsetMqttService(MqttService service) {
        mqttService = null;
        if (isBackgroundDiscoveryEnabled()) {
            stopBackgroundDiscovery();
        }
    }

    @Override
    protected void startScan() {
        mqttService.addBrokersListener(this);
        for (MqttBrokerConnection c : mqttService.getAllBrokerConnections()) {
            brokerAdded(c);
        }
        stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (mqttService == null) {
            return;
        }
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (mqttService == null) {
            return;
        }
        mqttService.removeBrokersListener(this);
    }

    public static ThingUID makeThingUID(String brokerName) {
        return new ThingUID(MqttBrokerBindingConstants.BRIDGE_TYPE_CONNECTION, brokerName);
    }

    @Override
    public void brokerAdded(MqttBrokerConnection broker) {
        logger.trace("Found broker connection {}", broker.getName());

        Map<String, Object> properties = new HashMap<>();
        properties.put(MqttBrokerBindingConstants.PARAM_BRIDGE_name, broker.getName());
        ThingUID thingUID = makeThingUID(broker.getName());
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withTTL(120).withProperties(properties)
                .withLabel("Broker connection (" + broker.getUrl() + ")").build());
    }

    @Override
    public void brokerRemoved(MqttBrokerConnection broker) {
        thingRemoved(new ThingUID(MqttBrokerBindingConstants.BRIDGE_TYPE_CONNECTION, broker.getName()));
    }
}
