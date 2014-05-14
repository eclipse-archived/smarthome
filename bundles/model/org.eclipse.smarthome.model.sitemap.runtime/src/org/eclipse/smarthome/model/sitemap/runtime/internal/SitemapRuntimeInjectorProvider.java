package org.eclipse.smarthome.model.sitemap.runtime.internal;

import org.eclipse.smarthome.model.core.ModelInjectorProvider;

import com.google.inject.Injector;

public class SitemapRuntimeInjectorProvider implements ModelInjectorProvider {

	public Injector getInjector() {
		return SitemapRuntimeActivator.getInstance().getInjector();
	}

}
