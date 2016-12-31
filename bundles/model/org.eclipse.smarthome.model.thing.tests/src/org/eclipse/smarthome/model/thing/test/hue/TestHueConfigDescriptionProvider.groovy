/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.test.hue

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type

/**
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
class TestHueConfigDescriptionProvider implements ConfigDescriptionProvider {

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        return null
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        if (uri.equals(new URI("hue:LCT001:color"))) {
            ConfigDescriptionParameter configDescriptionParameter = [
                getName: "defaultConfig",
                getType: Type.TEXT,
                getDefault: "defaultValue"
            ] as ConfigDescriptionParameter
            return new ConfigDescription(uri, Collections.singletonList(configDescriptionParameter))
        }
        return null
    }
}
