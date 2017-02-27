/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.ui.internal.actions;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.smarthome.designer.core.config.ConfigurationFolderProvider;
import org.eclipse.smarthome.designer.ui.UIActivator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Action for choosing a configuration folder
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class SelectConfigFolderAction extends Action {

    Viewer viewer;

    public SelectConfigFolderAction(Viewer viewer) {
        this.viewer = viewer;
        setText("Select configuration folder");
        setToolTipText("select a configuration folder");
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
    }

    @Override
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
        dialog.setMessage("Select the configuration folder of the Eclipse SmartHome runtime");
        String selection = dialog.open();
        if (selection != null) {
            try {
                File file = new File(selection);
                if (isValidConfigurationFolder(file)) {
                    ConfigurationFolderProvider.saveFolderToPreferences(selection);
                    ConfigurationFolderProvider.setRootConfigurationFolder(new File(selection));
                    viewer.setInput(ConfigurationFolderProvider.getRootConfigurationFolder());
                } else {
                    MessageDialog.openError(shell, "No valid configuration directory",
                            "The chosen directory is not a valid Eclipse SmartHome configuration"
                                    + " directory. Please choose a different one.");
                }
            } catch (CoreException e) {
                IStatus status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID,
                        "An error occurred while opening the configuration folder", e);
                ErrorDialog.openError(shell, "Cannot open configuration folder!", null, status);
            }
        }
    }

    private boolean isValidConfigurationFolder(File dir) {
        if (dir.isDirectory()) {
            return true;
        }
        return false;
    }
}
