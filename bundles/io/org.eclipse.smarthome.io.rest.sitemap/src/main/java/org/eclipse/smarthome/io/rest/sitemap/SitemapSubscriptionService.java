/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.io.rest.sitemap.internal.PageChangeListener;
import org.eclipse.smarthome.io.rest.sitemap.internal.SitemapEvent;
import org.eclipse.smarthome.model.sitemap.LinkableWidget;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;

/**
 * This is a service that provides the possibility to manage subscriptions to sitemaps.
 * As such subscriptions are stateful, they need to be created and removed upon disposal.
 * The subscription mechanism makes sure that only events for widgets of the currently active sitemap page are sent as
 * events to the subscriber.
 * For this to work correctly, the subscriber needs to make sure that setPageId is called whenever it switches to a new
 * page.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class SitemapSubscriptionService {

    public interface SitemapSubscriptionCallback {

        void onEvent(SitemapEvent event);
    }

    private final Map<String, String> pageOfSubscriptionMap = new ConcurrentHashMap<>();
    private ItemUIRegistry itemUIRegistry;
    private Map<String, SitemapSubscriptionCallback> callbacks = new ConcurrentHashMap<>();
    private Map<String, PageChangeListener> pageChangeListeners = new ConcurrentHashMap<>();
    private List<SitemapProvider> sitemapProviders = new ArrayList<>();

    public SitemapSubscriptionService() {
    }

    protected void activate() {
    }

    protected void deactivate() {
        pageOfSubscriptionMap.clear();
        callbacks.clear();
        for (PageChangeListener listener : pageChangeListeners.values()) {
            listener.dispose();
        }
        pageChangeListeners.clear();
    }

    protected void setItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = itemUIRegistry;
    }

    protected void unsetItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = null;
    }

    protected void addSitemapProvider(SitemapProvider provider) {
        sitemapProviders.add(provider);
    }

    protected void removeSitemapProvider(SitemapProvider provider) {
        sitemapProviders.remove(provider);
    }

    /**
     * Creates a new subscription with the given id.
     *
     * @param callback an instance that should receive the events
     * @returns a unique id that identifies the subscription
     */
    public String createSubscription(SitemapSubscriptionCallback callback) {
        String subscriptionId = UUID.randomUUID().toString();
        callbacks.put(subscriptionId, callback);
        return subscriptionId;
    }

    /**
     * Removes an existing subscription
     *
     * @param subscriptionId the id of the subscription to remove
     */
    public void removeSubscription(String subscriptionId) {
        pageOfSubscriptionMap.remove(subscriptionId);
        callbacks.remove(subscriptionId);
        PageChangeListener listener = pageChangeListeners.remove(subscriptionId);
        if (listener != null) {
            listener.dispose();
        }
    }

    /**
     * Checks whether a subscription with a given id (still) exists.
     *
     * @param subscriptionId the id of the subscription to check
     * @return true, if it exists, false otherwise
     */
    public boolean exists(String subscriptionId) {
        return callbacks.containsKey(subscriptionId);
    }

    /**
     * Retrieves the current page id for a subscription.
     *
     * @param subscriptionId the subscription to get the page id for
     * @return the id of the currently active page
     */
    public String getPageId(String subscriptionId) {
        return pageOfSubscriptionMap.get(subscriptionId).split("-")[1];
    }

    /**
     * Retrieves the current sitemap name for a subscription.
     *
     * @param subscriptionId the subscription to get the sitemap name for
     * @return the name of the current sitemap
     */
    public String getSitemapName(String subscriptionId) {
        return pageOfSubscriptionMap.get(subscriptionId).split("-")[0];
    }

    /**
     * Updates the subscription to send events for the provided page id.
     *
     * @param subscriptionId the subscription to update
     * @param sitemapName the current sitemap name
     * @param pageId the current page id
     */
    public void setPageId(String subscriptionId, String sitemapName, String pageId) {
        if (exists(subscriptionId)) {
            pageOfSubscriptionMap.put(subscriptionId, getValue(sitemapName, pageId));
            removeOldListener(subscriptionId);
            initNewListener(subscriptionId, sitemapName, pageId);
        } else {
            throw new IllegalArgumentException("Subscription " + subscriptionId + " does not exist!");
        }
    }

    private void initNewListener(String subscriptionId, String sitemapName, String pageId) {
        EList<Widget> widgets = null;
        Sitemap sitemap = getSitemap(sitemapName);
        if (sitemap != null) {
            if (pageId.equals(sitemap.getName())) {
                widgets = sitemap.getChildren();
            } else {
                Widget pageWidget = itemUIRegistry.getWidget(sitemap, pageId);
                if (pageWidget instanceof LinkableWidget) {
                    widgets = itemUIRegistry.getChildren((LinkableWidget) pageWidget);
                }
            }
        }
        if (widgets != null) {
            PageChangeListener listener = new PageChangeListener(sitemapName, pageId, itemUIRegistry, widgets,
                    callbacks.get(subscriptionId));
            pageChangeListeners.put(subscriptionId, listener);
        }
    }

    private void removeOldListener(String subscriptionId) {
        PageChangeListener oldListener = pageChangeListeners.get(subscriptionId);
        if (oldListener != null) {
            oldListener.dispose();
        }
    }

    private String getValue(String sitemapName, String pageId) {
        return sitemapName + "-" + pageId;
    }

    private Sitemap getSitemap(String sitemapName) {
        for (SitemapProvider provider : sitemapProviders) {
            Sitemap sitemap = provider.getSitemap(sitemapName);
            if (sitemap != null) {
                return sitemap;
            }
        }
        return null;
    }
}
