package org.eclipse.smarthome.model.rule.runtime.internal;

import org.eclipse.smarthome.model.rule.RulesRuntimeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class RuleRuntimeInjectorProvider {
	private static Injector injector;
	
	public static Injector getInjector() {
		
		if (injector == null) {
			injector = Guice.createInjector(new RulesRuntimeModule());
		}
		return injector;
	}

}
