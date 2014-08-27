package org.eclipse.smarthome.model.item.runtime.internal;

import org.eclipse.smarthome.model.ItemsStandaloneSetup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemRuntimeActivator implements BundleActivator {

	private final static Logger logger = LoggerFactory
			.getLogger(ItemRuntimeActivator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		ItemsStandaloneSetup.doSetup();
		logger.debug("Registered 'item' configuration parser");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
