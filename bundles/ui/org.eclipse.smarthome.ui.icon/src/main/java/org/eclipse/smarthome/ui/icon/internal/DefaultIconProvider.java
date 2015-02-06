/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.icon.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.ui.icon.IconProvider;

/**
 * The default icon provider reads the png icons delivered with the system in
 * the folder runtime/icons and also supports custom icons in the configurations/icons
 * folder. If a custom icon is found, it will be used over the standard system icon.
 * 
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class DefaultIconProvider implements IconProvider {

    @Override
    public boolean hasIcon(String iconName) {
        File file = getIconFile(iconName);
        return file != null;
    }

    @Override
    public InputStream getIcon(String iconName) {
        File file = getIconFile(iconName);
        if (file != null) {
            try {
                FileInputStream is = new FileInputStream(file);
                return is;
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    private File getIconFile(String iconName) {
        File folder = new File(ConfigConstants.getConfigFolder() + File.separator + "icons");
        File file = new File(folder, iconName + ".png");
        if (file.exists()) {
            return file;
        } else {
            folder = new File("runtime" + File.separator + "icons");
            file = new File(folder, iconName + ".png");

            if (file.exists()) {
                return file;
            }
        }
        return null;
    }
}
