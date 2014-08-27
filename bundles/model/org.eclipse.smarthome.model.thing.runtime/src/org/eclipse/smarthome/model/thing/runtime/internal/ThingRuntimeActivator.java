package org.eclipse.smarthome.model.thing.runtime.internal;

import org.eclipse.smarthome.model.thing.ThingStandaloneSetup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThingRuntimeActivator implements BundleActivator {

	private final static Logger logger = LoggerFactory
			.getLogger(ThingRuntimeActivator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		ThingStandaloneSetup.doSetup();
		logger.debug("Registered 'thing' configuration parser");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
