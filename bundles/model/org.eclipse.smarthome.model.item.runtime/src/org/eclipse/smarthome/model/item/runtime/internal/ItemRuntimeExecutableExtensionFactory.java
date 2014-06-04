/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.item.runtime.internal;

import org.eclipse.smarthome.model.core.guice.AbstractGuiceAwareExecutableExtensionFactory;

import com.google.inject.Injector;

/**
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class ItemRuntimeExecutableExtensionFactory extends
		AbstractGuiceAwareExecutableExtensionFactory {


	@Override
	protected Injector getInjector() {
		return ItemRuntimeInjectorProvider.getInjector();
	}

}
