/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module;

import java.util.StringTokenizer;

import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;

/**
 * {@link BaseModuleHandlerFactory} provides a base implementation for the
 * {@link ModuleHandlerFactory} interface.
 * 
 * @author Benedikt Niehues
 *
 */
public abstract class BaseModuleHandlerFactory implements ModuleHandlerFactory {

	protected static ModuleTypeRegistry moduleTypeRegistry;

	/**
	 * gets the ModuleTypeRegistry
	 * 
	 * @return
	 */
	public static ModuleTypeRegistry getModuleTypeRegistry() {
		return moduleTypeRegistry;
	}


	protected String getHandlerUID(String moduleTypeUID) {
		StringTokenizer tokenizer = new StringTokenizer(moduleTypeUID, ":");
		return tokenizer.nextToken();
	}

	/**
	 * the moduleTypeRegistry is added
	 * 
	 * @param moduleTypeRegistry
	 */
	public void setModuleTypeRegistry(ModuleTypeRegistry moduleTypeReg) {
		moduleTypeRegistry = moduleTypeReg;
	}

	/**
	 * the moduleTypeRegistry is removed
	 * 
	 * @param moduleTypeRegistry
	 */
	public void unsetModuleTypeRegistry(ModuleTypeRegistry moduleTypeReg) {
		moduleTypeRegistry = null;
	}

}
