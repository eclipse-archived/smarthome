package org.eclipse.smarthome.model.script.runtime.internal;

import org.eclipse.smarthome.core.scriptengine.Script;
import org.eclipse.smarthome.core.scriptengine.ScriptEngine;
import org.eclipse.smarthome.model.script.runtime.internal.engine.ScriptEngineImpl;
import org.eclipse.smarthome.model.script.runtime.internal.engine.ScriptImpl;
import org.eclipse.xtext.service.AbstractGenericModule;

public class ScriptRuntimeModule extends AbstractGenericModule {

	public Class<? extends ScriptEngine> bindScriptEngine() {
		return ScriptEngineImpl.class;
	}
	
	public Class<? extends Script> bindScript() {
		return ScriptImpl.class;
	}
}
