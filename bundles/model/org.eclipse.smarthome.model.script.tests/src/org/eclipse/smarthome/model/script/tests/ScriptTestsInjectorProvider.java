package org.eclipse.smarthome.model.script.tests;

import org.eclipse.smarthome.model.script.ScriptInjectorProvider;

import com.google.inject.Injector;

public class ScriptTestsInjectorProvider extends ScriptInjectorProvider {

	@Override
	protected Injector internalCreateInjector() {
		return new ScriptTestsStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}
