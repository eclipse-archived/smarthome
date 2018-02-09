/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.magic.binding.internal.http;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers itself under "/" and provides links to other web UIs
 *
 * @author Stefan Triller - initial contribution
 *
 */
@Component(immediate = true)
public class MagicHttpRessource {
    /** the root path of this web application */
    private static final String WEBAPP_ALIAS = "/";

    protected HttpService httpService;

    private final Logger logger = LoggerFactory.getLogger(MagicHttpRessource.class);

    @Activate
    protected void activate(Map<String, Object> configProps, BundleContext bundleContext) {
        try {
            httpService.registerResources(WEBAPP_ALIAS, WEBAPP_ALIAS, new MagicHttpContext(bundleContext.getBundle()));
            logger.info("Started Magic UI at " + WEBAPP_ALIAS);
        } catch (NamespaceException e) {
            logger.debug("Error during magic servlet startup", e);
        }
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(WEBAPP_ALIAS);
        logger.info("Stopped Magic UI");
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    private class MagicHttpContext implements HttpContext {
        private final Bundle bundle;

        private MagicHttpContext(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
            return true;
        }

        @Override
        /**
         * Return the main page with links if empty
         */
        public URL getResource(String pName) {
            String name = "";
            if ("".equals(pName)) {
                name = "web/index.html";
            } else {
                name = "web/" + pName;
            }
            return bundle.getResource(name);
        }

        @Override
        public String getMimeType(String name) {
            return null;
        }
    }
}
