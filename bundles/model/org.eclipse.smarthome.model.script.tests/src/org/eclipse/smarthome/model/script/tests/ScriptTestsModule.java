package org.eclipse.smarthome.model.script.tests;

import org.eclipse.smarthome.model.script.engine.IActionServiceProvider;
import org.eclipse.smarthome.model.script.engine.IItemRegistryProvider;
import org.eclipse.smarthome.model.script.tests.mock.ActionServiceProviderMock;
import org.eclipse.smarthome.model.script.tests.mock.ItemRegistryProviderMock;
import org.eclipse.xtext.service.AbstractGenericModule;

public class ScriptTestsModule extends AbstractGenericModule {

	public Class<? extends IItemRegistryProvider> bindIItemRegistryProvider() {
		return ItemRegistryProviderMock.class;
	}
	
	public Class<? extends IActionServiceProvider> bindIIActionServiceProvider() {
		return ActionServiceProviderMock.class;
	}
}
