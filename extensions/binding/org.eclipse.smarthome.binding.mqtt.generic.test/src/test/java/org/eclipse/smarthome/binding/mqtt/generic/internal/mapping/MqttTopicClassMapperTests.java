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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests cases for {@link MqttTopicClassMapper}.
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
    private static class Bean {
        @SuppressWarnings("unused")
        public transient String ignoreTransient = "";
        @SuppressWarnings("unused")
        public final String ignoreFinal = "";

        public @TestValue("string") String aString;
        public @TestValue("false") Boolean aBoolean;
        public @TestValue("10") Long aLong;
        public @TestValue("10") Integer aInteger;
        public @TestValue("10") BigDecimal aDecimal;

        public @TestValue("10") @TopicPrefix("a") int Int = 24;
        public @TestValue("false") boolean aBool = true;
        public @TestValue("abc,def") @MapToField(splitCharacter = ",") String[] properties;

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

        public @TestValue("integer") @MapToField(suffix = "_") DataTypeEnum datatype = DataTypeEnum.unknown;
    };

    Bean bean = new Bean();

    @Mock
    MqttBrokerConnection connection;

    @Mock
    ScheduledExecutorService executor;

    @Mock
    FieldMqttMessageSubscriber fieldSubscriber;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
    }

    @Test
    public void subscribeToCorrectFields() throws MqttException {
        MqttTopicClassMapper m = spy(new MqttTopicClassMapper(connection, executor));
        doReturn(CompletableFuture.completedFuture(true)).when(fieldSubscriber).start(any(), anyInt());
        doReturn(fieldSubscriber).when(m).createSubscriber(any(), any(), any(), any());

        // Subscribe now to all fields
        CompletableFuture<@NonNull Void> future = m.subscribe("homie/device123", bean, null, 10);
        assertThat(future.isDone(), is(true));
        assertThat(m.subscriptions.size(), is(10));
    }

    @Test
    public void subscribeAndReceive() throws MqttException, IllegalArgumentException, IllegalAccessException {
        MqttTopicClassMapper m = spy(new MqttTopicClassMapper(connection, executor));

        // Subscribe now to all fields
        CompletableFuture<@NonNull Void> future = m.subscribe("homie/device123", bean, null, 10);
        assertThat(future.isDone(), is(false));

        // Assign each field the value of the test annotation via the processMessage method
        for (FieldMqttMessageSubscriber f : m.subscriptions) {
            TestValue annotation = f.field.getAnnotation(TestValue.class);
            f.processMessage(f.topic, annotation.value().getBytes());
            // Check each value if the assignment worked
            if (!f.field.getType().isArray()) {
                assertNotNull(f.field.getName() + " is null", f.field.get(bean));
                // Consider if a mapToField was used that would manipulate the received value
                MapToField mapToField = f.field.getAnnotation(MapToField.class);
                @SuppressWarnings("null")
                String prefix = mapToField != null ? mapToField.prefix() : "";
                @SuppressWarnings("null")
                String suffix = mapToField != null ? mapToField.suffix() : "";
                assertThat(f.field.get(bean).toString(), is(prefix + annotation.value() + suffix));
            } else {
                assertThat(Stream.of((String[]) f.field.get(bean)).reduce((v, i) -> v + "," + i).orElse(""),
                        is(annotation.value()));
            }
        }

        assertThat(future.isDone(), is(true));
    }

    @Test
    public void TimeoutIfNoMessageReceive()
            throws InterruptedException, NoSuchFieldException, SecurityException, MqttException {
        Semaphore s = new Semaphore(1);
        s.acquire();
        final FieldChanged changed = (field, name, value) -> s.release();
        final Field field = Bean.class.getField("Int");
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        FieldMqttMessageSubscriber subscriber = new FieldMqttMessageSubscriber(scheduler, field, bean,
                "homie/device123", changed);
        subscriber.start(connection, 10);
        s.tryAcquire(50, TimeUnit.MILLISECONDS);
    }

}
