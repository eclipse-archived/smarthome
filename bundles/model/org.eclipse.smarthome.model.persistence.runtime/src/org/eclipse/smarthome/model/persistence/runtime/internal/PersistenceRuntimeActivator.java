package org.eclipse.smarthome.model.persistence.runtime.internal;

import org.eclipse.smarthome.model.persistence.PersistenceStandaloneSetup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceRuntimeActivator implements BundleActivator {

	private final static Logger logger = LoggerFactory
			.getLogger(PersistenceRuntimeActivator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		PersistenceStandaloneSetup.doSetup();
		logger.debug("Registered 'persistence' configuration parser");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
