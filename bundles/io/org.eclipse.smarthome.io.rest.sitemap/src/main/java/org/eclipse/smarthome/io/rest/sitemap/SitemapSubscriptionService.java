/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.io.rest.sitemap.internal.PageChangeListener;
import org.eclipse.smarthome.io.rest.sitemap.internal.SitemapEvent;
import org.eclipse.smarthome.model.core.EventType;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.core.ModelRepositoryChangeListener;
import org.eclipse.smarthome.model.sitemap.LinkableWidget;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SitemapSubscriptionService implements ModelRepositoryChangeListener {

    private static final String SITEMAP_PAGE_SEPARATOR = "#";
    private static final String SITEMAP_SUFFIX = ".sitemap";

    private final Logger logger = LoggerFactory.getLogger(SitemapSubscriptionService.class);

    public interface SitemapSubscriptionCallback {

        void onEvent(SitemapEvent event);
    }

    private ItemUIRegistry itemUIRegistry;
    private ModelRepository modelRepo;
    private List<SitemapProvider> sitemapProviders = new ArrayList<>();

    /* subscription id -> sitemap+page */
    private final Map<String, String> pageOfSubscription = new ConcurrentHashMap<>();

    /* subscription id -> callback */
    private Map<String, SitemapSubscriptionCallback> callbacks = new ConcurrentHashMap<>();

    /* sitemap+page -> listener */
    private Map<String, PageChangeListener> pageChangeListeners = new ConcurrentHashMap<>();

    public SitemapSubscriptionService() {
    }

    protected void activate() {
    }

    protected void deactivate() {
        pageOfSubscription.clear();
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

    protected void addModelRepository(ModelRepository modelRepo) {
        this.modelRepo = modelRepo;
        this.modelRepo.addModelRepositoryChangeListener(this);
    }

    protected void removeModelRepository(ModelRepository modelRepo) {
        this.modelRepo.removeModelRepositoryChangeListener(this);
        this.modelRepo = null;
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
        logger.debug("Created new subscription with id {}", subscriptionId);
        return subscriptionId;
    }

    /**
     * Removes an existing subscription
     *
     * @param subscriptionId the id of the subscription to remove
     */
    public void removeSubscription(String subscriptionId) {
        callbacks.remove(subscriptionId);
        String sitemapPage = pageOfSubscription.remove(subscriptionId);
        if (sitemapPage != null && !pageOfSubscription.values().contains(sitemapPage)) {
            // this was the only subscription listening on this page, so we can dispose the listener
            PageChangeListener listener = pageChangeListeners.remove(sitemapPage);
            if (listener != null) {
                listener.dispose();
            }
        }
        logger.debug("Removed subscription with id {}", subscriptionId);
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
        return extractPageId(pageOfSubscription.get(subscriptionId));
    }

    /**
     * Retrieves the current sitemap name for a subscription.
     *
     * @param subscriptionId the subscription to get the sitemap name for
     * @return the name of the current sitemap
     */
    public String getSitemapName(String subscriptionId) {
        return extractSitemapName(pageOfSubscription.get(subscriptionId));
    }

    private String extractSitemapName(String sitemapWithPageId) {
        return sitemapWithPageId.split(SITEMAP_PAGE_SEPARATOR)[0];
    }

    private String extractPageId(String sitemapWithPageId) {
        return sitemapWithPageId.split(SITEMAP_PAGE_SEPARATOR)[1];
    }

    /**
     * Updates the subscription to send events for the provided page id.
     *
     * @param subscriptionId the subscription to update
     * @param sitemapName the current sitemap name
     * @param pageId the current page id
     */
    public void setPageId(String subscriptionId, String sitemapName, String pageId) {
        SitemapSubscriptionCallback callback = callbacks.get(subscriptionId);
        if (callback != null) {
            String oldSitemapPage = pageOfSubscription.remove(subscriptionId);
            if (oldSitemapPage != null) {
                removeCallbackFromListener(oldSitemapPage, callback);
            }
            addCallbackToListener(sitemapName, pageId, callback);
            pageOfSubscription.put(subscriptionId, getValue(sitemapName, pageId));

            logger.debug("Subscription {} changed to page {} of sitemap {}",
                    new Object[] { subscriptionId, pageId, sitemapName });
        } else {
            throw new IllegalArgumentException("Subscription " + subscriptionId + " does not exist!");
        }
    }

    private void addCallbackToListener(String sitemapName, String pageId, SitemapSubscriptionCallback callback) {
        PageChangeListener listener = pageChangeListeners.get(getValue(sitemapName, pageId));
        if (listener == null) {
            // there is no listener for this page yet, so let's try to create one
            listener = new PageChangeListener(sitemapName, pageId, itemUIRegistry, collectWidgets(sitemapName, pageId));
            pageChangeListeners.put(getValue(sitemapName, pageId), listener);
        }
        if (listener != null) {
            listener.addCallback(callback);
        }
    }

    private EList<Widget> collectWidgets(String sitemapName, String pageId) {
        EList<Widget> widgets = new BasicEList<Widget>();

        Sitemap sitemap = getSitemap(sitemapName);
        if (sitemap != null) {
            if (pageId.equals(sitemap.getName())) {
                widgets = itemUIRegistry.getChildren(sitemap);
            } else {
                Widget pageWidget = itemUIRegistry.getWidget(sitemap, pageId);
                if (pageWidget instanceof LinkableWidget) {
                    widgets = itemUIRegistry.getChildren((LinkableWidget) pageWidget);
                    // We add the page widget. It will help any UI to update the page title.
                    widgets.add(pageWidget);
                }
            }
        }
        return widgets;
    }

    private void removeCallbackFromListener(String sitemapPage, SitemapSubscriptionCallback callback) {
        PageChangeListener oldListener = pageChangeListeners.get(sitemapPage);
        if (oldListener != null) {
            oldListener.removeCallback(callback);
            if (!pageOfSubscription.values().contains(sitemapPage)) {
                // no other callbacks are left here, so we can safely dispose the listener
                oldListener.dispose();
                pageChangeListeners.remove(sitemapPage);
            }
        }
    }

    private String getValue(String sitemapName, String pageId) {
        return sitemapName + SITEMAP_PAGE_SEPARATOR + pageId;
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

    @Override
    public void modelChanged(String modelName, EventType type) {
        if (type != EventType.MODIFIED || !modelName.endsWith(SITEMAP_SUFFIX)) {
            return; // we process only sitemap modifications here
        }

        String changedSitemapName = StringUtils.removeEnd(modelName, SITEMAP_SUFFIX);

        for (Entry<String, PageChangeListener> listenerEntry : pageChangeListeners.entrySet()) {
            String sitemapWithPage = listenerEntry.getKey();
            String sitemapName = extractSitemapName(sitemapWithPage);

            if (sitemapName.equals(changedSitemapName)) {
                listenerEntry.getValue().sitemapContentChanged();
            }
        }
    }
}
