/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class isolates the java 1.7 functionality which tracks the file system changes.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
@SuppressWarnings("rawtypes")
public class WatchServiceUtil {

    static Map<AbstractFileProvider, Map<String, AutomationWatchService>> WATCH_SERVICES = new HashMap<AbstractFileProvider, Map<String, AutomationWatchService>>();

    public static void initializeWatchService(String watchingDir, AbstractFileProvider provider) {
        AutomationWatchService aws = null;
        synchronized (WATCH_SERVICES) {
            Map<String, AutomationWatchService> watchers = WATCH_SERVICES.get(provider);
            if (watchers == null) {
                watchers = new HashMap<String, AutomationWatchService>();
                WATCH_SERVICES.put(provider, watchers);
            }
            if (watchers.get(watchingDir) == null) {
                aws = new AutomationWatchService(provider, watchingDir);
                watchers.put(watchingDir, aws);
            }
        }
        if (aws != null) {
            aws.activate();
            provider.importResources(new File(watchingDir));
        }
    }

    public static void deactivateWatchService(String watchingDir, AbstractFileProvider provider) {
        AutomationWatchService aws;
        synchronized (WATCH_SERVICES) {
            Map<String, AutomationWatchService> watchers = WATCH_SERVICES.get(provider);
            aws = watchers.remove(watchingDir);
            if (watchers.size() == 0) {
                WATCH_SERVICES.remove(provider);
            }
        }
        if (aws != null) {
            aws.deactivate();
            provider.removeResources(new File(aws.getSourcePath()));
        }
    }
}
