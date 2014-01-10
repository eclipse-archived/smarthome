/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschränkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.ui.internal.views;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.smarthome.designer.core.config.ConfigurationFolderProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;

public class ConfigNavigator extends CommonNavigator {

	private IResourceChangeListener changeListener;
	
	@Override
	protected Object getInitialInput() {
		changeListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						getCommonViewer().refresh();
					}
				});
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(changeListener);
		
		try {
			return ConfigurationFolderProvider.getRootConfigurationFolder().getProject();
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	protected ActionGroup createCommonActionGroup() {
		return new ConfigNavigatorActionGroup(getCommonViewer());
	}

	@Override
	public void dispose() {
		super.dispose();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(changeListener);
		changeListener = null;
	}
}
