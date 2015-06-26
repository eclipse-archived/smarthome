/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.icon.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.ui.icon.IconProvider;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers a servlet that serves icons through {@link IconProvider}s.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class IconServlet extends HttpServlet {

    private static final long serialVersionUID = 2880642275858634578L;

    private final Logger logger = LoggerFactory.getLogger(IconServlet.class);

    private static final String SERVLET_NAME = "/icon";

    private long startupTime;

    protected HttpService httpService;

    private List<IconProvider> iconProvider = new ArrayList<>();

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    public void addIconProvider(IconProvider iconProvider) {
        this.iconProvider.add(iconProvider);
    }

    public void removeIconProvider(IconProvider iconProvider) {
        this.iconProvider.remove(iconProvider);
    }

    protected void activate() {
        try {
            logger.debug("Starting up icon servlet at " + SERVLET_NAME);

            Hashtable<String, String> props = new Hashtable<String, String>();
            httpService.registerServlet(SERVLET_NAME, this, props, createHttpContext());
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        } catch (ServletException e) {
            logger.error("Error during servlet startup", e);
        }
        startupTime = System.currentTimeMillis();
    }

    /**
     * Creates a {@link HttpContext}
     * 
     * @return a {@link HttpContext}
     */
    protected HttpContext createHttpContext() {
        HttpContext defaultHttpContext = httpService.createDefaultHttpContext();
        return defaultHttpContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getDateHeader("If-Modified-Since") > startupTime) {
            resp.setStatus(304);
            return;
        }

        String filename = StringUtils.substringAfterLast(req.getRequestURI(), "/");

        String iconName = StringUtils.substringBeforeLast(filename, ".");

        for (IconProvider provider : iconProvider) {
            if (provider.hasIcon(iconName)) {
                resp.setContentType("image/png");
                resp.setDateHeader("Last-Modified", new Date().getTime());
                ServletOutputStream os = resp.getOutputStream();
                InputStream is = provider.getIcon(iconName);
                IOUtils.copy(is, os);
                resp.flushBuffer();
                return;
            }
        }
        resp.sendError(404);
    }
}
