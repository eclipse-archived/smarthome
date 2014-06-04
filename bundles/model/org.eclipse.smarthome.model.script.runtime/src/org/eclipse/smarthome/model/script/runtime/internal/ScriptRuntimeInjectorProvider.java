/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.runtime.internal;

import com.google.inject.Guice;
import com.google.inject.Injector;


/**
 * The {@link ScriptRuntimeInjectorProvider} provides a Guice injector for the Eclipse SmartHome runtime environment.
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class ScriptRuntimeInjectorProvider {
	private static Injector injector;
	
	public static Injector getInjector() {
		
		if (injector == null) {
			injector = Guice.createInjector(new org.eclipse.smarthome.model.script.ScriptRuntimeModule(), new ScriptRuntimeModule());
		}
		return injector;
	}

}
