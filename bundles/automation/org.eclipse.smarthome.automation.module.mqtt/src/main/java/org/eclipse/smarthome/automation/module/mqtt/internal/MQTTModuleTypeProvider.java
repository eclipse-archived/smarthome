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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.module.mqtt.handler.PublishActionHandler;
import org.eclipse.smarthome.automation.module.mqtt.handler.PublishedMessageConditionHandler;
import org.eclipse.smarthome.automation.module.mqtt.handler.PublishedMessageTriggerHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.io.transport.mqtt.MqttServiceObserver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Lists the MQTT system brokers for the user to choose from.
 *
 * @author David Graeff - Initial contribution
 *
 */
@NonNullByDefault()
@Component(immediate = true)
public class MQTTModuleTypeProvider implements ModuleTypeProvider, MqttServiceObserver {
    protected final Set<ProviderChangeListener<ModuleType>> listeners = new HashSet<>();

    @NonNullByDefault({})
    private MqttService mqttService;

    private @Nullable ActionType actionType;
    private @Nullable Locale cachedLocale;

    @Reference
    protected void setMQTTService(MqttService mqttService) {
        this.mqttService = mqttService;
        mqttService.addBrokersListener(this);
    }

    protected void unsetMQTTService(MqttService mqttService) {
        mqttService.removeBrokersListener(this);
        this.mqttService = null;
    }

    @SuppressWarnings("unchecked")
    @NonNullByDefault({}) // TODO Until the ModuleTypeProvider interface is fixed
    @Override
    public ModuleType getModuleType(String UID, Locale locale) {
        if (PublishActionHandler.MODULE_TYPE_ID.equals(UID)) {
            return getActionType(locale);
        } else if (PublishedMessageConditionHandler.MODULE_TYPE_ID.equals(UID)) {
            return getConditionType(locale);
        } else if (PublishedMessageTriggerHandler.MODULE_TYPE_ID.equals(UID)) {
            return getTriggerType(locale);
        } else {
            return null;
        }
    }

    @NonNullByDefault({}) // TODO Until the ModuleTypeProvider interface is fixed
    @Override
    public Collection<ModuleType> getModuleTypes(Locale locale) {
        cachedLocale = locale;
        return Stream.of(getActionType(locale), getConditionType(locale), getTriggerType(locale))
                .collect(Collectors.toList());
    }

    private ModuleType getActionType(Locale locale) {
        final ActionType actionType = new ActionType(PublishActionHandler.MODULE_TYPE_ID, getConfigActionDesc(locale), //
                "Publish MQTT message", "Publishes a message to a MQTT topic", //
                null, Visibility.VISIBLE, getInputConditionDesc(locale), getOutputTriggerDesc(locale));
        this.actionType = actionType;
        return actionType;
    }

    private ModuleType getConditionType(Locale locale) {
        return new ConditionType(PublishedMessageConditionHandler.MODULE_TYPE_ID, getConfigConditionDesc(locale),
                "MQTT message published", "Checks if the given MQTT topic has the configured message", //
                null, Visibility.VISIBLE, getInputConditionDesc(locale));
    }

    private ModuleType getTriggerType(Locale locale) {
        return new TriggerType(PublishedMessageTriggerHandler.MODULE_TYPE_ID, getConfigTriggerDesc(locale),
                "MQTT message published", "Triggers whenever the given MQTT topic has a new value", //
                null, Visibility.VISIBLE, getOutputTriggerDesc(locale));
    }

    private @Nullable List<Output> getOutputTriggerDesc(Locale locale) {
        Output topicName = new Output(MQTTModuleConstants.TOPIC_NAME_TYPE, MQTTModuleConstants.TOPIC_NAME_TYPE, //
                "Topic", "A topic that was triggered from", null, null, null);
        Output topicValue = new Output(MQTTModuleConstants.TOPIC_VALUE_TYPE, MQTTModuleConstants.TOPIC_VALUE_TYPE, //
                "Topic", "The received topic value if triggered from a topic", null, null, null);
        return Stream.of(topicName, topicValue).collect(Collectors.toList());
    }

