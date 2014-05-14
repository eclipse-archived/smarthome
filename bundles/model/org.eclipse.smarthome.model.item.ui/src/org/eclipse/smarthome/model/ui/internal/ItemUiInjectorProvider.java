package org.eclipse.smarthome.model.ui.internal;

import org.eclipse.smarthome.model.core.ModelInjectorProvider;

import com.google.inject.Injector;

public class ItemUiInjectorProvider implements ModelInjectorProvider {

	@Override
	public Injector getInjector() {
		return ItemsActivator.getInstance().getInjector(ItemsActivator.ORG_ECLIPSE_SMARTHOME_MODEL_ITEMS);
	}

}
