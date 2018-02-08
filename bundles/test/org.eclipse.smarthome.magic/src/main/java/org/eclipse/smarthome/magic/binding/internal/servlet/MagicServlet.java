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
package org.eclipse.smarthome.magic.binding.internal.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
public class MagicServlet implements Servlet {

    protected HttpService httpService;

    /** the root path of this web application */
    public static final String WEBAPP_ALIAS = "/";

    private final Logger logger = LoggerFactory.getLogger(MagicServlet.class);

    @Activate
    protected void activate(Map<String, Object> configProps, BundleContext bundleContext) {
        try {
            httpService.registerResources(WEBAPP_ALIAS, "web", new MagicHttpContext(bundleContext.getBundle()));
            logger.info("Started Magic UI at " + WEBAPP_ALIAS);
        } catch (NamespaceException e) {
            logger.error("Error during magic servlet startup", e);
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
         * Just return the main page with links
         */
        public URL getResource(String name) {
            return bundle.getResource("web/index.html");
        }

        @Override
        public String getMimeType(String name) {
            return null;
        }
    }

    // These servlet methods are intentionally left blank because we only want to show the index site with links
    @Override
    public void init(ServletConfig config) throws ServletException {
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {
    }
}
