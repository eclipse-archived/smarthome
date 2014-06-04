/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.sitemap.runtime.internal;

import org.eclipse.smarthome.model.SitemapRuntimeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class SitemapRuntimeInjectorProvider {
	private static Injector injector;
	
	public static Injector getInjector() {
		
		if (injector == null) {
			injector = Guice.createInjector(new SitemapRuntimeModule());
		}
		return injector;
	}

}
