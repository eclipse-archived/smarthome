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
package org.eclipse.smarthome.automation.module.mqtt.internal;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.mqtt.handler.PublishActionHandler;
import org.eclipse.smarthome.automation.module.mqtt.handler.PublishedMessageConditionHandler;
import org.eclipse.smarthome.automation.module.mqtt.handler.PublishedMessageTriggerHandler;
import org.eclipse.smarthome.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
@Component(service = ModuleHandlerFactory.class)
public class MQTTModuleHandlerFactory extends BaseModuleHandlerFactory {

    private static final Collection<String> TYPES = unmodifiableList(asList(PublishActionHandler.MODULE_TYPE_ID,
            PublishedMessageConditionHandler.MODULE_TYPE_ID, PublishedMessageTriggerHandler.MODULE_TYPE_ID));

    @NonNullByDefault({})
    private MqttService mqttService;
    @NonNullByDefault({})
    protected MQTTTopicDiscoveryService mqttTopicDiscovery;

    @Override
    @Deactivate
    protected void deactivate() {
        super.deactivate();
    }

    @Override
    public Collection<String> getTypes() {
        return TYPES;
    }

    @Reference
    public void setMQTTTopicDiscoveryService(MQTTTopicDiscoveryService service) {
        mqttTopicDiscovery = service;
    }

    public void unsetMQTTTopicDiscoveryService(@Nullable MQTTTopicDiscoveryService service) {
        this.mqttTopicDiscovery = null;
    }

    @Override
    protected @Nullable ModuleHandler internalCreate(Module module, String ruleUID) {
        switch (module.getTypeUID()) {
            case PublishActionHandler.MODULE_TYPE_ID:
                return new PublishActionHandler((Action) module, mqttService);
            case PublishedMessageConditionHandler.MODULE_TYPE_ID:
                return new PublishedMessageConditionHandler((Condition) module, mqttTopicDiscovery);
            case PublishedMessageTriggerHandler.MODULE_TYPE_ID:
                return new PublishedMessageTriggerHandler((Trigger) module, mqttTopicDiscovery);
            default:
                break;
        }
        return null;
    }

    @Reference
    protected void setMQTTService(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    protected void unsetMQTTService(MqttService mqttService) {
        this.mqttService = null;
    }
}
