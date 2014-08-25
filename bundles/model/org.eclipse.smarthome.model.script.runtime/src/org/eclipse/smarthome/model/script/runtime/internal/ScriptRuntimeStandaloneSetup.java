package org.eclipse.smarthome.model.script.runtime.internal;

import org.eclipse.smarthome.model.script.ScriptStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ScriptRuntimeStandaloneSetup extends ScriptStandaloneSetup {
	
	private static Injector injector;
	
	public static void doSetup() {
		if(injector==null) {
			new ScriptRuntimeStandaloneSetup().createInjectorAndDoEMFRegistration();
		}
	}
	
	static public Injector getInjector() {
		return injector;
	}
	
	@Override
	public Injector createInjector() {
		return Guice.createInjector(new org.eclipse.smarthome.model.script.ScriptRuntimeModule(), new ScriptRuntimeModule());
	}
}
