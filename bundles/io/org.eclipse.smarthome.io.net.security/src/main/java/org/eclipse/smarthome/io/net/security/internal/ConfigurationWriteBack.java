/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Provides useful utility methods for the configuration class and keystore files.
 *
 * @author David Graeff - Initial contribution
 *
 */
@NonNullByDefault
public class ConfigurationWriteBack {
    public static void writeConfiguration(ConfigurationAdmin configurationAdmin,
            ServiceConfiguration serviceConfiguration) throws IOException {

        try {
            // Marshal to map and convert to dictionary
            Map<String, Object> data;
            data = Configuration.readFromConfigurationClassInstance(serviceConfiguration);
            Dictionary<String, Object> dict = new Hashtable<>();
            data.forEach((key, value) -> dict.put(key, value));
            // Get ConfigAdmin and update configuration
            org.osgi.service.cm.Configuration config = configurationAdmin
                    .getConfiguration(NetworkServerTlsProvider.CONFIG_PID, null);
            config.update(dict);
        } catch (IllegalAccessException | IOException e) {
            throw new IOException(e);
        }
    }

}
