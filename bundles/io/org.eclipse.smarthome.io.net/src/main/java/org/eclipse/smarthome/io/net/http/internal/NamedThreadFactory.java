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
package org.eclipse.smarthome.io.net.http.internal;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory class to create custom named threads
 *
 * @author Michael Bock - initial API
 */
public class NamedThreadFactory implements ThreadFactory {

    private String threadNamePrefix;
    private final ThreadGroup group;
    private AtomicInteger threadIndex = new AtomicInteger(1);

    public NamedThreadFactory(String consumerName) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.threadNamePrefix = consumerName + "-";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(group, runnable, threadNamePrefix + threadIndex.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
