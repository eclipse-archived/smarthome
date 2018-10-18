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

import static java.lang.annotation.ElementType.FIELD;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.AbstractMqttAttributeClass.AttributeChanged;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;

/**
 * Tests cases for {@link AbstractMqttAttributeClass}.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttTopicClassMapperTests {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ FIELD })
    private @interface TestValue {
        String value() default "";
    }

    @TopicPrefix
    public static class Attributes extends AbstractMqttAttributeClass {
        public transient String ignoreTransient = "";
        public final String ignoreFinal = "";

        public @TestValue("string") String aString;
        public @TestValue("false") Boolean aBoolean;
        public @TestValue("10") Long aLong;
        public @TestValue("10") Integer aInteger;
        public @TestValue("10") BigDecimal aDecimal;

        public @TestValue("10") @TopicPrefix("a") int Int = 24;
        public @TestValue("false") boolean aBool = true;
        public @TestValue("abc,def") @MQTTvalueTransform(splitCharacter = ",") String[] properties;

        public enum ReadyState {
            unknown,
            init,
            ready,
        }

        public @TestValue("init") ReadyState state = ReadyState.unknown;

        public enum DataTypeEnum {
            unknown,
            integer_,
            float_,
        }

        public @TestValue("integer") @MQTTvalueTransform(suffix = "_") DataTypeEnum datatype = DataTypeEnum.unknown;

        @Override
        public @NonNull Object getFieldsOf() {
            return this;
        }
    };

    @Mock
    MqttBrokerConnection connection;

    @Mock
    ScheduledExecutorService executor;

    @Mock
    AttributeChanged fieldChangedObserver;

    @Spy
    Object countInjectedFields = new Object();
    int injectedFields = 0;

    // A completed future is returned for a subscribe call to the attributes
    final CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());
        injectedFields = (int) Stream.of(countInjectedFields.getClass().getDeclaredFields())
                .filter(AbstractMqttAttributeClass::filterField).count();
    }

    public Object createSubscriberAnswer(InvocationOnMock invocation) {
        final AbstractMqttAttributeClass attributes = (AbstractMqttAttributeClass) invocation.getMock();
        final ScheduledExecutorService scheduler = (ScheduledExecutorService) invocation.getArguments()[0];
        final Field field = (Field) invocation.getArguments()[1];
        final String topic = (String) invocation.getArguments()[2];
        final boolean mandatory = (boolean) invocation.getArguments()[3];
        final SubscribeFieldToMQTTtopic s = spy(
                new SubscribeFieldToMQTTtopic(scheduler, field, attributes, topic, mandatory));
        doReturn(CompletableFuture.completedFuture(true)).when(s).subscribeAndReceive(any(), anyInt());
        return s;
    }

    @Test
    public void subscribeToCorrectFields() {
        Attributes attributes = spy(new Attributes());

        doAnswer(this::createSubscriberAnswer).when(attributes).createSubscriber(any(), any(), anyString(),
                anyBoolean());

        // Subscribe now to all fields
        CompletableFuture<Void> future = attributes.subscribeAndReceive(connection, executor, "homie/device123", null,
                10);
        assertThat(future.isDone(), is(true));
        assertThat(attributes.subscriptions.size(), is(10 + injectedFields));
    }

    // TODO timeout

    @SuppressWarnings({ "null", "unused" })
    @Test
    public void subscribeAndReceive() throws IllegalArgumentException, IllegalAccessException {
        final Attributes attributes = spy(new Attributes());

        doAnswer(this::createSubscriberAnswer).when(attributes).createSubscriber(any(), any(), anyString(),
                anyBoolean());

        verify(connection, times(0)).subscribe(anyString(), any());

        // Subscribe now to all fields
        CompletableFuture<Void> future = attributes.subscribeAndReceive(connection, executor, "homie/device123",
                fieldChangedObserver, 10);
        assertThat(future.isDone(), is(true));

        // We expect 10 subscriptions now
        assertThat(attributes.subscriptions.size(), is(10 + injectedFields));

        int loopCounter = 0;

        // Assign each field the value of the test annotation via the processMessage method
        for (SubscribeFieldToMQTTtopic f : attributes.subscriptions) {
            @Nullable
            TestValue annotation = f.field.getAnnotation(TestValue.class);
            // A non-annotated field means a Mockito injected field.
            // Ignore that and complete the corresponding future.
            if (annotation == null) {
                f.future.complete(null);
                continue;
            }

            verify(f).subscribeAndReceive(any(), anyInt());

            // Simulate a received MQTT value and use the annotation data as input.
            f.processMessage(f.topic, annotation.value().getBytes());
            verify(fieldChangedObserver, times(++loopCounter)).attributeChanged(any(), any(), any(), any(),
                    anyBoolean());

            // Check each value if the assignment worked
            if (!f.field.getType().isArray()) {
                assertNotNull(f.field.getName() + " is null", f.field.get(attributes));
                // Consider if a mapToField was used that would manipulate the received value
                MQTTvalueTransform mapToField = f.field.getAnnotation(MQTTvalueTransform.class);
                String prefix = mapToField != null ? mapToField.prefix() : "";
                String suffix = mapToField != null ? mapToField.suffix() : "";
                assertThat(f.field.get(attributes).toString(), is(prefix + annotation.value() + suffix));
            } else {
                assertThat(Stream.of((String[]) f.field.get(attributes)).reduce((v, i) -> v + "," + i).orElse(""),
                        is(annotation.value()));
            }
        }

        assertThat(future.isDone(), is(true));
    }
}
