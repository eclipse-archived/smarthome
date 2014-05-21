package org.eclipse.smarthome.model.sitemap.runtime.internal;

import org.eclipse.smarthome.model.core.guice.AbstractGuiceAwareExecutableExtensionFactory;

import com.google.inject.Injector;

public class SitemapRuntimeExecutableExtensionFactory extends
		AbstractGuiceAwareExecutableExtensionFactory {


	@Override
	protected Injector getInjector() {
		return SitemapRuntimeInjectorProvider.getInjector();
	}

}
