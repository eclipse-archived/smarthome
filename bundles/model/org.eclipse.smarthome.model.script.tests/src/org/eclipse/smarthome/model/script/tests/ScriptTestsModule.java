/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
