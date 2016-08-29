/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.internal.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.sitemap.Image;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.Video;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The proxy servlet is used by image and video widgets. As its name suggests, it proxies the content, so
 * that it is possible to include resources (images/videos) from the LAN in the openHAB UI. This is
 * especially useful for webcams as you would not want to make them directly available to the internet.
 *
 * The servlet registers as "/proxy" and expects the two parameters "sitemap" and "widgetId". It will
 * hence provide the data of the url specified in the according widget. Note that it does NOT allow
 * general access to any servers in the LAN - only urls that are specified in a sitemap are accessible.
 *
 * It is also possible to use credentials in a url, e.g. "http://user:pwd@localserver/image.jpg" -
 * the proxy servlet will be able to access the content and provide it to the openHAB UIs through the
 * standard openHAB authentication mechanism (if enabled).
 *
 * This servlet also supports data streams, such as a webcam video stream etc.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Svilen Valkanov - Replaced Apache HttpClient with Jetty
 */
public class ProxyServlet extends HttpServlet {

    /** the alias for this servlet */
    public static final String PROXY_ALIAS = "proxy";

    private final Logger logger = LoggerFactory.getLogger(ProxyServlet.class);

    private static final long serialVersionUID = -4716754591953017793L;

    private static HttpClient httpClient = new HttpClient(new SslContextFactory());

    /** Timeout for HTTP requests in ms */
    private static final int TIMEOUT = 5000;

    protected HttpService httpService;
    protected ItemUIRegistry itemUIRegistry;
    protected ModelRepository modelRepository;

    protected void setItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = itemUIRegistry;
    }

    protected void unsetItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = null;
    }

    protected void setModelRepository(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    protected void unsetModelRepository(ModelRepository modelRepository) {
        this.modelRepository = null;
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    protected void activate() {
        try {
            logger.debug("Starting up proxy servlet at /{}", PROXY_ALIAS);

            startHttpClient(httpClient);
            Hashtable<String, String> props = new Hashtable<String, String>();
            httpService.registerServlet("/" + PROXY_ALIAS, this, props, createHttpContext());
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup: {}", e.getMessage());
        } catch (ServletException e) {
            logger.error("Error during servlet startup: {}", e.getMessage());
        }
    }

    protected void deactivate() {
        httpService.unregister("/" + PROXY_ALIAS);
        stopHttpClient(httpClient);
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
    public String getServletInfo() {
        return "Image and Video Widget Proxy";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sitemapName = request.getParameter("sitemap");
        String widgetId = request.getParameter("widgetId");

        if (sitemapName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'sitemap' must be provided!");
        }
        if (widgetId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'widget' must be provided!");
        }

        String uriString = null;

        Sitemap sitemap = (Sitemap) modelRepository.getModel(sitemapName);
        if (sitemap != null) {
            Widget widget = itemUIRegistry.getWidget(sitemap, widgetId);
            if (widget instanceof Image) {
                Image image = (Image) widget;
                uriString = image.getUrl();
            } else if (widget instanceof Video) {
                Video video = (Video) widget;
                uriString = video.getUrl();
            } else {
                if (widget == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Widget '" + widgetId + "' could not be found!");
                    return;
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Widget type '" + widget.getClass().getName() + "' is not supported!");
                    return;
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sitemap '" + sitemapName + "' could not be found!");
            return;
        }

        Request httpRequest;
        try {
            // check if the uri uses credentials and configure the http client accordingly
            URI uri = URI.create(uriString);

            httpRequest = httpClient.newRequest(uri);

            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":");

                String user = userInfo[0];
                String password = userInfo[1];

                String basicAuthentication = "Basic " + B64Code.encode(user + ":" + password, StringUtil.__ISO_8859_1);
                httpRequest.header(HttpHeader.AUTHORIZATION, basicAuthentication);
            }
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "URI '" + uriString + "' is not valid: " + e.getMessage());
            return;
        }

        InputStreamResponseListener listener = new InputStreamResponseListener();

        // do the client request
        try {
            httpRequest.send(listener);
            // wait for the response headers to arrive or the timeout to expire
            Response httpResponse = listener.get(TIMEOUT, TimeUnit.MILLISECONDS);

            // get response headers
            HttpFields headers = httpResponse.getHeaders();
            Iterator<HttpField> iterator = headers.iterator();

            // copy all headers
            while (iterator.hasNext()) {
                HttpField header = iterator.next();
                response.setHeader(header.getName(), header.getValue());
            }

        } catch (Exception e) {
            if (e instanceof TimeoutException) {
                logger.warn("Proxy servlet failed to stream content due to a timeout");
                response.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            } else {
                logger.warn("Proxy servlet failed to stream content: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
            return;
        }
        // now copy/stream the body content
        try (InputStream responseContent = listener.getInputStream()) {
            IOUtils.copy(responseContent, response.getOutputStream());
        }
    }

    private void startHttpClient(HttpClient client) {
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                logger.warn("Cannot start HttpClient!", e);
            }
        }
    }

    private void stopHttpClient(HttpClient client) {
        if (client.isStarted()) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.error("Unable to stop HttpClient!", e);
            }
        }
    }
}
