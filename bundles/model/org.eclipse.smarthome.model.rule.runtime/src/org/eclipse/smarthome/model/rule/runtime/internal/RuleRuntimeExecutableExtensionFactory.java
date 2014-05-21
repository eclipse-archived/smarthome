package org.eclipse.smarthome.model.rule.runtime.internal;

import org.eclipse.smarthome.model.core.guice.AbstractGuiceAwareExecutableExtensionFactory;

import com.google.inject.Injector;

public class RuleRuntimeExecutableExtensionFactory extends
		AbstractGuiceAwareExecutableExtensionFactory {


	@Override
	protected Injector getInjector() {
		return RuleRuntimeInjectorProvider.getInjector();
	}

}
