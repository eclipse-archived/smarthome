package org.eclipse.smarthome.model.script.runtime.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptRuntimeActivator implements BundleActivator {

	private final Logger logger = LoggerFactory
			.getLogger(ScriptRuntimeActivator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		ScriptRuntimeStandaloneSetup.doSetup();
		logger.debug("Registered 'script' configuration parser");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
