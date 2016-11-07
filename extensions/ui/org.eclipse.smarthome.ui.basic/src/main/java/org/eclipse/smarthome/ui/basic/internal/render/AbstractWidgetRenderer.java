/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.basic.internal.render;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.basic.internal.WebAppActivator;
import org.eclipse.smarthome.ui.basic.internal.WebAppConfig;
import org.eclipse.smarthome.ui.basic.render.RenderException;
import org.eclipse.smarthome.ui.basic.render.WidgetRenderer;
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
 * @author Vlad Ivanov - BasicUI changes
 *
 */
abstract public class AbstractWidgetRenderer implements WidgetRenderer {

    private final Logger logger = LoggerFactory.getLogger(AbstractWidgetRenderer.class);

    protected ItemUIRegistry itemUIRegistry;

    protected WebAppConfig config;

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
     * Replace some common values in the widget template
     *
     * @param snippet snippet html code
     * @param w corresponding widget
     * @return
     */
    protected String preprocessSnippet(String snippet, Widget w) {
        snippet = StringUtils.replace(snippet, "%widget_id%", itemUIRegistry.getWidgetId(w));
        snippet = StringUtils.replace(snippet, "%icon_type%", config.getIconType());
        snippet = StringUtils.replace(snippet, "%item%", w.getItem() != null ? w.getItem() : "");
        snippet = StringUtils.replace(snippet, "%label%", getLabel(w));
        snippet = StringUtils.replace(snippet, "%value%", getValue(w));
        snippet = StringUtils.replace(snippet, "%visibility_class%",
                itemUIRegistry.getVisiblity(w) ? "" : "mdl-form__row--hidden");

        String state = getState(w);
        snippet = StringUtils.replace(snippet, "%state%", state == null ? "" : escapeURL(state));

        String category = getCategory(w);
        snippet = StringUtils.replace(snippet, "%category%", escapeURL(category));

        return snippet;
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
                    snippetCache.put(elementType, snippet);
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
     * Retrieves the label for a widget
     *
     * @param w the widget to retrieve the label for
     * @return the label to use for the widget
     */
    public String getLabel(Widget w) {
        String label = itemUIRegistry.getLabel(w);
        int index = label.indexOf('[');

        if (index != -1) {
            label = label.substring(0, index);
        }

        return escapeHtml(label);
    }

    /**
     * Returns formatted value of the item associated to widget
     *
     * @param w widget to get value for
     * @return value to use for the widget
     */
    public String getValue(Widget w) {
        String label = itemUIRegistry.getLabel(w);
        int index = label.indexOf('[');

        if (index != -1) {
            return escapeHtml(label.substring(index + 1, label.length() - 1));
        } else {
            return "";
        }
    }

    /**
     * Escapes parts of a URL. This means, that for example the
     * path "/hello world" gets escaped to "/hello+world".
     *
     * @param string The string that has to be escaped
     * @return The escaped string
     */
    protected String escapeURL(String string) {
        if (string == null) {
            return null;
        }

        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException use) {
            logger.warn("Cannot escape string '{}'. Returning unmodified string.", string);
            return string;
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
        String color = "";

        color = itemUIRegistry.getLabelColor(w);

        if (color != null) {
            style = "style=\"color:" + color + "\"";
        }
        snippet = StringUtils.replace(snippet, "%labelstyle%", style);

        style = "";
        color = itemUIRegistry.getValueColor(w);

        if (color != null) {
            style = "style=\"color:" + color + "\"";
        }
        snippet = StringUtils.replace(snippet, "%valuestyle%", style);

        return snippet;
    }

    protected String getCategory(Widget w) {
        return itemUIRegistry.getCategory(w);
    }

    protected String getState(Widget w) {
        State state = itemUIRegistry.getState(w);
        if (state != null) {
            return state.toString();
        } else {
            return "NULL";
        }
    }

    protected String escapeHtml(String s) {
        return StringEscapeUtils.escapeHtml(s);
    }

    @Override
    public void setConfig(WebAppConfig config) {
        this.config = config;
    }
}
