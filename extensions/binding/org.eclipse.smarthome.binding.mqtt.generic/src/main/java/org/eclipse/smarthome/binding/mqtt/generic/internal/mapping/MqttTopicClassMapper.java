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
package org.eclipse.smarthome.binding.mqtt.generic.internal.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;

/**
 * Takes a java object, and subscribes to all topics that are named like the fields of that object.
 * The fields are kept in sync with the brokers topic states. The
 * {@link #subscribe(MqttBrokerConnection, String, Object, Map)}
 * method returns a future that is completed as soon as all subscriptions are performed and values are received for the
 * first time. Each subscription uses a separate timeout timer that fires if a topic is not known on the current broker
 * connection. The default timeout is 200ms.
 * <p>
 * The given object, called bean in this context, can consist of all basic java types boolean, int, double, long,
 * String, respective object wrappers like Integer, Double, Long, the BigDecimal type and Enum types. Enums need to be
 * declared within the bean class though. Arrays like String[] are supported as well, but require an annotation because
 * the separator needs to be known.
 * </p>
 * A topic prefix can be defined for the entire class or for a single field. A field annotation overwrites a class
 * annotation.
 *
 * An example:
 *
 * <pre>
 * &#64;TopicPrefix("$")
 * class Bean {
 *    public String testString;
 *    public @MapToField(splitCharacter=",") String[] multipleStrings;
 *
 *    public int anInt = 2;
 *
 *    public enum AnEnum {
 *      Value1,
 *      Value2
 *    };
 *    public AnEnum anEnum = AnEnum.Value1;
 *
 *    public BigDecimal aDecimalValue
 * };
 * </pre>
 *
 * You would use this class in this way:
 *
 * <pre>
 * Bean bean = new Bean();
 * MqttTopicClassMapper mapper = new MqttTopicClassMapper(new ScheduledExecutorService());
 * mapper.subscribe(connection, "mqtt/topic/bean", null).thenRun(() -> System.out.println("subscribed"));
 * </pre>
 *
 * The above bean class would end up with subscriptions to "mqtt/topic/bean/$testString",
 * "mqtt/topic/bean/$multipleStrings", "mqtt/topic/bean/$anInt" and so on. It is assumed that all MQTT messages are
 * UTF-8 strings.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MqttTopicClassMapper {
    protected List<FieldMqttMessageSubscriber> subscriptions = new ArrayList<>();
    public final MqttBrokerConnection connection;
    private final ScheduledExecutorService scheduler;

    public MqttTopicClassMapper(MqttBrokerConnection connection, ScheduledExecutorService scheduler) {
        this.connection = connection;
        this.scheduler = scheduler;
    }

    /**
     * Return fields of the given class as well as all super classes.
     *
     * @param clazz The class
     * @return A list of Field objects
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();

        Class<?> currentClass = clazz;
        while (currentClass != null) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * Unsubscribe from all subscriptions managed by this class object. A {@link MqttTopicClassMapper} may have
     * multiple bean objects registered. All subscriptions will be cancelled.
     *
     * @return Returns a future that completes as soon as all unsubscriptions have been performed.
     */
    public CompletableFuture<Void> unsubscribeAll() {
        List<CompletableFuture<Boolean>> futures = subscriptions.stream().map(c -> connection.unsubscribe(c.topic, c))
                .collect(Collectors.toList());
        subscriptions.clear();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
    }

    /**
     * Unsubscribe from all topics of the given object bean.
     *
     * @param connection A broker connection to remove the subscriptions from.
     * @param objectWithFields A bean class
     * @return Returns a future that completes as soon as all unsubscriptions have been performed.
     */
    public CompletableFuture<Void> unsubscribe(Object objectWithFields) {
        List<CompletableFuture<Boolean>> unsubscribe = new ArrayList<>();
        subscriptions.removeIf(m -> {
            boolean same = m.objectWithFields == objectWithFields;
            if (same) {
                unsubscribe.add(connection.unsubscribe(m.topic, m));
            }
            return same;
        });
        return CompletableFuture.allOf(unsubscribe.toArray(new CompletableFuture[unsubscribe.size()]));
    }

    /**
     * Subscribe to all subtopics on a MQTT broker connection base topic that match field names of a given java object.
     * The fields will be kept in sync with their respective topics. Optionally, you can register update-observers for
     * specific fields.
     *
     * @param basetopic The base topic. Given a base topic of "base/topic", a field "test" would be registered as
     *            "base/topic/test".
     * @param objectWithFields A java object with public, accessible fields. All final or transient fields are going to
     *            be ignored.
     * @param changeObservers Observers for specific fields.
     * @param timeout Timeout per subscription in milliseconds. The returned future completes after this time
     *            even if no
     *            message has been received for a single MQTT topic.
     * @return Returns a future that completes as soon as values for all subscriptions have been received or have timed
     *         out.
     */
    public CompletableFuture<Void> subscribe(final String basetopic, Object objectWithFields,
            @Nullable Map<String, FieldChanged> changeObservers, int timeout) {

        TopicPrefix topicUsesPrefix = objectWithFields.getClass().getAnnotation(TopicPrefix.class);
        @SuppressWarnings("null")
        final String prefix = (topicUsesPrefix != null) ? topicUsesPrefix.value() : "";

        List<CompletableFuture<Boolean>> futures = new ArrayList<CompletableFuture<Boolean>>();
        List<Field> fields = getAllFields(objectWithFields.getClass());
        for (Field field : fields) {
            // Don't try to write to final fields and ignore transient fields
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            topicUsesPrefix = field.getAnnotation(TopicPrefix.class);
            @SuppressWarnings("null")
            String localPrefix = (topicUsesPrefix != null) ? topicUsesPrefix.value() : prefix;

            final String topic = basetopic + "/" + localPrefix + field.getName();
            final FieldChanged changedObserver = changeObservers != null ? changeObservers.get(field.getName()) : null;

            FieldMqttMessageSubscriber m = createSubscriber(field, objectWithFields, topic, changedObserver);
            futures.add(m.start(connection, timeout));
            subscriptions.add(m);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
    }

    /**
     * Creates a field subscriber for the given field on the given object
     *
     * @param field The field
     * @param objectWithFields The object with a matching field
     * @param topic The full topic to subscribe to
     * @param changedObserver
     * @return
     */
    protected FieldMqttMessageSubscriber createSubscriber(Field field, Object objectWithFields, String topic,
            @Nullable FieldChanged changedObserver) {
        return new FieldMqttMessageSubscriber(scheduler, field, objectWithFields, topic, changedObserver);
    }
}
