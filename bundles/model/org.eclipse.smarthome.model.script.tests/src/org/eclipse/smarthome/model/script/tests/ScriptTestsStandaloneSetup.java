package org.eclipse.smarthome.model.script.tests;

import org.eclipse.smarthome.model.script.ScriptRuntimeModule;
import org.eclipse.smarthome.model.script.ScriptStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ScriptTestsStandaloneSetup extends ScriptStandaloneSetup {

	@Override
	public Injector createInjector() {
		return Guice.createInjector(new ScriptRuntimeModule(), new ScriptTestsModule());
	}
}
