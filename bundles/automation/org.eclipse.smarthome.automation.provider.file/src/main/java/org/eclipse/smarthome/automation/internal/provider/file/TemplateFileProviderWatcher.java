/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import org.eclipse.smarthome.automation.template.TemplateProvider;

/**
 * This class is a wrapper of multiple {@link TemplateProvider}s, responsible for initializing the WatchService.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class TemplateFileProviderWatcher extends TemplateFileProvider {

    @Override
    protected void initializeWatchService(String watchingDir) {
        WatchServiceUtil.initializeWatchService(watchingDir, this);
    }

    @Override
    protected void deactivateWatchService(String watchingDir) {
        WatchServiceUtil.deactivateWatchService(watchingDir, this);
    }

}
