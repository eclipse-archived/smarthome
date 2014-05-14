package org.eclipse.smarthome.model.script.runtime.internal;

import org.eclipse.smarthome.model.core.ModelInjectorProvider;

import com.google.inject.Injector;

public class ScriptRuntimeInjectorProvider implements ModelInjectorProvider {

	public Injector getInjector() {
		return ScriptRuntimeActivator.getInstance().getInjector();
	}

}
