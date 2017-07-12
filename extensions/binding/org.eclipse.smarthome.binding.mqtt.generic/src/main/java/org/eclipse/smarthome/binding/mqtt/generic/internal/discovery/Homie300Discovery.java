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
package org.eclipse.smarthome.binding.mqtt.generic.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.generic.internal.discovery.MqttTopicDiscovery.TopicDiscovered;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Homie300Discovery} is responsible for discovering device nodes that follow the
 * Homie 3.x convention (https://github.com/homieiot/convention).
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.mqtthomie")
@NonNullByDefault
public class Homie300Discovery extends AbstractDiscoveryService implements TopicDiscovered {
    private final Logger logger = LoggerFactory.getLogger(Homie300Discovery.class);
    protected @Nullable MqttTopicDiscovery mqttTopicDiscovery;

    public Homie300Discovery() {
        super(Stream.of(MqttBindingConstants.HOMIE300_MQTT_THING).collect(Collectors.toSet()), 500, true);
    }

    @Reference
    public void setThingRegistry(ThingRegistry service) {
        mqttTopicDiscovery = new MqttTopicDiscovery(service, this, "homie/+/$homie");
    }

    public void unsetThingRegistry(@Nullable ThingRegistry service) {
        final MqttTopicDiscovery mqttTopicDiscovery = this.mqttTopicDiscovery;
        if (mqttTopicDiscovery != null) {
            mqttTopicDiscovery.stopBackgroundDiscovery();
            this.mqttTopicDiscovery = null;
        }
    }

    @Override
    protected void startScan() {
        final MqttTopicDiscovery mqttTopicDiscovery = this.mqttTopicDiscovery;
        if (mqttTopicDiscovery != null) {
            mqttTopicDiscovery.startScan();
        }
        stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        final MqttTopicDiscovery mqttTopicDiscovery = this.mqttTopicDiscovery;
        if (mqttTopicDiscovery != null) {
            mqttTopicDiscovery.startBackgroundDiscovery();
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        final MqttTopicDiscovery mqttTopicDiscovery = this.mqttTopicDiscovery;
        if (mqttTopicDiscovery != null) {
            mqttTopicDiscovery.stopBackgroundDiscovery();
        }
    }

    /**
     * @param topic A topic like "homie/mydevice/$homie"
     * @return Returns the "mydevice" part of the example
     */
    public static @Nullable String extractDeviceID(String topic) {
        String[] strings = topic.split("/");
        if (strings.length > 2) {
            return strings[1];
        }
        return null;
    }

    /**
     * Returns true if the version is something like "3.x". We accept
     * version 3 up to but not including version 4 of the homie spec.
     */
    public static boolean checkVersion(String versionString) {
        String[] strings = versionString.split("\\.");
        if (strings.length < 2) {
            return false;
        }
        return strings[0].equals("3");
    }

    @Override
    public void topicDiscovered(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
            String topicValue) {
        if (!checkVersion(topicValue)) {
            logger.trace("Found homie device. But version {} is out of range.", topicValue);
            return;
        }
        String deviceID = extractDeviceID(topic);
        if (deviceID == null) {
            logger.trace("Found homie device. But deviceID {} is invalid.", deviceID);
            return;
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put("deviceid", deviceID);
        thingDiscovered(DiscoveryResultBuilder
                .create(new ThingUID(MqttBindingConstants.HOMIE300_MQTT_THING, connectionBridge, deviceID))
                .withProperties(properties).withRepresentationProperty("deviceid").withLabel("Homie device").build());
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
            String topicValue) {
        String deviceID = extractDeviceID(topic);
        if (deviceID == null) {
            return;
        }
        thingRemoved(new ThingUID(MqttBindingConstants.HOMIE300_MQTT_THING, connectionBridge, deviceID));
    }

}
