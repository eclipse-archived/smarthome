package org.eclipse.smarthome.model.item.runtime.internal;

import org.eclipse.smarthome.model.ItemsRuntimeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ItemRuntimeInjectorProvider {
	private static Injector injector;
	
	public static Injector getInjector() {
		
		if (injector == null) {
			injector = Guice.createInjector(new ItemsRuntimeModule());
		}
		return injector;
	}

}
