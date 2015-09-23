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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.ui.icon.IconProvider;
import org.eclipse.smarthome.ui.icon.IconSet.Format;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers a servlet that serves icons through {@link IconProvider}s.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class IconServlet extends HttpServlet {

    private static final long serialVersionUID = 2880642275858634578L;

    private final Logger logger = LoggerFactory.getLogger(IconServlet.class);

    private static final String SERVLET_NAME = "/icon";
    private static final String PARAM_ICONSET = "iconset";
    private static final String PARAM_FORMAT = "format";
    private static final String PARAM_STATE = "state";

    private long startupTime;

    protected HttpService httpService;

    protected String defaultIconSetId = "classic";

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

    protected void activate(Map<String, Object> config) {
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

        modified(config);
    }

    protected void modified(Map<String, Object> config) {
        Object iconSetId = config.get("default");
        if (iconSetId instanceof String) {
            defaultIconSetId = (String) iconSetId;
        }
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

        String category = getCategory(req);
        Format format = getFormat(req);
        String state = getState(req);
        String iconSetId = getIconSetId(req);

        IconProvider topProvider = null;
        int maxPrio = Integer.MIN_VALUE;
        for (IconProvider provider : iconProvider) {
            Integer prio = provider.hasIcon(category, iconSetId, format);
            if (prio != null && prio > maxPrio) {
                maxPrio = prio;
                topProvider = provider;
            }
        }
        if (topProvider != null) {
            if (format.equals(Format.SVG)) {
                resp.setContentType("image/svg+xml");
            } else {
                resp.setContentType("image/png");
            }
            resp.setDateHeader("Last-Modified", new Date().getTime());
            ServletOutputStream os = resp.getOutputStream();
            try (InputStream is = topProvider.getIcon(category, iconSetId, state, format)) {
                IOUtils.copy(is, os);
                resp.flushBuffer();
            } catch (IOException e) {
                logger.error("Failed sending the icon byte stream as a response: {}", e.getMessage());
                resp.sendError(500, e.getMessage());
            }
        } else {
            resp.sendError(404);
        }
    }

    private String getCategory(HttpServletRequest req) {
        String category = StringUtils.substringAfterLast(req.getRequestURI(), "/");
        category = StringUtils.substringBeforeLast(category, ".");
        return StringUtils.substringBeforeLast(category, "-");
    }

    private Format getFormat(HttpServletRequest req) {
        String format = req.getParameter(PARAM_FORMAT);
        if (format == null) {
            String filename = StringUtils.substringAfterLast(req.getRequestURI(), "/");
            format = StringUtils.substringAfterLast(filename, ".");
        }
        try {
            Format f = Format.valueOf(format.toUpperCase());
            return f;
        } catch (IllegalArgumentException e) {
            logger.debug("unknown format '{}' in HTTP request - falling back to PNG", format);
            return Format.PNG;
        }
    }

    private String getIconSetId(HttpServletRequest req) {
        String iconSetId = req.getParameter(PARAM_ICONSET);
        if (iconSetId == null || iconSetId.isEmpty()) {
            return defaultIconSetId;
        } else {
            return iconSetId;
        }
    }

    private String getState(HttpServletRequest req) {
        String state = req.getParameter(PARAM_STATE);
        if (state != null) {
            return state;
        } else {
            String filename = StringUtils.substringAfterLast(req.getRequestURI(), "/");
            state = StringUtils.substringAfterLast(filename, "-");
            state = StringUtils.substringBeforeLast(state, ".");
            if (StringUtils.isNotEmpty(state)) {
                return state;
            } else {
                return null;
            }
        }
    }
}
