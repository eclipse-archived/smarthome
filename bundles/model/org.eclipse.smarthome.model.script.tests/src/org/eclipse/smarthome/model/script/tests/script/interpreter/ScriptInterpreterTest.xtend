package org.eclipse.smarthome.model.script.tests.script.interpreter

import com.google.inject.Inject
import org.eclipse.smarthome.core.scriptengine.ScriptEngine
import org.eclipse.smarthome.model.script.script.Script
import org.eclipse.smarthome.model.script.tests.ScriptTestsInjectorProvider
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(ScriptTestsInjectorProvider)
class ScriptInterpreterTest {
	

	@Inject
	ScriptEngine scriptEngine
	
//	@Inject
//	org.eclipse.smarthome.core.scriptengine.Script script
//	
	@Inject 
	extension ParseHelper<Script>
	
	
	@Test
	def void testInterpreter() {
		val parsedScript = '''
			// println("Test")
			// println("Das klappt!")
			println(Switch1)
			if (2 > 1 )
				println("Jupp")
			else
				println("ups")
		'''.toString
		//var evaluationContext = (parsedScript.eResource as XtextResource).resourceServiceProvider.get(IEvaluationContext)
		val script = scriptEngine.newScriptFromString(parsedScript)
		script.execute()

		//scriptInterpreter.evaluate(script, evaluationContext, null)
	}
	
}