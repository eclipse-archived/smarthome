package org.eclipse.smarthome.model.script.runtime.internal;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ScriptRuntimeInjectorProvider {
	private static Injector injector;
	
	public static Injector getInjector() {
		
		if (injector == null) {
			injector = Guice.createInjector(new org.eclipse.smarthome.model.script.ScriptRuntimeModule(), new ScriptRuntimeModule());
		}
		return injector;
	}

}
