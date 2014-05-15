package org.eclipse.smarthome.model.script.tests.scriptengine

import com.google.inject.Inject
import org.eclipse.smarthome.core.scriptengine.ScriptEngine
import org.eclipse.smarthome.model.script.tests.ScriptTestsInjectorProvider
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.junit.Test
import org.junit.runner.RunWith
import static org.junit.Assert.*
import org.junit.Ignore

@RunWith(XtextRunner)
@InjectWith(ScriptTestsInjectorProvider)
@Ignore
class ScriptEngineTest {
	

	@Inject
	ScriptEngine scriptEngine
	
	@Test
	def void testInterpreter() {
		val parsedScript = '''
			Switch1.state = ON
			Switch1.state = OFF
			Switch1.state = ON

			Switch1.state
		'''.toString
		val script = scriptEngine.newScriptFromString(parsedScript)
		val switch1State = script.execute()
		assertNotNull(switch1State)
		assertEquals("org.eclipse.smarthome.core.library.types.OnOffType", switch1State.class.name)
		assertEquals("ON", switch1State.toString)
	}
	
}