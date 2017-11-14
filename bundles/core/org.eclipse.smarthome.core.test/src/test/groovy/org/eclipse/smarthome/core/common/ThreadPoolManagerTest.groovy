/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import org.junit.Test


/**
 * The ThreadPoolManagerTest tests functionality of the ThreadPoolManager class.
 *
 * @author Kai Kreuzer - Initial contribution
 */
class ThreadPoolManagerTest {

    @Test
    void 'get scheduled pool'() {
        ThreadPoolExecutor result = ThreadPoolManager.getScheduledPool("test1")

        assertThat result, instanceOf(ScheduledExecutorService)

        assertTrue result.allowsCoreThreadTimeOut()
        assertThat result.getKeepAliveTime(TimeUnit.SECONDS), is(ThreadPoolManager.THREAD_TIMEOUT)
        assertThat result.getCorePoolSize(), is(ThreadPoolManager.DEFAULT_THREAD_POOL_SIZE)
    }

    @Test
    void 'get cached pool'() {
        def result = ThreadPoolManager.getPool("test2")

        assertThat result, instanceOf(ExecutorService)

        ThreadPoolExecutor tpe = result

        assertTrue tpe.allowsCoreThreadTimeOut()
        assertThat tpe.getKeepAliveTime(TimeUnit.SECONDS), is(ThreadPoolManager.THREAD_TIMEOUT)
        assertThat tpe.getMaximumPoolSize(), is(ThreadPoolManager.DEFAULT_THREAD_POOL_SIZE)
    }

    @Test
    void 'get configured scheduled pool'() {

        def tpm = new ThreadPoolManager()
        tpm.modified(["test3":"5"])
        ThreadPoolExecutor result = ThreadPoolManager.getScheduledPool("test3")

        assertThat result, instanceOf(ScheduledExecutorService)
        assertThat result.getCorePoolSize(), is(5)
    }

    @Test
    void 'get configured cached pool'() {

        def tpm = new ThreadPoolManager()
        tpm.modified(["test4":"4"])
        ThreadPoolExecutor result = ThreadPoolManager.getPool("test4")

        assertThat result.getMaximumPoolSize(), is(4)
    }

    @Test
    void 'reconfiguring scheduled pool'() {
        ThreadPoolExecutor result = ThreadPoolManager.getScheduledPool("test5")
        assertThat result.getCorePoolSize(), is(ThreadPoolManager.DEFAULT_THREAD_POOL_SIZE)

        def tpm = new ThreadPoolManager()
        tpm.modified(["test5":"11"])

        assertThat result.getCorePoolSize(), is(11)
    }

    @Test
    void 'reconfiguring cached pool'() {
        ThreadPoolExecutor result = ThreadPoolManager.getPool("test6")
        assertThat result.getMaximumPoolSize(), is(ThreadPoolManager.DEFAULT_THREAD_POOL_SIZE)

        def tpm = new ThreadPoolManager()
        tpm.modified(["test6":"7"])

        assertThat result.getMaximumPoolSize(), is(7)

        tpm.modified(["test6":"3"])
        assertThat result.getMaximumPoolSize(), is(3)
    }
}
