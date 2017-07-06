/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.classic.internal.render;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.classic.internal.WebAppActivator;
import org.eclipse.smarthome.ui.classic.internal.WebAppConfig;
import org.eclipse.smarthome.ui.classic.render.RenderException;
import org.eclipse.smarthome.ui.classic.render.WidgetRenderer;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an abstract implementation of a widget renderer. It provides
 * methods that are very useful for any widget renderer implementation,
 * so it should be subclassed by most concrete implementations.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
abstract public class AbstractWidgetRenderer implements WidgetRenderer {

    private final Logger logger = LoggerFactory.getLogger(AbstractWidgetRenderer.class);

    protected WebAppConfig config;

    protected ItemUIRegistry itemUIRegistry;

    /* the file extension of the snippets */
    protected static final String SNIPPET_EXT = ".html";

    /* the snippet location inside this bundle */
    protected static final String SNIPPET_LOCATION = "snippets/";

    /* a local cache so we do not have to read the snippets over and over again from the bundle */
    protected static final Map<String, String> snippetCache = new HashMap<String, String>();

    public void setItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = itemUIRegistry;
    }

    public void unsetItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = null;
    }

    public ItemUIRegistry getItemUIRegistry() {
        return itemUIRegistry;
    }

    protected void activate(ComponentContext context) {
    }

    protected void deactivate(ComponentContext context) {
    }

    /**
     * This method provides the html snippet for a given elementType of the sitemap model.
     *
     * @param elementType the name of the model type (e.g. "Group" or "Switch")
     * @return the html snippet to be used in the UI (including placeholders for variables)
     * @throws RenderException if snippet could not be read
     */
    protected synchronized String getSnippet(String elementType) throws RenderException {
        elementType = elementType.toLowerCase();
        String snippet = snippetCache.get(elementType);
        if (snippet == null) {
            String snippetLocation = SNIPPET_LOCATION + elementType + SNIPPET_EXT;
            URL entry = WebAppActivator.getContext().getBundle().getEntry(snippetLocation);
            if (entry != null) {
                try {
                    snippet = IOUtils.toString(entry.openStream());
                    if (!config.isHtmlCacheDisabled()) {
                        snippetCache.put(elementType, snippet);
                    }
                } catch (IOException e) {
                    logger.warn("Cannot load snippet for element type '{}'", elementType, e);
                }
            } else {
                throw new RenderException("Cannot find a snippet for element type '" + elementType + "'");
            }
        }
        return snippet;
    }

    /**
     * Retrieves the label for a widget and formats it for the WebApp.Net framework
     *
     * @param w the widget to retrieve the label for
     * @return the label to use for the widget
     */
    public String getLabel(Widget w) {
        return getLabel(w, null);
    }

    /**
     * Retrieves the label for a widget and formats it for the WebApp.Net framework
     *
     * @param w the widget to retrieve the label for
     * @param preferredValue the value to consider in place of the value between [ and ] if not null
     * @return the label to use for the widget
     */
    public String getLabel(Widget w, String preferredValue) {

        String label = itemUIRegistry.getLabel(w);
        int index = label.indexOf('[');
        int index2 = label.lastIndexOf(']');

        if (index != -1 && index2 != -1) {
            label = formatLabel(label.substring(0, index).trim(),
                    (preferredValue == null) ? label.substring(index + 1, index2) : preferredValue);
        } else {
            label = formatLabel(label, null);
        }

        return label;
    }

    /**
     * Formats the widget label for the WebApp.Net framework
     *
     * @param left the left part of the label
     * @param right the right part of the label; null if no right part to consider
     * @return the label to use for the widget
     */
    private String formatLabel(String left, String right) {
        String label = "<span style=\"%labelstyle%\" class=\"iLabel\">" + StringEscapeUtils.escapeHtml(left)
                + "</span>";
        if (right != null) {
            label += "<span class=\"iValue\" style=\"%valuestyle%\">" + StringEscapeUtils.escapeHtml(right) + "</span>";
        }
        return label;
    }

    /**
     * Escapes the path part of a URL as defined in RFC2396. This means, that for example the
     * path "/hello world" gets escaped to "/hello%20world".
     *
     * @param path The path of the URL that has to be escaped
     * @return The escaped path
     */
    protected String escapeURLPath(String path) {
        try {
            return URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException use) {
            logger.warn("Cannot escape string '{}'. Returning unmodified string.", path);
            return path;
        }
    }

    /**
     * Process the color tags - labelcolor and valuecolor
     *
     * @param w
     *            The widget to process
     * @param snippet
     *            The snippet to translate
     * @return The updated snippet
     */
    protected String processColor(Widget w, String snippet) {
        String style = "";
        String color = itemUIRegistry.getLabelColor(w);
        if (color != null) {
            style = "color:" + color;
        }
        snippet = StringUtils.replace(snippet, "%labelstyle%", style);

        style = "";
        color = itemUIRegistry.getValueColor(w);
        if (color != null) {
            style = "color:" + color;
        }
        snippet = StringUtils.replace(snippet, "%valuestyle%", style);

        return snippet;
    }

    protected String getFormat() {
        return config.getIconType();
    }

    protected String getState(Widget w) {
        State state = itemUIRegistry.getState(w);
        if (state != null) {
            return escapeURLPath(state.toString());
        } else {
            return "NULL";
        }
    }

    protected String getStateAsNumber(Widget w) {
        String itemName = w.getItem();
        if (itemName != null) {
            try {
                Item item = itemUIRegistry.getItem(itemName);
                State state = item.getState();
                if (item.getAcceptedDataTypes().contains(PercentType.class)) {
                    state = item.getStateAs(PercentType.class);
                } else {
                    state = item.getStateAs(DecimalType.class);
                }
                if (state != null) {
                    return escapeURLPath(state.toString());
                } else {
                    logger.debug("State '{}' of item '{}' is not a number!", item.getState(), itemName);
                }
            } catch (ItemNotFoundException e) {
                logger.error("Cannot retrieve item '{}' for widget {}",
                        new Object[] { itemName, w.eClass().getInstanceTypeName() });
            }
        }
        return "NULL";
    }

    protected String getCategory(Widget w) {
        String icon = escapeURLPath(itemUIRegistry.getCategory(w));
        return icon;
    }

    @Override
    public void setConfig(WebAppConfig config) {
        this.config = config;
    }

}
