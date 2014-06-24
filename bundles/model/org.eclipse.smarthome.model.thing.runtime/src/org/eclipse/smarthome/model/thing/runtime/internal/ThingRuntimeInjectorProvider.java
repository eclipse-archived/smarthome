package org.eclipse.smarthome.model.thing.runtime.internal;

import org.eclipse.smarthome.model.thing.ThingRuntimeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class ThingRuntimeInjectorProvider {
	private static Injector injector;
	
	public static Injector getInjector() {
		
		if (injector == null) {
			injector = Guice.createInjector(new ThingRuntimeModule());
		}
		return injector;
	}
}
