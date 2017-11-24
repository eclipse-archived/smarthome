/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.ui.basic.internal.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.io.net.http.HttpContextFactoryService;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

/**
 * This is the base servlet class for other servlet in the Basic UI.
 *
 * @author Thomas.Eichstaedt-Engelen
 */
public abstract class BaseServlet implements Servlet {

    /** the root path of this web application */
    public static final String WEBAPP_ALIAS = "/basicui";

    protected HttpService httpService;
    private HttpContextFactoryService httpContextFactoryService;
    protected ItemRegistry itemRegistry;

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    public void setHttpContextFactoryService(HttpContextFactoryService httpContextFactoryService) {
        this.httpContextFactoryService = httpContextFactoryService;
    }

    public void unsetHttpContextFactoryService(HttpContextFactoryService httpContextFactoryService) {
        this.httpContextFactoryService = null;
    }

    /**
     * Creates a {@link HttpContext}
     *
     * @return a {@link HttpContext}
     */
    protected HttpContext createHttpContext(Bundle bundle) {
        return httpContextFactoryService.createDefaultHttpContext(bundle);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {
    }

}
