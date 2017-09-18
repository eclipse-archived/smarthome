/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.scheduler2;

import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Peter Kriens - initial contribution and API
 * @author Simon Kaufmann - adapted to Java 8
 *
 */
public class SchedulerTest {
    InternalSchedulerImpl si = new InternalSchedulerImpl();

    @Before
    public void setUp() {
        si.activate();
    }

    @After
    public void tearDown() {
        si.deactivate();
    }

    @Test
    public void testCronReboot() throws Exception {
        long now = System.currentTimeMillis();
        Semaphore s = new Semaphore(0);
        si.schedule((foo) -> {
            s.release();
        }, null, "@reboot");
        s.acquire(1);

        long diff = (System.currentTimeMillis() - now);
        assertTrue(diff < 100);
    }

    @Test
    public void testCron2() throws Exception {
        long now = System.currentTimeMillis();
        AtomicReference<Object> ref = new AtomicReference<>();

        Semaphore s = new Semaphore(0);
        si.schedule((foo) -> {
            s.release();
            ref.set(foo.get("foo"));
        }, Collections.singletonMap("foo", "bar"), "#\n" //
                + "\n" //
                + " foo = bar \n" //
                + "# bla bla foo=foo\n" //
                + "0/2 * * * * *");
        s.acquire(2);

        long diff = (System.currentTimeMillis() - now + 500) / 1000;
        assertTrue(diff >= 3 && diff <= 4);
        assertEquals("bar", ref.get());
    }

    @Test
    public void testCancellableWithTimeout() throws InterruptedException, InvocationTargetException {
        CompletableFuture<Integer> d = new CompletableFuture<>();
        CompletableFuture<Integer> before = si.before(d, Duration.ofMillis(100));
        before.cancel(true);
        // assertEquals(CancelException.SINGLETON, before.getFailure());
        assertTrue(before.isCompletedExceptionally());
    }

    @Test
    public void testResolveWithTimeout() throws Exception {
        CompletableFuture<Integer> d = new CompletableFuture<>();
        CompletableFuture<Integer> before = si.before(d, Duration.ofMillis(100));
        d.complete(3);
        assertTrue(before.isDone());
        assertEquals(Integer.valueOf(3), before.get());
    }

    @Test
    public void testFailureWithTimeout() throws InterruptedException, InvocationTargetException {
        CompletableFuture<Integer> d = new CompletableFuture<>();
        CompletableFuture<Integer> before = si.before(d, Duration.ofMillis(100));
        Exception e = new Exception();
        d.completeExceptionally(e);
        assertTrue(before.isDone());
        assertTrue(before.isCompletedExceptionally());
    }

    @Test
    public void testTimeout() throws InterruptedException, InvocationTargetException {
        CompletableFuture<Integer> d = new CompletableFuture<>();
        CompletableFuture<Integer> before = si.before(d, Duration.ofMillis(100));
        Thread.sleep(200);
        assertTrue(before.isDone());
        assertTrue(before.isCompletedExceptionally());
    }

    @Test
    public void testNegative() throws InterruptedException {
        Semaphore s = new Semaphore(0);
        si.after(() -> {
            s.release(1);
            return null;
        }, Duration.ofMillis(-100));
        Thread.sleep(2);
        assertEquals(1, s.availablePermits());
    }

    @Test
    public void testCron() throws Exception {
        long now = System.currentTimeMillis();

        Semaphore s = new Semaphore(0);
        si.schedule(() -> s.release(), "0/2 * * * * *");
        s.acquire(3);

        long diff = (System.currentTimeMillis() - now + 500) / 1000;
        assertTrue(diff >= 5 && diff <= 6);
    }

    @Test
    public void testSchedule() throws InterruptedException, IOException {
        long now = System.currentTimeMillis();

        Semaphore s = new Semaphore(0);
        Closeable c = si.schedule(() -> s.release(), Duration.ofMillis(100), Duration.ofMillis(200),
                Duration.ofMillis(300), Duration.ofMillis(400));

        s.acquire(3);
        long diff = System.currentTimeMillis() - now;
        assertEquals(6, (diff + 50) / 100);

        int n = s.availablePermits();
        Thread.sleep(3000);
        assertEquals(n + 7, s.availablePermits());
        c.close();
        n = s.availablePermits();
        Thread.sleep(3000);
        assertEquals(n, s.availablePermits());
    }

    @Test
    public void testSimple() throws InterruptedException {
        long now = System.currentTimeMillis();

        Semaphore s = new Semaphore(0);

        si.after(Duration.ofMillis(10)).thenAccept((p) -> {
            s.release(1);
        });

        s.acquire();

        assertTrue(System.currentTimeMillis() - now > 9);

        si.deactivate();
    }
}