    private @Nullable List<Input> getInputConditionDesc(Locale locale) {
        Input topicName = new Input(MQTTModuleConstants.TOPIC_NAME_TYPE, MQTTModuleConstants.TOPIC_NAME_TYPE, //
                "Topic", "A topic that was triggered from", null, false, null, null);
        Input topicValue = new Input(MQTTModuleConstants.TOPIC_VALUE_TYPE, MQTTModuleConstants.TOPIC_VALUE_TYPE, //
                "Topic", "The received topic value if triggered from a topic", null, false, null, null);
        return Stream.of(topicName, topicValue).collect(Collectors.toList());
    }

    private List<ConfigDescriptionParameter> getConfigActionDesc(Locale locale) {
        ConfigDescriptionParameter paramBroker = ConfigDescriptionParameterBuilder
                .create(PublishActionHandler.CFG_BROKER, Type.TEXT).withRequired(true).withLabel("Broker")
                .withDescription("The broker to publish to").withOptions(getSystemBrokerNames())
                .withLimitToOptions(true).build();
        ConfigDescriptionParameter paramMessage = ConfigDescriptionParameterBuilder
                .create(PublishActionHandler.CFG_MESSAGE, Type.TEXT).withRequired(true).withLabel("Message")
                .withDescription("The message to publish").build();
        ConfigDescriptionParameter paramRetained = ConfigDescriptionParameterBuilder
                .create(PublishActionHandler.CFG_RETAINED, Type.BOOLEAN).withAdvanced(true).withDefault("true")
                .withLabel("Retained").withDescription("A non-retained message disappears after being published")
                .build();
        ConfigDescriptionParameter paramTimeout = ConfigDescriptionParameterBuilder
                .create(PublishActionHandler.CFG_TIMEOUT, Type.INTEGER).withAdvanced(true).withDefault("500")
                .withMinimum(BigDecimal.valueOf(0)).withMaximum(BigDecimal.valueOf(1000)).withLabel("Timeout")
                .withDescription("Timeout in milliseconds").build();
        return Stream.of(paramBroker, paramMessage, paramRetained, paramTimeout, getTopicConfigDescParam(locale))
                .collect(Collectors.toList());
    }

    private List<ConfigDescriptionParameter> getConfigConditionDesc(Locale locale) {
        ConfigDescriptionParameter param1 = ConfigDescriptionParameterBuilder
                .create(PublishedMessageConditionHandler.CFG_PAYLOAD, Type.TEXT).withRequired(true)
                .withLabel("Expected").withDescription("The expected message on the given topic").build();
        return Stream.of(param1, getTopicConfigDescParam(locale)).collect(Collectors.toList());
    }

    private List<ConfigDescriptionParameter> getConfigTriggerDesc(Locale locale) {
        return Stream.of(getTopicConfigDescParam(locale)).collect(Collectors.toList());
    }

    private ConfigDescriptionParameter getTopicConfigDescParam(Locale locale) {
        ConfigDescriptionParameter param2 = ConfigDescriptionParameterBuilder
                .create(PublishActionHandler.CFG_TOPIC, Type.TEXT).withRequired(true).withLabel("Topic")
                .withDescription("the MQTT topic").build();
        return param2;
    }

    /**
     * This method creates one option for every MQTT system broker connection.
     *
     * @return a list of parameter options representing the connection
     */
    private List<ParameterOption> getSystemBrokerNames() {
        List<ParameterOption> options = new ArrayList<>();
        for (Entry<String, MqttBrokerConnection> entry : mqttService.getAllBrokerConnections().entrySet()) {
            options.add(new ParameterOption(entry.getKey(), entry.getValue().getHost()));
        }
        return options;
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        listeners.remove(listener);
    }

    @Override
    public Collection<ModuleType> getAll() {
        return getModuleTypes(null);
    }

    @Override
    public void brokerAdded(String brokerID, MqttBrokerConnection broker) {
        updateActionType();
    }

    @Override
    public void brokerRemoved(String brokerID, MqttBrokerConnection broker) {
        updateActionType();
    }

    // A broker connection got added or removed. Notify listeners about updated ModuleTypes.
    private void updateActionType() {
        // Only the action type requires a specific broker connection
        final ModuleType oldAction = this.actionType;
        final Locale locale = this.cachedLocale;
        if (oldAction == null || locale == null) {
            return;
        }

        listeners.forEach(l -> l.updated(this, oldAction, getActionType(locale)));
    }
}
