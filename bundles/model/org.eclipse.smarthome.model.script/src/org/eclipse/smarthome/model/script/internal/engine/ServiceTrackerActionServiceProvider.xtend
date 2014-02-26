package org.eclipse.smarthome.model.script.internal.engine;

import com.google.inject.Singleton
import java.util.List
import org.eclipse.smarthome.core.scriptengine.action.ActionService
import org.eclipse.smarthome.model.script.internal.ScriptActivator

@Singleton
class ServiceTrackerActionServiceProvider implements org.eclipse.smarthome.model.script.engine.IActionServiceProvider {

	private List<ActionService> cache = null;
	
	private int trackingCount = -1;
	
	override get() {
		val currentTrackingCount = ScriptActivator.actionServiceTracker.trackingCount
		
		// if something has changed about the tracked services, recompute the list
		if(trackingCount != currentTrackingCount) {
			cache = ScriptActivator.actionServiceTracker.services.filter(ActionService).toList
		}
		cache
	}

}
