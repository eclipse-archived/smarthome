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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant.TopicToID;
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
 * The {@link HomeAssistantDiscovery} is responsible for discovering device nodes that follow the
 * Home Assistant MQTT discovery convention (https://www.home-assistant.io/docs/mqtt/discovery/).
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.mqttha")
@NonNullByDefault
public class HomeAssistantDiscovery extends AbstractDiscoveryService implements TopicDiscovered {
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantDiscovery.class);
    public static final Map<String, String> HA_COMP_TO_NAME = new TreeMap<String, String>();
    {
        HA_COMP_TO_NAME.put("alarm_control_panel", "Alarm Control Panel");
        HA_COMP_TO_NAME.put("binary_sensor", "Sensor");
        HA_COMP_TO_NAME.put("camera", "Camera");
        HA_COMP_TO_NAME.put("cover", "Blind");
        HA_COMP_TO_NAME.put("fan", "Fan");
        HA_COMP_TO_NAME.put("climate", "Climate Control");
        HA_COMP_TO_NAME.put("light", "Light");
        HA_COMP_TO_NAME.put("lock", "Lock");
        HA_COMP_TO_NAME.put("sensor", "Sensor");
        HA_COMP_TO_NAME.put("switch", "Switch");
    }
    final String baseTopic = "homeassistant";

    protected @Nullable MqttTopicDiscovery mqttTopicDiscovery;
    protected Map<String, Set<String>> componentsPerThingID = new TreeMap<>();

    public HomeAssistantDiscovery() {
        super(Stream.of(MqttBindingConstants.HOMEASSISTANT_MQTT_THING).collect(Collectors.toSet()), 500, true);
    }

    @Reference
    public void setThingRegistry(ThingRegistry service) {
        mqttTopicDiscovery = new MqttTopicDiscovery(service, this, baseTopic + "/#");
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
    protected synchronized void stopScan() {
        componentsPerThingID.clear();
        super.stopScan();
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
        componentsPerThingID.clear();
    }

    /**
     * @param topic A topic like "homeassistant/binary_sensor/garden/config"
     * @return Returns the "mydevice" part of the example
     */
    public static TopicToID determineTopicParts(String topic) {
        return new TopicToID(topic);
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
        // For HomeAssistant we need to subscribe to a wildcard topic, because topics can either be:
        // homeassistant/<component>/<node_id>/<object_id>/config OR homeassistant/<component>/<object_id>/config.
        // We check for the last part to filter all non-config topics out.
        if (!topic.endsWith("/config")) {
            return;
        }

        // We will of course find multiple of the same unique Thing IDs, for each different component another one.
        // Therefore the components are assembled into a list and given to the DiscoveryResult label for the user to
        // easily
        // recognize object capabilities.
        TopicToID topicParts = determineTopicParts(topic);
        final String thingID = topicParts.getThingID();
        final ThingUID thingUID = new ThingUID(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, connectionBridge,
                thingID);

        // We need to keep track of already found component topics for a specific object_id/node_id
        Set<String> components = componentsPerThingID.getOrDefault(thingID, new HashSet<>());
        if (components.contains(topicParts.component)) {
            logger.trace("Discovered an already known component {}", topicParts.component);
            return; // If we already know about this object component, ignore the discovered topic.
        }
        components.add(topicParts.component);
        componentsPerThingID.put(thingID, components);

        final String componentNames = components.stream().map(c -> HA_COMP_TO_NAME.getOrDefault(c, c))
                .collect(Collectors.joining(","));

        Map<String, Object> properties = new HashMap<>();
        properties.put("objectid", topicParts.objectID);
        properties.put("nodeid", topicParts.nodeID);
        properties.put("basetopic", baseTopic);
        properties.put("deviceid", thingID);
        // First remove an already discovered thing with the same ID
        thingRemoved(thingUID);
        // Because we need the new properties map with the updated "components" list
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty("deviceid").withLabel("HomeAssistant MQTT Object (" + componentNames + ")")
                .build());
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
            String topicValue) {
        if (!topic.endsWith("/config")) {
            return;
        }
        final String thingID = determineTopicParts(topic).getThingID();
        componentsPerThingID.remove(thingID);
        thingRemoved(new ThingUID(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, connectionBridge, thingID));
    }

}
