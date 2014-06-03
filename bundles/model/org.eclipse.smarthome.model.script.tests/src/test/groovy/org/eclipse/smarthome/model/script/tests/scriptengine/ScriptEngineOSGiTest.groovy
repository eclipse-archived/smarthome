package org.eclipse.smarthome.model.script.tests.scriptengine

import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.scriptengine.ScriptEngine
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before;
import org.junit.Test
import org.eclipse.smarthome.core.scriptengine.Script;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

class ScriptEngineOSGiTest extends OSGiTest {

	ItemRegistry itemRegistry
	ItemProvider itemProvider
	def ITEM_NAME = "Switch1"

	@Before
	void setUp() {
		itemRegistry = getService(ItemRegistry)
		itemProvider = [
			getItems: {[new SwitchItem(ITEM_NAME)]},
			addItemChangeListener: {def itemCHangeListener -> },
			removeItemChangeListener: {def itemCHangeListener -> }] as ItemProvider
	}
	
	@Test
	def void testInterpreter() {
		
		assertThat itemRegistry.getItems().size, is(0)
		
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
