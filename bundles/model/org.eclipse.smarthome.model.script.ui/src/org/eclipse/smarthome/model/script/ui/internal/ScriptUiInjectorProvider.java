package org.eclipse.smarthome.model.script.ui.internal;

import org.eclipse.smarthome.model.core.ModelInjectorProvider;

import com.google.inject.Injector;

public class ScriptUiInjectorProvider implements ModelInjectorProvider {

	@Override
	public Injector getInjector() {
		return ScriptUIActivator.getInstance().getInjector(ScriptUIActivator.ORG_ECLIPSE_SMARTHOME_MODEL_SCRIPT_SCRIPT);
	}

}
