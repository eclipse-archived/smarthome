package org.eclipse.smarthome.binding.mqtt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.annotation.ActionInput;
import org.eclipse.smarthome.automation.annotation.ActionScope;
import org.eclipse.smarthome.automation.annotation.RuleAction;
import org.eclipse.smarthome.binding.mqtt.handler.AbstractBrokerHandler;
import org.eclipse.smarthome.core.thing.binding.AnnotatedActionThingHandlerService;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the
 * publishMQTT action.
 *
 * @author David Graeff - Initial contribution
 */
@ActionScope(name = "binding.mqtt")
@Component(immediate = false, service = { AnnotatedActionThingHandlerService.class })
@NonNullByDefault
public class ActionService implements AnnotatedActionThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(ActionService.class);
    private @Nullable AbstractBrokerHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (AbstractBrokerHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    void publishMQTT(
            @ActionInput(name = "topic", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc") String topic,
            @ActionInput(name = "value", label = "@text/actionInputValueLabel", description = "@text/actionInputValueDesc") String value) {
        AbstractBrokerHandler brokerHandler = handler;
        if (brokerHandler == null) {
            logger.warn("MQTT Action service ThingHandler is null!");
            return;
        }
        MqttBrokerConnection connection = brokerHandler.getConnection();
        if (connection == null) {
            logger.warn("MQTT Action service ThingHandler connection is null!");
            return;
        }
        connection.publish(topic, value.getBytes()).thenRun(() -> {
            logger.debug("MQTT publish to {} performed", topic);
        }).exceptionally(e -> {
            logger.warn("MQTT publish to {} failed!", topic);
            return null;
        });
    }
}
