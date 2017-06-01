/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.basic.internal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.io.rest.sitemap.SitemapSubscriptionService;
import org.eclipse.smarthome.model.sitemap.LinkableWidget;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.basic.internal.WebAppConfig;
import org.eclipse.smarthome.ui.basic.internal.render.PageRenderer;
import org.eclipse.smarthome.ui.basic.render.RenderException;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main servlet for the Basic UI.
 * It serves the Html code based on the sitemap model.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - BasicUI changes
 *
 */
public class WebAppServlet extends BaseServlet {

    private final Logger logger = LoggerFactory.getLogger(WebAppServlet.class);

    /** the name of the servlet to be used in the URL */
    public static final String SERVLET_NAME = "app";

    private static final String CONTENT_TYPE_ASYNC = "application/xml;charset=UTF-8";
    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    private PageRenderer renderer;
    private SitemapSubscriptionService subscriptions;
    private WebAppConfig config = new WebAppConfig();
    protected Set<SitemapProvider> sitemapProviders = new CopyOnWriteArraySet<>();

    public void setSitemapSubscriptionService(SitemapSubscriptionService subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void unsetSitemapSubscriptionService(SitemapSubscriptionService subscriptions) {
        this.subscriptions = null;
    }

    public void addSitemapProvider(SitemapProvider sitemapProvider) {
        this.sitemapProviders.add(sitemapProvider);
    }

    public void removeSitemapProvider(SitemapProvider sitemapProvider) {
        this.sitemapProviders.remove(sitemapProvider);
    }

    public void setPageRenderer(PageRenderer renderer) {
        renderer.setConfig(config);
        this.renderer = renderer;
    }

    protected void activate(Map<String, Object> configProps) {
        config.applyConfig(configProps);
        try {
            Hashtable<String, String> props = new Hashtable<String, String>();
            httpService.registerServlet(WEBAPP_ALIAS + "/" + SERVLET_NAME, this, props, createHttpContext());
            httpService.registerResources(WEBAPP_ALIAS, "web", null);
            logger.info("Started Basic UI at " + WEBAPP_ALIAS + "/" + SERVLET_NAME);
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        } catch (ServletException e) {
            logger.error("Error during servlet startup", e);
        }
    }

    protected void modified(Map<String, Object> configProps) {
        config.applyConfig(configProps);
    }

    protected void deactivate() {
        httpService.unregister(WEBAPP_ALIAS + "/" + SERVLET_NAME);
        httpService.unregister(WEBAPP_ALIAS);
        logger.info("Stopped Basic UI");
    }

    private void showSitemapList(ServletResponse res) throws IOException, RenderException {
        PrintWriter resWriter;
        resWriter = res.getWriter();
        resWriter.append(renderer.renderSitemapList(sitemapProviders));

        res.setContentType(CONTENT_TYPE);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        logger.debug("Servlet request received!");

        // read request parameters
        String sitemapName = req.getParameter("sitemap");
        String widgetId = req.getParameter("w");
        String subscriptionId = req.getParameter("subscriptionId");
        boolean async = "true".equalsIgnoreCase(req.getParameter("__async"));

        if (sitemapName == null) {
            sitemapName = config.getDefaultSitemap();
        }

        StringBuilder result = new StringBuilder();
        Sitemap sitemap = null;

        for (SitemapProvider sitemapProvider : sitemapProviders) {
            sitemap = sitemapProvider.getSitemap(sitemapName);
            if (sitemap != null) {
                break;
            }
        }

        try {
            if (sitemap == null) {
                showSitemapList(res);
                return;
            }

            logger.debug("reading sitemap {}", sitemap.getName());
            if (widgetId == null || widgetId.isEmpty() || widgetId.equals(sitemapName)) {
                // we are at the homepage, so we render the children of the sitemap root node
                if (subscriptionId != null) {
                    if (subscriptions.exists(subscriptionId)) {
                        subscriptions.setPageId(subscriptionId, sitemap.getName(), sitemapName);
                    } else {
                        logger.debug("Basic UI requested a non-existing event subscription id ({})", subscriptionId);
                    }
                }
                String label = sitemap.getLabel() != null ? sitemap.getLabel() : sitemapName;
                EList<Widget> children = renderer.getItemUIRegistry().getChildren(sitemap);
                result.append(renderer.processPage(sitemapName, sitemapName, label, children, async));
            } else if (!widgetId.equals("Colorpicker")) {
                // we are on some subpage, so we have to render the children of the widget that has been selected
                if (subscriptionId != null) {
                    if (subscriptions.exists(subscriptionId)) {
                        subscriptions.setPageId(subscriptionId, sitemap.getName(), widgetId);
                    } else {
                        logger.debug("Basic UI requested a non-existing event subscription id ({})", subscriptionId);
                    }
                }
                Widget w = renderer.getItemUIRegistry().getWidget(sitemap, widgetId);
                if (w != null) {
                    String label = renderer.getItemUIRegistry().getLabel(w);
                    if (label == null) {
                        label = "undefined";
                    }
                    if (!(w instanceof LinkableWidget)) {
                        throw new RenderException("Widget '" + w + "' can not have any content");
                    }
                    EList<Widget> children = renderer.getItemUIRegistry().getChildren((LinkableWidget) w);
                    result.append(renderer.processPage(renderer.getItemUIRegistry().getWidgetId(w), sitemapName, label,
                            children, async));
                }
            }
        } catch (RenderException e) {
            throw new ServletException(e.getMessage(), e);
        }
        if (async) {
            res.setContentType(CONTENT_TYPE_ASYNC);
        } else {
            res.setContentType(CONTENT_TYPE);
        }
        res.getWriter().append(result);
        res.getWriter().close();
    }

}
