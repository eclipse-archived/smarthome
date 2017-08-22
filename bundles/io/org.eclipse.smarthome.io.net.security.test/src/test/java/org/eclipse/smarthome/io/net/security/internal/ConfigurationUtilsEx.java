/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

public class ConfigurationUtilsEx extends ComputeConfigurationDifference {
    boolean writeConfig = true;

    public ConfigurationUtilsEx(@NonNull List<ContextConfiguration> contexts, @NonNull File baseDir,
            @NonNull MessageDigest messageDigest) {
        super(null, contexts, baseDir, messageDigest);
    }

    @Override
    protected void writeConfiguration(ServiceConfiguration serviceConfiguration) throws IOException {
        if (writeConfig) {
            super.writeConfiguration(serviceConfiguration);
        }
    }

    /**
     * Return true if the configuration contains the given context
     *
     * @param context The context name
     */
    public static boolean containsContext(ServiceConfiguration serviceConfiguration, String context) {
        return serviceConfiguration.contexts.stream().anyMatch(c -> context.equals(c.context));
    }

}