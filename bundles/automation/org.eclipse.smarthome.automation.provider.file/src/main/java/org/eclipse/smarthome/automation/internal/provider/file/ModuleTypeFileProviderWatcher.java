/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import org.eclipse.smarthome.automation.type.ModuleTypeProvider;

/**
 * This class is a wrapper of {@link ModuleTypeProvider}, responsible for initializing the WatchService.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ModuleTypeFileProviderWatcher extends ModuleTypeFileProvider {

    @Override
    protected void initializeWatchService(String watchingDir) {
        WatchServiceUtil.initializeWatchService(watchingDir, this);
    }

    @Override
    protected void deactivateWatchService(String watchingDir) {
        WatchServiceUtil.deactivateWatchService(watchingDir, this);
    }
}
