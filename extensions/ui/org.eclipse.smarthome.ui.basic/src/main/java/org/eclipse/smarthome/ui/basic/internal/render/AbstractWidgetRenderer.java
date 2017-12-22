/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.util.UnitUtils;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.basic.internal.WebAppActivator;
import org.eclipse.smarthome.ui.basic.internal.WebAppConfig;
import org.eclipse.smarthome.ui.basic.render.RenderException;
import org.eclipse.smarthome.ui.basic.render.WidgetRenderer;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.osgi.framework.BundleContext;
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

    private static final Pattern LABEL_PATTERN = Pattern.compile(".*?\\[.*? (.*?)\\]");

    protected ItemUIRegistry itemUIRegistry;
    protected TranslationProvider i18nProvider;
    protected LocaleProvider localeProvider;

    protected WebAppConfig config;

    private BundleContext bundleContext;

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

    public void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    public void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    public void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }

    protected void activate(BundleContext context) {
        this.bundleContext = context;
    }

    protected void deactivate(BundleContext context) {
        this.bundleContext = null;
    }

    /**
     * Replace some common values in the widget template
     *
     * @param snippet snippet html code
     * @param w corresponding widget
     * @return
     */
    protected String preprocessSnippet(String originalSnippet, Widget w) {
        String snippet = originalSnippet;
        snippet = StringUtils.replace(snippet, "%widget_id%", itemUIRegistry.getWidgetId(w));
        snippet = StringUtils.replace(snippet, "%icon_type%", config.getIconType());
        snippet = StringUtils.replace(snippet, "%item%", w.getItem() != null ? w.getItem() : "");
        // Optimization: avoid calling 3 times itemUIRegistry.getLabel(w)
        String text = itemUIRegistry.getLabel(w);
        snippet = StringUtils.replace(snippet, "%label%", getLabel(text));
        snippet = StringUtils.replace(snippet, "%value%", getValue(text));
        snippet = StringUtils.replace(snippet, "%has_value%", new Boolean(hasValue(text)).toString());
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
        String lowerTypeElementType = elementType.toLowerCase();
        String snippet = snippetCache.get(lowerTypeElementType);
        if (snippet == null) {
            String snippetLocation = SNIPPET_LOCATION + lowerTypeElementType + SNIPPET_EXT;
            URL entry = WebAppActivator.getContext().getBundle().getEntry(snippetLocation);
            if (entry != null) {
                try {
                    snippet = IOUtils.toString(entry.openStream());
                    snippetCache.put(lowerTypeElementType, snippet);
                } catch (IOException e) {
                    logger.warn("Cannot load snippet for element type '{}'", lowerTypeElementType, e);
                }
            } else {
                throw new RenderException("Cannot find a snippet for element type '" + lowerTypeElementType + "'");
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
        return getLabel(itemUIRegistry.getLabel(w));
    }

    /**
     * Retrieves the label for a widget
     *
     * @param text the text containing the label and an optional value around []
     * @return the label extracted from the text
     */
    protected String getLabel(String text) {
        int index = text.indexOf('[');

        if (index != -1) {
            return escapeHtml(text.substring(0, index));
        } else {
            return escapeHtml(text);
        }
    }

    /**
     * Returns formatted value of the item associated to widget
     *
     * @param w widget to get value for
     * @return value to use for the widget
     */
    public String getValue(Widget w) {
        return getValue(itemUIRegistry.getLabel(w));
    }

    /**
     * Returns formatted value of the item associated to widget
     *
     * @param text the text containing the label and an optional value around []
     * @return the value extracted from the text or "" if not present
     */
    protected String getValue(String text) {
        int index = text.indexOf('[');

        if (index != -1) {
            return escapeHtml(text.substring(index + 1, text.length() - 1));
        } else {
            return "";
        }
    }

    /**
     * Returns whether the item associated to widget has a value or not
     *
     * @param w widget
     * @return true if the item associated to widget has a value
     */
    public boolean hasValue(Widget w) {
        return hasValue(itemUIRegistry.getLabel(w));
    }

    /**
     * Returns whether the item associated to widget has a value or not
     *
     * @param text the text containing the label and an optional value around []
     * @return true if the text contains a value
     */
    protected boolean hasValue(String text) {
        return (text.indexOf('[') != -1);
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
    protected String processColor(Widget w, String originalSnippet) {
        String style = "";
        String color = "";
        String snippet = originalSnippet;

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

    protected String localizeText(String key) {
        String result = "";
        if (I18nUtil.isConstant(key)) {
            result = this.i18nProvider.getText(this.bundleContext.getBundle(), I18nUtil.stripConstant(key), "",
                    this.localeProvider.getLocale());
        }
        return result;
    }

    protected String getUnitForWidget(Widget w) {
        String unit = getUnitFromLabel(w.getLabel());
        if (StringUtils.isNotBlank(unit) && !UnitUtils.UNIT_PLACEHOLDER.equals(unit)) {
            return unit;
        }

        try {
            Item item = itemUIRegistry.getItem(w.getItem());
            if (item instanceof NumberItem && ((NumberItem) item).getDimension() != null) {
                return ((NumberItem) item).getUnitSymbol();
            }
        } catch (ItemNotFoundException e) {
            logger.debug("Failed to retrieve item during widget rendering: {}", e.getMessage());
        }

        return "";
    }

    protected State convertStateToLabelUnit(QuantityType state, String label) {
        String labelUnit = label.lastIndexOf(" ") > 0 ? label.substring(label.lastIndexOf(" ")) : null;
        if (labelUnit != null && !state.getUnit().toString().equals(labelUnit)) {
            return state.toUnit(labelUnit);
        }

        return state;
    }

    private String getUnitFromLabel(String label) {
        if (StringUtils.isBlank(label)) {
            return null;
        }

        Matcher m = LABEL_PATTERN.matcher(label);
        if (m.matches()) {
            return m.group(1);
        }

        return null;
    }
}
