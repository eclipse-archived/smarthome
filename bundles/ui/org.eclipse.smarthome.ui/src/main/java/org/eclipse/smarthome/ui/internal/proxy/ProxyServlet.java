/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.internal.proxy;

import java.net.URI;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.proxy.AsyncProxyServlet;
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
 * that it is possible to include resources (images/videos) from the LAN in the web UI. This is
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
 * the proxy servlet will be able to access the content and provide it to the web UIs through the
 * standard web authentication mechanism (if enabled).
 *
 * This servlet also supports data streams, such as a webcam video stream etc.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Svilen Valkanov - Replaced Apache HttpClient with Jetty
 * @author John Cocula - added optional Image/Video item= support; refactor
 */
public class ProxyServlet extends AsyncProxyServlet {

    public static final String PROXY_ALIAS = "proxy";
    private static final String CONFIG_MAX_THREADS = "maxThreads";
    private static final int DEFAULT_MAX_THREADS = 8;
    private static final String ATTR_URI = ProxyServlet.class.getName() + ".URI";

    private final Logger logger = LoggerFactory.getLogger(ProxyServlet.class);

    private static final long serialVersionUID = -4716754591953017793L;

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

    protected void activate(Map<String, Object> config) {
        try {
            logger.debug("Starting up proxy servlet at /{}", PROXY_ALIAS);
            Hashtable<String, String> props = propsFromConfig(config);
            httpService.registerServlet("/" + PROXY_ALIAS, this, props, createHttpContext());
        } catch (NamespaceException | ServletException e) {
            logger.error("Error during servlet startup: {}", e.getMessage());
        }
    }

    protected void deactivate() {
        try {
            httpService.unregister("/" + PROXY_ALIAS);
        } catch (IllegalArgumentException e) {
            // ignore, had not been registered before
        }
    }

    /**
     * Copy the ConfigAdminManager's config to the init parameters of the servlet.
     *
     * @param config the OSGi config, may be <code>null</code>
     * @return properties to pass to servlet for initialization
     */
    private Hashtable<String, String> propsFromConfig(Map<String, Object> config) {
        Hashtable<String, String> props = new Hashtable<String, String>();

        if (config != null) {
            for (String key : config.keySet()) {
                props.put(key, config.get(key).toString());
            }
        }

        // must specify, per http://stackoverflow.com/a/27625380
        if (props.get(CONFIG_MAX_THREADS) == null) {
            props.put(CONFIG_MAX_THREADS, String.valueOf(DEFAULT_MAX_THREADS));
        }

        return props;
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

    /**
     * {@inheritDoc}
     *
     * Override <code>newHttpClient</code> so we can proxy to HTTPS URIs.
     */
    @Override
    protected HttpClient newHttpClient() {
        return new HttpClient(new SslContextFactory());
    }

    /**
     * {@inheritDoc}
     *
     * Add Basic Authentication header to request if user and password are specified in URI.
     * After Jetty is upgrade past 9.2.9, change to copyRequestHeaders to avoid deprecated warning.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void copyHeaders(HttpServletRequest clientRequest, Request proxyRequest) {
        super.copyHeaders(clientRequest, proxyRequest);

        // check if the URI uses credentials and configure the HTTP client accordingly
        URI uri = uriFromRequest(clientRequest);
        if (uri != null && uri.getUserInfo() != null) {
            String[] userInfo = uri.getUserInfo().split(":");

            if (userInfo.length == 2) {
                String user = userInfo[0];
                String password = userInfo[1];

                String basicAuthentication = "Basic " + B64Code.encode(user + ":" + password, StringUtil.__ISO_8859_1);
                proxyRequest.header(HttpHeader.AUTHORIZATION, basicAuthentication);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String rewriteTarget(HttpServletRequest request) {
        return Objects.toString(uriFromRequest(request), null);
    }

    /**
     * Determine which URI to address based on the request contents.
     *
     * @param request the servlet request
     * @return the URI indicated by the request, or <code>null</code> if not possible
     */
    private URI uriFromRequest(HttpServletRequest request) {

        // Return any URI we've already saved for this request
        URI uri = (URI) request.getAttribute(ATTR_URI);
        if (uri != null) {
            return uri;
        }

        String sitemapName = request.getParameter("sitemap");
        if (sitemapName == null) {
            logger.error("Parameter 'sitemap' must be provided!");
            return null;
        }

        String widgetId = request.getParameter("widgetId");
        if (widgetId == null) {
            logger.error("Parameter 'widgetId' must be provided!");
            return null;
        }

        Sitemap sitemap = (Sitemap) modelRepository.getModel(sitemapName);
        if (sitemap == null) {
            logger.error("Sitemap '{}' could not be found!", sitemapName);
            return null;
        }

        Widget widget = itemUIRegistry.getWidget(sitemap, widgetId);
        if (widget == null) {
            logger.error("Widget '{}' could not be found!", widgetId);
            return null;
        }

        String uriString = null;
        if (widget instanceof Image) {
            uriString = ((Image) widget).getUrl();
        } else if (widget instanceof Video) {
            uriString = ((Video) widget).getUrl();
        } else {
            logger.error("Widget type '{}' is not supported!", widget.getClass().getName());
            return null;
        }

        String itemName = widget.getItem();
        if (itemName != null) {
            State state = itemUIRegistry.getItemState(itemName);
            if (state != null && state instanceof StringType) {
                try {
                    uri = URI.create(state.toString());
                    request.setAttribute(ATTR_URI, uri);
                    return uri;
                } catch (IllegalArgumentException ex) {
                    // fall thru
                }
            }
        }

        try {
            uri = URI.create(uriString);
            request.setAttribute(ATTR_URI, uri);
            return uri;
        } catch (IllegalArgumentException e) {
            logger.error("URI '{}' is not valid: {}", uriString, e.getMessage());
            return null;
        }
    }
}
