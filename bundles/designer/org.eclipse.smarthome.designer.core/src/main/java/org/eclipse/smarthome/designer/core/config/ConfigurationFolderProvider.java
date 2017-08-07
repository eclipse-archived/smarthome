/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.core.config;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.smarthome.designer.core.CoreActivator;
import org.eclipse.smarthome.designer.core.DesignerCoreConstants;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class ConfigurationFolderProvider {

    private static IFolder folder;

    private static IProjectCreator projectCreator = new PluginProjectCreator();

    static public synchronized IFolder getRootConfigurationFolder() throws CoreException {
        if (folder == null) {
            IProject project = projectCreator.createProject("config");

            File configFolder = getFolderFromPreferences();
            if (configFolder != null) {
                folder = project.getFolder("config");
                folder.createLink(configFolder.toURI(), IResource.BACKGROUND_REFRESH | IResource.REPLACE, null);
                CoreActivator.setConfigFolder(configFolder.getAbsolutePath());
            }
        }
        return folder;
    }

    static public synchronized void setRootConfigurationFolder(final File configFolder) throws CoreException {
        CoreActivator.setConfigFolder(configFolder.getAbsolutePath());

        try {
            CoreActivator.updateFolderObserver();
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, e.getMessage()));
        }

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject project = projectCreator.createProject("config");

                if (configFolder != null) {
                    folder = project.getFolder("config");
                    if (folder.exists()) {
                        folder.delete(true, null);
                    }
                    folder.createLink(configFolder.toURI(), IResource.ALLOW_MISSING_LOCAL, null);
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);
    }

    private static File getFolderFromPreferences() {
        IPreferencesService service = Platform.getPreferencesService();
        Preferences node = service.getRootNode().node(ConfigurationScope.SCOPE).node(CoreActivator.PLUGIN_ID);
        if (node != null) {
            String folderPath = node.get(DesignerCoreConstants.CONFIG_FOLDER_PREFERENCE, null);
            if (folderPath != null) {
                File file = new File(folderPath);
                if (file != null && file.isDirectory()) {
                    return file;
                } else {
                    LoggerFactory.getLogger(ConfigurationFolderProvider.class).warn("'{}' is no valid directory.",
                            folderPath);
                }
            }
        }
        return null;
    }

    public static void saveFolderToPreferences(String folderPath) {
        IPreferencesService service = Platform.getPreferencesService();
        Preferences node = service.getRootNode().node(ConfigurationScope.SCOPE).node(CoreActivator.PLUGIN_ID);
        try {
            if (node != null) {
                node.put(DesignerCoreConstants.CONFIG_FOLDER_PREFERENCE, folderPath);
                node.flush();
                return;
            }
        } catch (BackingStoreException e) {
        }
        LoggerFactory.getLogger(ConfigurationFolderProvider.class).warn("Could not save folder '{}' to preferences.",
                folderPath);
    }
}
