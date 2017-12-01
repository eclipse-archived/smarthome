/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.util.Dictionary;
import java.util.Map;

import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationPlugin;
import org.osgi.service.component.annotations.Component;

/**
 * Intercepts configurations of {@link ConfigurationAdmin} and
 * converts between a flat map of <key,value> tuples to a nested
 * map before passing these to the destination services.
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = { ConfigurationPlugin.class })
public class NestedConfigConverterService implements ConfigurationPlugin {
    @Override
    public void modifyConfiguration(ServiceReference<?> reference, Dictionary<String, Object> properties) {
        if (!(properties instanceof Map)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> c = (Map<String, Object>) properties;
        NestedConfigConverter.mapToNested(c);
    }
}
