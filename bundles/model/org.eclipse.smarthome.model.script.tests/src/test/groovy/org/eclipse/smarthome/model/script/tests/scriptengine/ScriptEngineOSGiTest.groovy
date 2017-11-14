/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.tests.scriptengine

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.model.script.ScriptServiceUtil
import org.eclipse.smarthome.model.script.engine.Script
import org.eclipse.smarthome.model.script.engine.ScriptEngine
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Test


/**
 * The {@link ScriptEngineOSGiTest} provides tests for the {@link ScriptEngine}.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
class ScriptEngineOSGiTest extends OSGiTest {

    def ITEM_NAME = "Switch1"

    @Test
    def void testInterpreter() {

        registerVolatileStorageService()

        def EventPublisher eventPublisher = [ post: {def Event -> }] as EventPublisher
        assertNotNull(eventPublisher)
        registerService eventPublisher

        def ItemRegistry itemRegistry = getService(ItemRegistry)
        assertNotNull(itemRegistry)
        assertThat itemRegistry.getItems().size(), is(0)

        def ItemProvider itemProvider = [
            getAll: {[new SwitchItem(ITEM_NAME)]},
            addProviderChangeListener: {def itemCHangeListener -> },
            removeProviderChangeListener: {def itemCHangeListener -> }] as ItemProvider
        registerService itemProvider

        ScriptServiceUtil scriptServiceUtil = null

        scriptServiceUtil = getService(ScriptServiceUtil)
        assertNotNull(scriptServiceUtil)

        def ScriptEngine scriptEngine = scriptServiceUtil.scriptEngine
        assertNotNull(scriptEngine)

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
