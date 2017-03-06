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

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

import org.junit.Test


/**
 * The SafeMethodCallerTest tests functionality of the SafeMethodCaller helper class.
 *
 * @author Dennis Nobel - Initial contribution
 */
class SafeMethodCallerTest {

    class Target {
        public String method() {
            return "Hello"
        }

        public void methodWithTimeout() {
            Thread.sleep(500);
        }

        public void methodWithException() {
            throw new RuntimeException("Error")
        }
    }

    @Test
    void 'call executes method'() {
        def target = new Target();
        def result = SafeMethodCaller.call({ target.method() } as SafeMethodCaller.ActionWithException)

        assertThat result, is(equalTo("Hello"))
    }

    @Test(expected=ExecutionException)
    void 'call throws ExecutionException'() {
        def target = new Target();
        SafeMethodCaller.call({ target.methodWithException() } as SafeMethodCaller.ActionWithException)
    }

    @Test(expected=TimeoutException)
    void 'call throws TimeoutException'() {
        def target = new Target();
        SafeMethodCaller.call({ target.methodWithTimeout() } as SafeMethodCaller.ActionWithException, 100)
    }

    @Test
    void 'call does not throws TimeoutException if timeout is high enough'() {
        def target = new Target();
        SafeMethodCaller.call({ target.methodWithTimeout() } as SafeMethodCaller.ActionWithException, 600)
    }

    @Test
    void 'call just logs ExecutionException'() {
        def target = new Target();
        SafeMethodCaller.call({ target.methodWithException() } as SafeMethodCaller.Action)
    }

    @Test
    void 'call just logs TimeoutException'() {
        def target = new Target();
        SafeMethodCaller.call({ target.methodWithTimeout() } as SafeMethodCaller.Action, 100)
    }

    @Test
    void 'wrapped call executes directly'() {
        String outerThreadName
        String innerThreadName
        SafeMethodCaller.call({
            outerThreadName = Thread.currentThread().getName()
            SafeMethodCaller.call({
                innerThreadName = Thread.currentThread().getName()
            } as SafeMethodCaller.ActionWithException)
        } as SafeMethodCaller.ActionWithException)

        assertThat innerThreadName, is(equalTo(outerThreadName))
    }
}
