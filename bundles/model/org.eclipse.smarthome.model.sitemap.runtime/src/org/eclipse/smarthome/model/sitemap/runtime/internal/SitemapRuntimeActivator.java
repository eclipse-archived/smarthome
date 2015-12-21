/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.sitemap.runtime.internal;

import org.eclipse.smarthome.model.SitemapStandaloneSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitemapRuntimeActivator {

    private final Logger logger = LoggerFactory.getLogger(SitemapRuntimeActivator.class);

    public void activate() throws Exception {
        SitemapStandaloneSetup.doSetup();
        logger.debug("Registered 'sitemap' configuration parser");
    }

    public void deactivate() throws Exception {
    }

}
