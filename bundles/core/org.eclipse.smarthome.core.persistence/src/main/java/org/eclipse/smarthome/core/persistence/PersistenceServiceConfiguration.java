/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence;

import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.persistence.strategy.SimpleStrategy;

public class PersistenceServiceConfiguration {
    private final List<SimpleItemConfiguration> configs;
    private final List<SimpleStrategy> defaults;
    private final List<SimpleStrategy> strategies;

    public PersistenceServiceConfiguration(final List<SimpleItemConfiguration> configs,
            final List<SimpleStrategy> defaults, final List<SimpleStrategy> strategies) {
        this.configs = Collections.unmodifiableList(configs);
        this.defaults = Collections.unmodifiableList(defaults);
        this.strategies = Collections.unmodifiableList(strategies);
    }

    public List<SimpleItemConfiguration> getConfigs() {
        return configs;
    }

    public List<SimpleStrategy> getDefaults() {
        return defaults;
    }

    public List<SimpleStrategy> getStrategies() {
        return strategies;
    }

}
