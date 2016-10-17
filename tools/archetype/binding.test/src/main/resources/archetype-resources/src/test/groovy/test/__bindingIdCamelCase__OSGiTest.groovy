/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ${package}.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Test

import ${package}.handler.${bindingIdCamelCase}Handler



/**
 * Tests for {@link ${bindingIdCamelCase}Handler}.
 *
 * @author ${author} - Initial contribution
 */
class ${bindingIdCamelCase}OSGiTest extends OSGiTest {

	final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("${bindingId}", "bridge")

	ManagedThingProvider managedThingProvider
	VolatileStorageService volatileStorageService = new VolatileStorageService()


	@Before
	void setUp() {
		registerService(volatileStorageService)
		managedThingProvider = getService(ThingProvider, ManagedThingProvider)
		assertThat managedThingProvider, is(notNullValue())
	}

	@Test
	void "creation of a ${bindingIdCamelCase}Handler"() {
		${bindingIdCamelCase}Handler handler = getService(ThingHandler,${bindingIdCamelCase}Handler)
		assertThat handler, is(nullValue())
	}

}
