/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.basic.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds BasicUI configuration values
 * and validates newly applied values.
 *
 * @author Vlad Ivanov - Initial contribution
 */
public class WebAppConfig {
    private final static String DEFAULT_SITEMAP = "default";
    private final static String DEFAULT_ICON_TYPE = "png";

    private String defaultSitemap = DEFAULT_SITEMAP;
    private String iconType = DEFAULT_ICON_TYPE;

    private List<String> cssClassList = new ArrayList<String>();

    private static final Map<String, String> cssClasses;
    private static final Map<String, Boolean> cssDefaultValues;

    private final static String CONFIG_ENABLE_ICONS = "enableIcons";
    private final static String CONFIG_CONDENSED_LAYOUT = "condensedLayout";
    private final static String CONFIG_CAPITALIZE = "capitalizeValues";

    static {
        cssClasses = new HashMap<String, String>();
        cssClasses.put(CONFIG_ENABLE_ICONS, "ui-icons-enabled");
        cssClasses.put(CONFIG_CONDENSED_LAYOUT, "ui-layout-condensed");
        cssClasses.put(CONFIG_CAPITALIZE, "ui-capitalize-values");

        cssDefaultValues = new HashMap<String, Boolean>();
        cssDefaultValues.put(CONFIG_ENABLE_ICONS, true);
        cssDefaultValues.put(CONFIG_CONDENSED_LAYOUT, false);
        cssDefaultValues.put(CONFIG_CAPITALIZE, false);
    }

    private void applyCssClasses(Map<String, Object> configProps) {
        cssClassList.clear();

        for (String key : cssClasses.keySet()) {
            Boolean value = cssDefaultValues.get(key);
            if (configProps.containsKey(key)) {
                value = configProps.get(key).toString().equalsIgnoreCase("true");
            }
            if (value) {
                cssClassList.add(cssClasses.get(key));
            }
        }
    }

    public void applyConfig(Map<String, Object> configProps) {
        String configIconType = (String) configProps.get("iconType");
        String configDefaultSitemap = (String) configProps.get("defaultSitemap");

        if (configDefaultSitemap == null) {
            configDefaultSitemap = DEFAULT_SITEMAP;
        }

        if (configIconType == null) {
            configIconType = DEFAULT_ICON_TYPE;
        } else if (!configIconType.equalsIgnoreCase("svg") && !configIconType.equalsIgnoreCase("png")) {
            configIconType = DEFAULT_ICON_TYPE;
        }

        iconType = configIconType;
        defaultSitemap = configDefaultSitemap;

        applyCssClasses(configProps);
    }

    public String getDefaultSitemap() {
        return defaultSitemap;
    }

    public String getIconType() {
        return iconType;
    }

    public String getCssClassList() {
        String result = " ";
        for (String item : cssClassList) {
            result += item + " ";
        }
        return result;
    }
}
