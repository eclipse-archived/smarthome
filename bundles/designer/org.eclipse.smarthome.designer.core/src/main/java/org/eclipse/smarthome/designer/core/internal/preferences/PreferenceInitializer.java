/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.core.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.designer.core.CoreActivator;
import org.eclipse.smarthome.designer.core.DesignerCoreConstants;

/**
 * This class initializes the preference setting for the configuration folder.
 * If no other preference has been set yet, the default defined in the config.core bundle
 * will be used.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public PreferenceInitializer() {
    }

    @Override
    public void initializeDefaultPreferences() {
        IScopeContext context = DefaultScope.INSTANCE;
        IEclipsePreferences node = context.getNode(CoreActivator.getDefault().getBundle().getSymbolicName());
        String folderPath = ConfigConstants.DEFAULT_CONFIG_FOLDER;
        node.put(DesignerCoreConstants.CONFIG_FOLDER_PREFERENCE, folderPath);
    }

}
