package org.eclipse.smarthome.model.item.runtime.internal;

import org.eclipse.smarthome.model.core.ModelInjectorProvider;

import com.google.inject.Injector;

public class ItemRuntimeInjectorProvider implements ModelInjectorProvider {

	public Injector getInjector() {
		return ItemRuntimeActivator.getInstance().getInjector();
	}

}
