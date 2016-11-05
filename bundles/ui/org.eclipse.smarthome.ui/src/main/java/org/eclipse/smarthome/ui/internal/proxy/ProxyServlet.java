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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
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
 * However, if the Image or Video widget is associated with an item whose current State is a StringType,
 * it will attempt to use the state of the item as the url to proxy, or fall back to the url= attribute
 * if the state is not a valid url, so you must make sure that the item's state cannot be set to an
 * internal image or video url that you do not wish to proxy out of your network. If you are concerned
 * with the security aspect of using item= to proxy image or video URLs, then do not use item= with those
 * widgets in your sitemaps.
 *
 * It is also possible to use credentials in a url, e.g. "http://user:pwd@localserver/image.jpg" -
 * the proxy servlet will be able to access the content and provide it to the openHAB UIs through the
 * standard openHAB authentication mechanism (if enabled).
 *
 * This servlet also supports data streams, such as a webcam video stream etc.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Svilen Valkanov - Replaced Apache HttpClient with Jetty
 * @author John Cocula - added optional Image/Video item= support
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
        URI uri = getURI(sitemapName, widgetId, response);

        if (uri != null) {
            Request httpRequest = httpClient.newRequest(uri);

            // check if the uri uses credentials and configure the http client accordingly
            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":");

                String user = userInfo[0];
                String password = userInfo[1];

                String basicAuthentication = "Basic " + B64Code.encode(user + ":" + password, StringUtil.__ISO_8859_1);
                httpRequest.header(HttpHeader.AUTHORIZATION, basicAuthentication);
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
    }

    /**
     * Given a sitemap and widget, return the URI referenced by the widget in the sitemap.
     * If the widget is not an Image or Video widget, then return <code>null</code>.
     * If the widget is associated with an item, attempt to use the item's state as a URL.
     * If the item's state as a string does not conform to RFC 2396, attempt to use the
     * <code>url=</code> attribute in the sitemap. If that too does not conform to RFC 2396,
     * then return <code>null</code>. In all cases where <code>null</code> is returned,
     * this method first sends {@link HttpServletResponse.SC_BAD_REQUEST} back to the client.
     *
     * @param sitemapName
     * @param widgetId
     * @param response the HttpServletResponse to which detailed errors are sent
     * @return the URI referenced by the widget
     */
    private URI getURI(String sitemapName, String widgetId, HttpServletResponse response) throws IOException {

        if (sitemapName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'sitemap' must be provided!");
            return null;
        }
        if (widgetId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'widget' must be provided!");
            return null;
        }

        Sitemap sitemap = (Sitemap) modelRepository.getModel(sitemapName);
        if (sitemap == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sitemap '" + sitemapName + "' could not be found!");
            return null;
        }
        Widget widget = itemUIRegistry.getWidget(sitemap, widgetId);
        if (widget == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Widget '" + widgetId + "' could not be found!");
            return null;
        }

        String uriString = null;
        if (widget instanceof Image) {
            uriString = ((Image) widget).getUrl();
        } else if (widget instanceof Video) {
            uriString = ((Video) widget).getUrl();
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Widget type '" + widget.getClass().getName() + "' is not supported!");
            return null;
        }

        String itemName = widget.getItem();
        if (itemName != null) {
            State state = itemUIRegistry.getItemState(itemName);
            if (state != null && state instanceof StringType) {
                try {
                    return URI.create(state.toString());
                } catch (IllegalArgumentException ex) {
                    // fall thru
                }
            }
        }

        try {
            return URI.create(uriString);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "URI '" + uriString + "' is not valid: " + e.getMessage());
            return null;
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
