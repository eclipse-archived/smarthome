package org.eclipse.smarthome.model.persistence.runtime.internal;

import org.eclipse.smarthome.model.core.guice.AbstractGuiceAwareExecutableExtensionFactory;

import com.google.inject.Injector;

public class PersistenceRuntimeExecutableExtensionFactory extends
		AbstractGuiceAwareExecutableExtensionFactory {


	@Override
	protected Injector getInjector() {
		return PersistenceRuntimeInjectorProvider.getInjector();
	}

}
