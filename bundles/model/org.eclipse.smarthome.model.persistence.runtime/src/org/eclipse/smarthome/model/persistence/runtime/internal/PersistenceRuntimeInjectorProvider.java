package org.eclipse.smarthome.model.persistence.runtime.internal;

import org.eclipse.smarthome.model.core.ModelInjectorProvider;

import com.google.inject.Injector;

public class PersistenceRuntimeInjectorProvider implements ModelInjectorProvider {

	public Injector getInjector() {
		return PersistenceRuntimeActivator.getInstance().getInjector();
	}

}
