/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.tests.scriptengine

import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.model.script.engine.Script;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before;
import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*


/**
 * The {@link ScriptEngineOSGiTest} provides tests for the {@link ScriptEngine}.
 * @author Oliver Libutzki - Initial contribution
 *
 */
class ScriptEngineOSGiTest extends OSGiTest {

	ItemRegistry itemRegistry
	ItemProvider itemProvider
	def ITEM_NAME = "Switch1"

	@Before
	void setUp() {
		itemRegistry = getService(ItemRegistry)
		itemProvider = [
			getAll: {[new SwitchItem(ITEM_NAME)]},
			addProviderChangeListener: {def itemCHangeListener -> },
			removeProviderChangeListener: {def itemCHangeListener -> }] as ItemProvider
	}
	
	@Test
	def void testInterpreter() {
		
		assertThat itemRegistry.getItems().size(), is(0)
		
		registerService itemProvider
		
		ScriptEngine scriptEngine = getService(ScriptEngine)

		String parsedScript = """
			Switch1.state = ON
			Switch1.state = OFF
			Switch1.state = ON

			Switch1.state
		"""
		Script script = scriptEngine.newScriptFromString(parsedScript)
		def switch1State = script.execute()
		
		assertNotNull(switch1State)
		assertEquals("org.eclipse.smarthome.core.library.types.OnOffType", switch1State.class.name)
		assertEquals("ON", switch1State.toString())
		
		unregisterService itemProvider
	}
	
}
