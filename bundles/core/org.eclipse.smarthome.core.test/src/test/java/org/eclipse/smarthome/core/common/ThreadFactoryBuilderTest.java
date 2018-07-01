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
package org.eclipse.smarthome.core.common;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

import org.junit.Test;

/**
 * Unit tests for the {@link ThreadFactoryBuilder}.
 *
 * @author Henning Sudbrock - initial contribution
 */
public class ThreadFactoryBuilderTest {

    @Test
    public void testThreadFactoryBuilderDefaults() {
        ThreadFactory threadFactory = ThreadFactoryBuilder.create().build();
        Thread thread = threadFactory.newThread(() -> {
        });

        assertThat(thread.getName(), is(notNullValue()));
        assertThat(thread.isDaemon(), is(false));
        assertThat(thread.getUncaughtExceptionHandler(), is(notNullValue()));
    }

    @Test
    public void testThreadFactoryBuilderNamePrefix() {
        ThreadFactory threadFactory = ThreadFactoryBuilder.create().withNamePrefix("hello").build();

        assertThat(threadFactory.newThread(() -> {
        }).getName(), is("ESH-hello-1"));

        assertThat(threadFactory.newThread(() -> {
        }).getName(), is("ESH-hello-2"));

        assertThat(threadFactory.newThread(() -> {
        }).getName(), is("ESH-hello-3"));
    }

    @Test
    public void testThreadFactoryBuilderDaemonize() {
        ThreadFactory threadFactory = ThreadFactoryBuilder.create().withDaemonThreads(true).build();
        assertThat(threadFactory.newThread(() -> {
        }).isDaemon(), is(true));

        threadFactory = ThreadFactoryBuilder.create().withDaemonThreads(false).build();
        assertThat(threadFactory.newThread(() -> {
        }).isDaemon(), is(false));

    }

    @Test
    public void testThreadFactoryBuilderUncaughtExceptionHandler() {
        UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {

            }
        };

        ThreadFactory threadFactory = ThreadFactoryBuilder.create().withUncaughtExceptionHandler(handler).build();

        assertThat(threadFactory.newThread(() -> {
        }).getUncaughtExceptionHandler(), is(handler));
    }

    @Test
    public void testThreadFactoryBuilderPriority() {
        ThreadFactory threadFactory = ThreadFactoryBuilder.create().withPriority(Thread.MIN_PRIORITY).build();
        assertThat(threadFactory.newThread(() -> {
        }).getPriority(), is(Thread.MIN_PRIORITY));

        threadFactory = ThreadFactoryBuilder.create().withPriority(Thread.MAX_PRIORITY).build();
        assertThat(threadFactory.newThread(() -> {
        }).getPriority(), is(Thread.MAX_PRIORITY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThreadFactoryBuilderPriorityValidationTooLow() {
        ThreadFactoryBuilder.create().withPriority(Thread.MIN_PRIORITY - 1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThreadFactoryBuilderPriorityValidationTooHigh() {
        ThreadFactoryBuilder.create().withPriority(Thread.MAX_PRIORITY + 1).build();
    }

}
