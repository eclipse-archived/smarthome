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
package org.eclipse.smarthome.binding.hue.test;

import static org.junit.Assert.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.test.java.JavaOSGiTest;

/**
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class AbstractHueOSGiTest extends JavaOSGiTest {

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
                assertNotNull(tmp);
                assertEquals(clazz, tmp.getClass());
                throw new RuntimeException();
            }
        });
    }

}
