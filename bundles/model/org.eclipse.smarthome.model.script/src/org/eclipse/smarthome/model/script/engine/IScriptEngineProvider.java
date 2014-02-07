package org.eclipse.smarthome.model.script.engine;

import org.eclipse.smarthome.core.scriptengine.ScriptEngine;
import org.eclipse.smarthome.model.script.internal.engine.ServiceTrackerScriptEngineProvider;

import com.google.inject.ImplementedBy;
import com.google.inject.Provider;

@ImplementedBy(ServiceTrackerScriptEngineProvider.class)
public interface IScriptEngineProvider extends Provider<ScriptEngine>{

}
