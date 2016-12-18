/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.binding.hue.internal.HueThingHandlerFactory
import org.eclipse.smarthome.core.thing.ThingTypeMigrationService
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.test.OSGiTest

class AbstractHueOSGiTest extends OSGiTest {

    /**
     * Gets a thing handler of a specific type.
     *
     * @param clazz type of thing handler
     *
     * @return the thing handler
     */
    protected <T extends ThingHandler> T getThingHandler(Class<T> clazz){
        HueThingHandlerFactory factory
        waitForAssert({
            factory = getService(ThingHandlerFactory, HueThingHandlerFactory)
            assertThat factory, is(notNullValue())
        }, 10000)
        def handlers = getThingHandlers(factory)

        for(ThingHandler handler : handlers) {
            if(clazz.isInstance(handler)) {
                return handler
            }
        }
        return null
    }

    private Set<ThingHandler> getThingHandlers(ThingHandlerFactory factory) {
        def thingManager = getService(ThingTypeMigrationService.class, { "org.eclipse.smarthome.core.thing.internal.ThingManager" } )
        assertThat thingManager, not(null)
        thingManager.thingHandlersByFactory.get(factory)
    }
}
