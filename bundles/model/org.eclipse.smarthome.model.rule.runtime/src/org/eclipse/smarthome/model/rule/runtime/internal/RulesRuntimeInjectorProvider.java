package org.eclipse.smarthome.model.rule.runtime.internal;

import org.eclipse.smarthome.model.core.ModelInjectorProvider;

import com.google.inject.Injector;

public class RulesRuntimeInjectorProvider implements ModelInjectorProvider {

	public Injector getInjector() {
		return RulesRuntimeActivator.getInstance().getInjector();
	}

}
