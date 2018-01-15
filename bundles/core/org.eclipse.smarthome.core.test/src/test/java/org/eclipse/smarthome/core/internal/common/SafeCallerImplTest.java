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
package org.eclipse.smarthome.core.internal.common;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.smarthome.core.common.QueueingThreadPoolExecutor;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class SafeCallerImplTest extends JavaTest {

    private static final int THREAD_POOL_SIZE = 3;

    // the duration that the called object should block for
    private static final int BLOCK = 1500;

    // the standard timeout for the safe-caller used in most tests
    private static final int TIMEOUT = 600;

    // the grace period allowed for processing before a timing assertion should fail
    private static final int GRACE = 300;

    @Mock
    private Runnable mockRunnable;

    @Mock
    private Runnable mockTimeoutHandler;

    @Mock
    private Consumer<Throwable> mockErrorHandler;

    @Rule
    public TestName name = new TestName();

    private SafeCallerImpl safeCaller;

    private QueueingThreadPoolExecutor scheduler;

    public static interface ITarget {
        public String method();
    }

    public static class Target implements ITarget {
        @Override
        public String method() {
            return "Hello";
        }
    }

    public static class DerivedTarget extends Target implements ITarget {
    }

    @Before
    public void setup() {
        initMocks(this);
        scheduler = QueueingThreadPoolExecutor.createInstance(name.getMethodName(), THREAD_POOL_SIZE);
        safeCaller = new SafeCallerImpl() {
            @Override
            protected String getPoolName() {
                return name.getMethodName();
            }
        };
        safeCaller.activate(null);

        assertTrue(BLOCK > TIMEOUT + GRACE);
        assertTrue(GRACE < TIMEOUT);
    }

    @After
    public void tearDown() {
        scheduler.shutdownNow();
        safeCaller.deactivate();
    }

    @Test
    public void testSimpleCall() throws Exception {
        Target target = new Target();
        String result = safeCaller.create(target, ITarget.class).build().method();
        assertThat(result, is("Hello"));
    }

    @Test
    public void testInterfaceDetection() throws Exception {
        ITarget target = new Target();
        String result = safeCaller.create(target).build().method();
        assertThat(result, is("Hello"));
    }

    @Test
    public void testExceptionHandler() throws Exception {
        Runnable mock = mock(Runnable.class);
        doThrow(RuntimeException.class).when(mock).run();

        safeCaller.create(mock).onException(mockErrorHandler).build().run();
        waitForAssert(() -> {
            verify(mockErrorHandler).accept(isA(Throwable.class));
        });
    }

    @Test
    public void testTimeoutHandler() throws Exception {
        Runnable mock = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock).run();

        safeCaller.create(mock).withTimeout(TIMEOUT).onTimeout(mockTimeoutHandler).build().run();
        waitForAssert(() -> {
            verify(mockTimeoutHandler).run();
        });
    }

    @Test
    public void testTimeoutReturnsEarly() throws Exception {
        Runnable mock = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock).run();

        assertDurationBetween(TIMEOUT - GRACE, BLOCK - GRACE, () -> {
            safeCaller.create(mock, Runnable.class).withTimeout(TIMEOUT).build().run();
        });
    }

    @Test
    public void testMultiThread_sync() throws Exception {
        Runnable mock = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock).run();

        spawn(() -> {
            assertDurationBetween(TIMEOUT - GRACE, BLOCK - GRACE, () -> {
                safeCaller.create(mock).withTimeout(TIMEOUT).build().run();
            });
        });
        spawn(() -> {
            assertDurationBetween(TIMEOUT - GRACE, BLOCK - GRACE, () -> {
                safeCaller.create(mock).withTimeout(TIMEOUT).build().run();
            });
        });
        waitForAssert(() -> {
            verify(mock, times(2)).run();
        });
    }

    @Test
    public void testSingleThread_sync_secondCallWhileInTimeout() throws Exception {
        Runnable mock = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock).run();
        configureSingleThread();

        assertDurationBetween(TIMEOUT - GRACE, BLOCK - GRACE, () -> {
            safeCaller.create(mock).withTimeout(TIMEOUT).build().run();
        });
        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock).withTimeout(TIMEOUT).build().run();
        });
        assertDurationBetween(TIMEOUT - GRACE, BLOCK + GRACE, () -> {
            waitForAssert(() -> {
                verify(mock, times(2)).run();
            });
        });
    }

    @Test
    public void testSingleThread_sync_parallel() throws Exception {
        Runnable mock = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock).run();
        configureSingleThread();

        spawn(() -> {
            assertDurationBetween(TIMEOUT - GRACE, BLOCK - GRACE, () -> {
                safeCaller.create(mock).withTimeout(TIMEOUT).build().run();
            });
        });
        spawn(() -> {
            assertDurationBelow(GRACE, () -> {
                safeCaller.create(mock).withTimeout(TIMEOUT).build().run();
            });
        });
        assertDurationBetween(TIMEOUT - GRACE, BLOCK - GRACE, () -> {
            waitForAssert(() -> {
                verify(mock, times(2)).run();
            });
        });
    }

    @Test
    public void testMultiThread_async() throws Exception {
        Runnable mock = mock(Runnable.class);
        doAnswer(a -> sleep(TIMEOUT)).when(mock).run();

        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock).withTimeout(TIMEOUT).withAsync().build().run();
        });
        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock).withTimeout(TIMEOUT).withAsync().build().run();
        });
        waitForAssert(() -> {
            verify(mock, times(2)).run();
        });
    }

    @Test
    public void testSingleThread_async() throws Exception {
        Runnable mock = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock).run();
        configureSingleThread();

        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock).withTimeout(TIMEOUT).withAsync().build().run();
        });
        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock).withTimeout(TIMEOUT).withAsync().build().run();
        });
        waitForAssert(() -> {
            verify(mock, times(2)).run();
        });
    }

    @Test
    public void testSecondCallGetsRefused_sameIdentifier() throws Exception {
        Runnable mock1 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock1).run();
        Runnable mock2 = mock(Runnable.class);

        assertDurationBetween(TIMEOUT - GRACE, BLOCK - GRACE, () -> {
            safeCaller.create(mock1).withTimeout(TIMEOUT).withIdentifier("id").build().run();
        });
        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock2).withTimeout(TIMEOUT).withIdentifier("id").build().run();
        });
    }

    @Test
    public void testSecondCallGetsAccepted_differentIdentifier() throws Exception {
        Runnable mock1 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock1).run();
        Runnable mock2 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock2).run();

        assertDurationBetween(TIMEOUT - GRACE, BLOCK - GRACE, () -> {
            safeCaller.create(mock1).withTimeout(TIMEOUT).withIdentifier(new Object()).build().run();
        });
        assertDurationBetween(TIMEOUT - GRACE, BLOCK - GRACE, () -> {
            safeCaller.create(mock2).withTimeout(TIMEOUT).withIdentifier(new Object()).build().run();
        });
    }

    @Test
    public void testTimeoutConfiguration() throws Exception {
        Runnable mock = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock).run();

        assertDurationAbove(BLOCK - GRACE, () -> {
            safeCaller.create(mock).withTimeout(BLOCK + GRACE * 2).onTimeout(mockTimeoutHandler).build().run();
        });
        verifyNoMoreInteractions(mockTimeoutHandler);
    }

    @Test
    public void testCall_wrapped() throws Exception {
        AtomicReference<String> outerThreadName = new AtomicReference<>();
        AtomicReference<String> middleThreadName = new AtomicReference<>();
        AtomicReference<String> innerThreadName = new AtomicReference<>();

        safeCaller.create(new Runnable() {
            @Override
            public void run() {
                outerThreadName.set(Thread.currentThread().getName());
                safeCaller.create((Runnable) () -> {
                }, Runnable.class).build().run();
                safeCaller.create(new Runnable() {
                    @Override
                    public void run() {
                        middleThreadName.set(Thread.currentThread().getName());
                        safeCaller.create((Runnable) () -> {
                        }, Runnable.class).build().run();
                        safeCaller.create(new Runnable() {
                            @Override
                            public void run() {
                                innerThreadName.set(Thread.currentThread().getName());
                                sleep(BLOCK);
                            }

                            @Override
                            public String toString() {
                                return "inner";
                            }
                        }, Runnable.class).build().run();
                    }

                    @Override
                    public String toString() {
                        return "middle";
                    }
                }, Runnable.class).build().run();
            }

            @Override
            public String toString() {
                return "outer";
            }
        }, Runnable.class).withTimeout(TIMEOUT).build().run();
        assertThat(innerThreadName.get(), is(notNullValue()));
        assertThat(middleThreadName.get(), is(notNullValue()));
        assertThat(outerThreadName.get(), is(notNullValue()));
        assertThat(middleThreadName.get(), is(outerThreadName.get()));
        assertThat(innerThreadName.get(), is(outerThreadName.get()));
    }

    @Test
    public void testLambdas() throws Exception {
        ITarget thingHandler = new Target();

        safeCaller.create((Callable<Void>) () -> {
            thingHandler.method();
            return null;
        }).build().call();

        safeCaller.create((Runnable) () -> {
            thingHandler.method();
        }).build().run();

        String res = safeCaller.create((Function<String, String>) name -> {
            return "Hello " + name + "!";
        }).build().apply("World");
        assertThat(res, is("Hello World!"));
    }

    @Test
    public void testAsyncReturnsImmediately() throws Exception {
        Runnable mock1 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock1).run();
        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock1).withTimeout(TIMEOUT).withAsync().build().run();
        });
        waitForAssert(() -> verify(mock1, times(1)).run());
    }

    @Test
    public void testAsyncTimeoutHandler() throws Exception {
        Object identifier = new Object();
        Runnable mock1 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock1).run();

        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock1).withTimeout(TIMEOUT).withAsync().withIdentifier(identifier)
                    .onTimeout(mockTimeoutHandler).onException(mockErrorHandler).build().run();
        });
        waitForAssert(() -> verify(mock1, times(1)).run());
        waitForAssert(() -> verify(mockTimeoutHandler, times(1)).run());
        verifyNoMoreInteractions(mockErrorHandler);
    }

    @Test
    public void testAsyncExceptionHandler() throws Exception {
        Object identifier = new Object();
        Runnable mock1 = mock(Runnable.class);
        doThrow(RuntimeException.class).when(mock1).run();

        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock1).withTimeout(TIMEOUT).withAsync().withIdentifier(identifier)
                    .onTimeout(mockTimeoutHandler).onException(mockErrorHandler).build().run();
        });
        waitForAssert(() -> verify(mock1, times(1)).run());
        waitForAssert(() -> verify(mockErrorHandler, times(1)).accept(isA(Exception.class)));
        verifyNoMoreInteractions(mockErrorHandler);
        verifyNoMoreInteractions(mockTimeoutHandler);
    }

    @Test
    public void testAsyncDoesNotTimeout_differentIdentifiers() throws Exception {
        Runnable mock1 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock1).run();
        Runnable mock2 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock2).run();

        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock1).withTimeout(TIMEOUT).withAsync().withIdentifier(new Object()).build().run();
            safeCaller.create(mock2).withTimeout(TIMEOUT).withAsync().withIdentifier(new Object()).build().run();
        });
        waitForAssert(() -> verify(mock1, times(1)).run());
        waitForAssert(() -> verify(mock2, times(1)).run());
        verifyNoMoreInteractions(mock1, mock2);
    }

    @Test
    public void testAsyncDoesNotTimeout_defaultIdentifiers() throws Exception {
        Runnable mock1 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock1).run();
        Runnable mock2 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock2).run();

        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock1).withTimeout(TIMEOUT).withAsync().build().run();
            safeCaller.create(mock2).withTimeout(TIMEOUT).withAsync().build().run();
        });
        waitForAssert(() -> verify(mock1, times(1)).run());
        waitForAssert(() -> verify(mock2, times(1)).run());
        verifyNoMoreInteractions(mock1, mock2);
    }

    @Test
    public void testAsyncRunsSubsequentAndDoesNotTimeout_sameIdentifier() throws Exception {
        Object identifier = new Object();
        Runnable mock1 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock1).run();
        Runnable mock2 = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock2).run();

        assertDurationBelow(GRACE, () -> {
            safeCaller.create(mock1).withAsync().withIdentifier(identifier).withTimeout(BLOCK + TIMEOUT).build().run();
            safeCaller.create(mock2).withAsync().withIdentifier(identifier).withTimeout(BLOCK + TIMEOUT).build().run();
        });
        waitForAssert(() -> verify(mock1, times(1)).run());
        waitForAssert(() -> verify(mock2, times(1)).run());
        verifyNoMoreInteractions(mock1, mock2);
    }

    @Test
    public void testAsyncSequential_sameIdentifier() throws Exception {
        Runnable mock = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock).run();

        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            assertDurationBelow(GRACE, () -> {
                safeCaller.create(mock).withTimeout(TIMEOUT).withAsync().build().run();
            });
        }

        assertDurationBetween(BLOCK * (THREAD_POOL_SIZE - 1) - GRACE, (BLOCK + GRACE) * (THREAD_POOL_SIZE), () -> {
            waitForAssert(() -> {
                verify(mock, times(THREAD_POOL_SIZE)).run();
            });
        });
    }

    @Test
    public void testAsyncExceedingThreadPool_differentIdentifier() throws Exception {
        Runnable mock = mock(Runnable.class);
        doAnswer(a -> sleep(BLOCK)).when(mock).run();

        for (int i = 0; i < THREAD_POOL_SIZE * 2; i++) {
            assertDurationBelow(GRACE, () -> {
                safeCaller.create(mock).withTimeout(TIMEOUT).withIdentifier(new Object()).withAsync().build().run();
            });
        }
        assertDurationBetween(BLOCK - GRACE, BLOCK + TIMEOUT + GRACE, () -> {
            waitForAssert(() -> {
                verify(mock, times(THREAD_POOL_SIZE * 2)).run();
            });
        });
    }

    @Test
    public void testAsyncExecutionOrder() throws Exception {
        Queue<Integer> q = new ConcurrentLinkedQueue<>();
        final Random r = new Random();

        for (int i = 0; i < THREAD_POOL_SIZE * 10; i++) {
            final int j = i;
            safeCaller.create(() -> {
                q.add(j);
                sleep(r.nextInt(GRACE));
            }, Runnable.class).withTimeout(TIMEOUT).withAsync().withIdentifier(q).build().run();
        }

        waitForAssert(() -> {
            assertThat(q.size(), is(THREAD_POOL_SIZE * 10));
        });

        int expected = 0;
        for (int actual : q) {
            assertThat(actual, is(expected++));
        }
    }

    @Test
    public void testDuplicateInterface() {
        ITarget target = new DerivedTarget();
        safeCaller.create(target).build().method();
    }

    private void assertDurationBelow(long high, Runnable runnable) {
        assertDurationBetween(-1, high, runnable);
    }

    private void assertDurationAbove(long low, Runnable runnable) {
        assertDurationBetween(low, -1, runnable);
    }

    private void assertDurationBetween(long low, long high, Runnable runnable) {
        long startNanos = System.nanoTime();
        try {
            runnable.run();
        } finally {
            long durationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            if (low > -1) {
                assertTrue(MessageFormat.format("Duration should have been above {0} but was {1}", low, durationMillis),
                        durationMillis >= low);
            }
            if (high > -1) {
                assertTrue(
                        MessageFormat.format("Duration should have been below {0} but was {1}", high, durationMillis),
                        durationMillis < high);
            }
        }
    }

    private static Object sleep(int duration) {
        try {
            Thread.sleep(duration);
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void configureSingleThread() {
        safeCaller.modified(new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("singleThread", "true");
            }
        });
    }

    private void spawn(Runnable runnable) throws InterruptedException, ExecutionException {
        try {
            Executors.newSingleThreadExecutor().submit(runnable).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof AssertionError) {
                throw (AssertionError) e.getCause();
            } else {
                throw e;
            }
        }
    }

}
