package org.eclipse.smarthome.model.persistence.runtime.internal;

import org.eclipse.smarthome.model.persistence.PersistenceRuntimeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class PersistenceRuntimeInjectorProvider {
	private static Injector injector;
	
	public static Injector getInjector() {
		
		if (injector == null) {
			injector = Guice.createInjector(new PersistenceRuntimeModule());
		}
		return injector;
	}

}
