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
