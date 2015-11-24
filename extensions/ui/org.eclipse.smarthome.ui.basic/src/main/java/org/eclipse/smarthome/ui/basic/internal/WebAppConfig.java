/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.basic.internal;

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
    private final static boolean DEFAULT_ICONS_ENABLED = true;

    private String defaultSitemap = DEFAULT_SITEMAP;
    private String iconType = DEFAULT_ICON_TYPE;
    private boolean iconsEnabled = DEFAULT_ICONS_ENABLED;

    public void applyConfig(Map<String, Object> configProps) {
        String configIconsEnabled = (String) configProps.get("enableIcons");
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

        if (configIconsEnabled == null) {
            iconsEnabled = DEFAULT_ICONS_ENABLED;
        } else {
            iconsEnabled = "true".equalsIgnoreCase(configIconsEnabled);
        }
        iconType = configIconType;
        defaultSitemap = configDefaultSitemap;
    }

    public String getDefaultSitemap() {
        return defaultSitemap;
    }

    public String getIconType() {
        return iconType;
    }

    public boolean getIconsEnabled() {
        return iconsEnabled;
    }
}
