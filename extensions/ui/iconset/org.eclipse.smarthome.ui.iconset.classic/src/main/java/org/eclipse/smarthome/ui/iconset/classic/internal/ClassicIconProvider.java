/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.iconset.classic.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.ui.icon.AbstractResourceIconProvider;
import org.eclipse.smarthome.ui.icon.IconProvider;
import org.eclipse.smarthome.ui.icon.IconSet;
import org.eclipse.smarthome.ui.icon.IconSet.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This icon provider provides the classic icons (dating from openHAB). They are packaged
 * within this bundle and served from there.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class ClassicIconProvider extends AbstractResourceIconProvider implements IconProvider {

    private final Logger logger = LoggerFactory.getLogger(ClassicIconProvider.class);

    static String ICONSET_ID = "classic";

    @Override
    public Set<IconSet> getIconSets(Locale locale) {
        Set<Format> formats = new HashSet<>(2);
        formats.add(Format.PNG);
        formats.add(Format.SVG);
        String label = i18nProvider.getText(context.getBundle(), "iconset.label", "Classic Icons", locale);
        String description = i18nProvider.getText(context.getBundle(), "iconset.description",
                "This is a modernized version of the original icon set of openHAB 1.", locale);
        IconSet iconSet = new IconSet(ICONSET_ID, label, description, formats);
        return Collections.singleton(iconSet);
    }

    @Override
    protected InputStream getResource(String iconSetId, String resourceName) {
        if (ICONSET_ID.equals(iconSetId)) {
            URL iconResource = context.getBundle().getEntry("icons" + File.separator + resourceName);
            try {
                return iconResource.openStream();
            } catch (IOException e) {
                logger.error("Failed to read icon '{}': {}", resourceName, e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected boolean hasResource(String iconSetId, String resourceName) {
        if (ICONSET_ID.equals(iconSetId)) {
            URL iconResource = context.getBundle().getEntry("icons" + File.separator + resourceName);
            return iconResource != null;
        } else {
            return false;
        }
    }

    @Override
    protected Integer getPriority() {
        return 0;
    }

}
