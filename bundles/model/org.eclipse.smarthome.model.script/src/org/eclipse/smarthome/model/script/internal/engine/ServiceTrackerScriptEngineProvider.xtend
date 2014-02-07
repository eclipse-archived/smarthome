package org.eclipse.smarthome.model.script.internal.engine

import org.eclipse.smarthome.model.script.engine.IScriptEngineProvider
import org.eclipse.smarthome.model.script.internal.ScriptActivator
import com.google.inject.Inject
import com.google.inject.Injector

class ServiceTrackerScriptEngineProvider implements IScriptEngineProvider {
	
	@Inject
	Injector injector
	
	override get() {
		val scriptEngine = ScriptActivator.scriptEngineTracker.service
		injector.injectMembers(scriptEngine)
		scriptEngine
	}
	
}