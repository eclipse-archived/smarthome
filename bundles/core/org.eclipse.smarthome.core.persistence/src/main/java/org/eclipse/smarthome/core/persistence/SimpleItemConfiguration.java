/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.persistence.config.SimpleConfig;
import org.eclipse.smarthome.core.persistence.strategy.SimpleStrategy;

/**
 * This class holds the configuration of a persistence strategy for specific items.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class SimpleItemConfiguration {

    private final List<SimpleConfig> items;
    private final String alias;
    private final List<SimpleStrategy> strategies;
    private final List<SimpleFilter> filters;

    public SimpleItemConfiguration(final List<SimpleConfig> items, final String alias,
            final List<SimpleStrategy> strategies, final List<SimpleFilter> filters) {
        this.items = items;
        this.alias = alias;
        this.strategies = strategies;
        this.filters = filters;
    }

    public List<SimpleConfig> getItems() {
        return items;
    }

    public String getAlias() {
        return alias;
    }

    public List<SimpleStrategy> getStrategies() {
        return strategies;
    }

    public List<SimpleFilter> getFilters() {
        return filters;
    }

    @Override
    public String toString() {
        return String.format("%s [items=%s, alias=%s, strategies=%s, filters=%s]", getClass().getSimpleName(),
                Arrays.toString(items.toArray()), alias, Arrays.toString(strategies.toArray()),
                Arrays.toString(filters.toArray()));
    }
}
