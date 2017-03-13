/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.test.java.JavaOSGiTest;

import groovy.lang.Closure;

/**
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class AbstractHueOSGiTest extends JavaOSGiTest {

    protected void waitForAssert(Closure<?> assertion) {
        waitForAssert(() -> assertion.run(), null, DFL_TIMEOUT, DFL_SLEEP_TIME);
    }

    protected void waitForAssert(Closure<?> assertion, int timeout) {
        waitForAssert(() -> assertion.run(), null, timeout, DFL_SLEEP_TIME);
    }

    protected void waitForAssert(Closure<?> assertion, int timeout, int sleepTime) {
        super.waitForAssert(() -> assertion.run(), null, timeout, sleepTime);
    }

    protected void waitForAssert(Closure<?> assertion, Runnable beforeLastCall, int timeout, int sleepTime) {
        super.waitForAssert(() -> assertion.run(), beforeLastCall, timeout, sleepTime);
    }

    /**
     * Gets the handler of a thing if it fits to a specific type.
     *
     * @param thing the thing
     * @param clazz type of thing handler
     * @return the thing handler
     */
    protected <T extends ThingHandler> T getThingHandler(Thing thing, Class<T> clazz) {
        return waitForAssert(() -> {
            final ThingHandler tmp = thing.getHandler();
            if (clazz.isInstance(tmp)) {
                return clazz.cast(tmp);
            } else {
                throw new IllegalArgumentException("Handler of wrong type");
            }
        });
    }

}
