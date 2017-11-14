/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.persistence.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.types.State;

/**
 * A simple persistence service used for unit tests
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class TestPersistenceService implements QueryablePersistenceService {

    @Override
    public String getId() {
        return "test";
    }

    @Override
    public void store(Item item) {
    }

    @Override
    public void store(Item item, String alias) {
    }

    @SuppressWarnings("deprecation")
    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        int startValue = 1950;
        int endValue = 2012;

        if (filter.getBeginDate() != null) {
            startValue = filter.getBeginDate().getYear() + 1900;
        }
        if (filter.getEndDate() != null) {
            endValue = filter.getEndDate().getYear() + 1900;
        }

        if (endValue <= startValue || startValue < 1950) {
            return Collections.emptyList();
        }

        ArrayList<HistoricItem> results = new ArrayList<HistoricItem>(endValue - startValue);
        for (int i = startValue; i <= endValue; i++) {
            final int year = i;
            results.add(new HistoricItem() {
                @Override
                public Date getTimestamp() {
                    return new Date(year - 1900, 0, 1);
                }

                @Override
                public State getState() {
                    return new DecimalType(year);
                }

                @Override
                public String getName() {
                    return "Test";
                }
            });
        }
        if (filter.getOrdering() == Ordering.DESCENDING) {
            Collections.reverse(results);
        }
        return results;
    }

    @Override
    public @NonNull Set<@NonNull PersistenceItemInfo> getItemInfo() {
        return Collections.emptySet();
    }

    @Override
    public String getLabel(Locale locale) {
        return "Test Label";
    }

}
