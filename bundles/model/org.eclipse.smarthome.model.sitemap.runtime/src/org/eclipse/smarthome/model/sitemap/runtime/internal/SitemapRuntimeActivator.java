package org.eclipse.smarthome.model.sitemap.runtime.internal;

import org.eclipse.smarthome.model.SitemapStandaloneSetup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitemapRuntimeActivator implements BundleActivator {

	private final static Logger logger = LoggerFactory
			.getLogger(SitemapRuntimeActivator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		SitemapStandaloneSetup.doSetup();
		logger.debug("Registered 'sitemap' configuration parser");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
