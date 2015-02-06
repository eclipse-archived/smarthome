/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.sitemap.runtime.internal;

import org.eclipse.smarthome.model.SitemapStandaloneSetup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitemapRuntimeActivator implements BundleActivator {

    private final Logger logger = LoggerFactory.getLogger(SitemapRuntimeActivator.class);

    @Override
    public void start(BundleContext context) throws Exception {
        SitemapStandaloneSetup.doSetup();
        logger.debug("Registered 'sitemap' configuration parser");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
