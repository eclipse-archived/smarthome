package org.eclipse.smarthome.model.script.internal.engine;

import com.google.inject.Singleton
import java.util.List
import org.eclipse.smarthome.model.script.engine.action.ActionService
import org.eclipse.smarthome.model.script.ScriptServiceUtil

@Singleton
class ServiceTrackerActionServiceProvider implements org.eclipse.smarthome.model.script.engine.IActionServiceProvider {

	override get() {
		ScriptServiceUtil.getActionServices
	}

}
