package org.eclipse.smarthome.model.thing.runtime.internal;

import org.eclipse.smarthome.model.core.guice.AbstractGuiceAwareExecutableExtensionFactory;

import com.google.inject.Injector;

public class ThingRuntimeExecutableExtensionFactory extends
		AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Injector getInjector() {
		return ThingRuntimeInjectorProvider.getInjector();
	}
}
