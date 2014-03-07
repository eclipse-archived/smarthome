package org.eclipse.smarthome.model.script.engine;

import java.util.List;

import org.eclipse.smarthome.core.scriptengine.action.ActionService;
import org.eclipse.smarthome.model.script.internal.engine.ServiceTrackerActionServiceProvider;

import com.google.inject.ImplementedBy;
import com.google.inject.Provider;

@ImplementedBy(ServiceTrackerActionServiceProvider.class)
public interface IActionServiceProvider extends Provider<List<ActionService>> {

}
