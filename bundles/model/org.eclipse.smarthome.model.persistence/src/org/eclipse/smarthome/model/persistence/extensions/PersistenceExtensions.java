/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.persistence.extensions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.joda.time.DateTime;
import org.joda.time.base.AbstractInstant;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.LoggerFactory;

/**
 * This class provides static methods that can be used in automation rules
 * for using persistence services
 *
 * @author Thomas.Eichstaedt-Engelen
 * @author Kai Kreuzer - Initial contribution and API
 * @author Chris Jackson
 * @author Gaël L'hopital
 * @author Jan N. Klug
 * @author John Cocula
 *
 */
public class PersistenceExtensions implements ManagedService {

    private static Map<String, PersistenceService> services = new HashMap<String, PersistenceService>();
    private static String defaultService = null;

    public PersistenceExtensions() {
        // default constructor, necessary for osgi-ds
    }

    public void addPersistenceService(PersistenceService service) {
        services.put(service.getName(), service);
    }

    public void removePersistenceService(PersistenceService service) {
        services.remove(service.getName());
    }

    /**
     * Persists the state of a given <code>item</code> through a {@link PersistenceService} identified
     * by the <code>serviceName</code>.
     *
     * @param item the item to store
     * @param serviceName the name of the {@link PersistenceService} to use
     */
    public static void persist(Item item, String serviceName) {
        PersistenceService service = services.get(serviceName);
        if (service != null) {
            service.store(item);
        } else {
            LoggerFactory.getLogger(PersistenceExtensions.class)
                    .warn("There is no persistence service registered with the name '{}'", serviceName);
        }
    }

    /**
     * Persists the state of a given <code>item</code> through the default persistence service.
     *
     * @param item the item to store
     */
    public static void persist(Item item) {
        if (isDefaultServiceAvailable()) {
            persist(item, defaultService);
        }
    }

    /**
     * Retrieves the historic item for a given <code>item</code> at a certain point in time through the default
     * persistence service.
     *
     * @param item the item for which to retrieve the historic item
     * @param timestamp the point in time for which the historic item should be retrieved
     * @return the historic item at the given point in time, or <code>null</code> if no historic item could be found,
     *         the default persistence service is not available or does not refer to a
     *         {@link QueryablePersistenceService}
     */
    public static HistoricItem historicState(Item item, AbstractInstant timestamp) {
        if (isDefaultServiceAvailable()) {
            return historicState(item, timestamp, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Retrieves the historic item for a given <code>item</code> at a certain point in time through a
     * {@link PersistenceService} identified by the <code>serviceName</code>.
     *
     * @param item the item for which to retrieve the historic item
     * @param timestamp the point in time for which the historic item should be retrieved
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return the historic item at the given point in time, or <code>null</code> if no historic item could be found or
     *         if the provided <code>serviceName</code> does not refer to an available
     *         {@link QueryablePersistenceService}
     */
    public static HistoricItem historicState(Item item, AbstractInstant timestamp, String serviceName) {
        PersistenceService service = services.get(serviceName);
        if (service instanceof QueryablePersistenceService) {
            QueryablePersistenceService qService = (QueryablePersistenceService) service;
            FilterCriteria filter = new FilterCriteria();
            filter.setEndDate(timestamp.toDate());
            filter.setItemName(item.getName());
            filter.setPageSize(1);
            filter.setOrdering(Ordering.DESCENDING);
            Iterable<HistoricItem> result = qService.query(filter);
            if (result.iterator().hasNext()) {
                return result.iterator().next();
            } else {
                return null;
            }
        } else {
            LoggerFactory.getLogger(PersistenceExtensions.class)
                    .warn("There is no queryable persistence service registered with the name '{}'", serviceName);
            return null;
        }
    }

    /**
     * Checks if the state of a given <code>item</code> has changed since a certain point in time.
     * The default persistence service is used.
     *
     * @param item the item to check for state changes
     * @param timestamp the point in time to start the check
     * @return <code>true</code> if item state had changed, <code>false</code> if it hasn't or if the default
     *         persistence service does not refer to a {@link QueryablePersistenceService}, or <code>null</code> if the
     *         default persistence service is not available
     */
    public static Boolean changedSince(Item item, AbstractInstant timestamp) {
        if (isDefaultServiceAvailable()) {
            return changedSince(item, timestamp, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Checks if the state of a given <code>item</code> has changed since a certain point in time.
     * The {@link PersistenceService} identified by the <code>serviceName</code> is used.
     *
     * @param item the item to check for state changes
     * @param timestamp the point in time to start the check
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return <code>true</code> if item state has changed, or <code>false</code> if it hasn't or if the given
     *         <code>serviceName</code> does not refer to an available {@link QueryablePersistenceService}
     */
    public static Boolean changedSince(Item item, AbstractInstant timestamp, String serviceName) {
        Iterable<HistoricItem> result = getAllStatesSince(item, timestamp, serviceName);
        Iterator<HistoricItem> it = result.iterator();
        HistoricItem itemThen = historicState(item, timestamp);
        if (itemThen == null) {
            // Can't get the state at the start time
            // If we've got results more recent that this, it must have changed
            return it.hasNext();
        }

        State state = itemThen.getState();
        while (it.hasNext()) {
            HistoricItem hItem = it.next();
            if (state != null && !hItem.getState().equals(state)) {
                return true;
            }
            state = hItem.getState();
        }
        return false;
    }

    /**
     * Checks if the state of a given <code>item</code> has been updated since a certain point in time.
     * The default persistence service is used.
     *
     * @param item the item to check for state updates
     * @param timestamp the point in time to start the check
     * @return <code>true</code> if item state was updated, <code>false</code> if either item has not been updated since
     *         <code>timestamp</code> or if the default persistence does not refer to a
     *         {@link QueryablePersistenceService}, or <code>null</code> if the default persistence service is not
     *         available
     */
    public static Boolean updatedSince(Item item, AbstractInstant timestamp) {
        if (isDefaultServiceAvailable()) {
            return updatedSince(item, timestamp, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Checks if the state of a given <code>item</code> has changed since a certain point in time.
     * The {@link PersistenceService} identified by the <code>serviceName</code> is used.
     *
     * @param item the item to check for state changes
     * @param timestamp the point in time to start the check
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return <code>true</code> if item state was updated or <code>false</code> if either the item has not been updated
     *         since <code>timestamp</code> or if the given <code>serviceName</code> does not refer to a
     *         {@link QueryablePersistenceService}
     */
    public static Boolean updatedSince(Item item, AbstractInstant timestamp, String serviceName) {
        Iterable<HistoricItem> result = getAllStatesSince(item, timestamp, serviceName);
        if (result.iterator().hasNext()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the historic item with the maximum value of the state of a given <code>item</code> since
     * a certain point in time. The default persistence service is used.
     *
     * @param item the item to get the maximum state value for
     * @param timestamp the point in time to start the check
     * @return a historic item with the maximum state value since the given point in time, or <code>null</code> if the
     *         default persistence service is not available, or a {@link HistoricItem} constructed from the
     *         <code>item</code> if the default persistence service does not refer to a
     *         {@link QueryablePersistenceService}
     */
    public static HistoricItem maximumSince(Item item, AbstractInstant timestamp) {
        if (isDefaultServiceAvailable()) {
            return maximumSince(item, timestamp, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Gets the historic item with the maximum value of the state of a given <code>item</code> since
     * a certain point in time. The {@link PersistenceService} identified by the <code>serviceName</code> is used.
     *
     * @param item the item to get the maximum state value for
     * @param timestamp the point in time to start the check
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return a {@link HistoricItem} with the maximum state value since the given point in time, or a
     *         {@link HistoricItem} constructed from the <code>item</code>'s state if <code>item</code>'s state is the
     *         maximum value or if the given <code>serviceName</code> does not refer to an available
     *         {@link QueryablePersistenceService}
     */
    public static HistoricItem maximumSince(final Item item, AbstractInstant timestamp, String serviceName) {
        Iterable<HistoricItem> result = getAllStatesSince(item, timestamp, serviceName);
        Iterator<HistoricItem> it = result.iterator();
        HistoricItem maximumHistoricItem = null;
        DecimalType maximum = (DecimalType) item.getStateAs(DecimalType.class);
        while (it.hasNext()) {
            HistoricItem historicItem = it.next();
            State state = historicItem.getState();
            if (state instanceof DecimalType) {
                DecimalType value = (DecimalType) state;
                if (maximum == null || value.compareTo(maximum) > 0) {
                    maximum = value;
                    maximumHistoricItem = historicItem;
                }
            }
        }
        if (maximumHistoricItem == null && maximum != null) {
            // the maximum state is the current one, so construct a historic item on the fly
            final DecimalType state = maximum;
            return new HistoricItem() {

                @Override
                public Date getTimestamp() {
                    return Calendar.getInstance().getTime();
                }

                @Override
                public State getState() {
                    return state;
                }

                @Override
                public String getName() {
                    return item.getName();
                }
            };
        } else {
            return maximumHistoricItem;
        }
    }

    /**
     * Gets the historic item with the minimum value of the state of a given <code>item</code> since
     * a certain point in time. The default persistence service is used.
     *
     * @param item the item to get the minimum state value for
     * @param timestamp the point in time from which to search for the minimum state value
     * @return the historic item with the minimum state value since the given point in time, <code>null</code> if the
     *         default persistence service is not available, or a {@link HistoricItem} constructed from the
     *         <code>item</code>'s state if <code>item</code>'s state is the minimum value or if the default persistence
     *         service does not refer to an available {@link QueryablePersistenceService}
     */
    public static HistoricItem minimumSince(Item item, AbstractInstant timestamp) {
        if (isDefaultServiceAvailable()) {
            return minimumSince(item, timestamp, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Gets the historic item with the minimum value of the state of a given <code>item</code> since
     * a certain point in time. The {@link PersistenceService} identified by the <code>serviceName</code> is used.
     *
     * @param item the item to get the minimum state value for
     * @param timestamp the point in time from which to search for the minimum state value
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return the historic item with the minimum state value since the given point in time, or a {@link HistoricItem}
     *         constructed from the <code>item</code>'s state if <code>item</code>'s state is the minimum value or if
     *         the given <code>serviceName</code> does not refer to an available {@link QueryablePersistenceService}
     */
    public static HistoricItem minimumSince(final Item item, AbstractInstant timestamp, String serviceName) {
        Iterable<HistoricItem> result = getAllStatesSince(item, timestamp, serviceName);
        Iterator<HistoricItem> it = result.iterator();
        HistoricItem minimumHistoricItem = null;
        DecimalType minimum = (DecimalType) item.getStateAs(DecimalType.class);
        while (it.hasNext()) {
            HistoricItem historicItem = it.next();
            State state = historicItem.getState();
            if (state instanceof DecimalType) {
                DecimalType value = (DecimalType) state;
                if (minimum == null || value.compareTo(minimum) < 0) {
                    minimum = value;
                    minimumHistoricItem = historicItem;
                }
            }
        }
        if (minimumHistoricItem == null && minimum != null) {
            // the minimal state is the current one, so construct a historic item on the fly
            final DecimalType state = minimum;
            return new HistoricItem() {

                @Override
                public Date getTimestamp() {
                    return Calendar.getInstance().getTime();
                }

                @Override
                public State getState() {
                    return state;
                }

                @Override
                public String getName() {
                    return item.getName();
                }
            };
        } else {
            return minimumHistoricItem;
        }
    }

    /**
     * Gets the average value of the state of a given <code>item</code> since a certain point in time.
     * The default persistence service is used.
     *
     * @param item the item to get the average state value for
     * @param timestamp the point in time from which to search for the average state value
     * @return the average state values since <code>timestamp</code>, <code>null</code> if the default persistence
     *         service is not available, or the state of the given <code>item</code> if no previous states could be
     *         found or if the default persistence service does not refer to an available
     *         {@link QueryablePersistenceService}
     */
    public static DecimalType averageSince(Item item, AbstractInstant timestamp) {
        if (isDefaultServiceAvailable()) {
            return averageSince(item, timestamp, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Gets the average value of the state of a given <code>item</code> since a certain point in time.
     * The {@link PersistenceService} identified by the <code>serviceName</code> is used.
     *
     * @param item the item to get the average state value for
     * @param timestamp the point in time from which to search for the average state value
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return the average state values since <code>timestamp</code>, or the state of the given <code>item</code> if no
     *         previous states could be found or if the persistence service given by <code>serviceName</code> does not
     *         refer to an available {@link QueryablePersistenceService}
     */
    public static DecimalType averageSince(Item item, AbstractInstant timestamp, String serviceName) {
        Iterable<HistoricItem> result = getAllStatesSince(item, timestamp, serviceName);
        Iterator<HistoricItem> it = result.iterator();

        DecimalType value = (DecimalType) item.getStateAs(DecimalType.class);
        if (value == null) {
            value = DecimalType.ZERO;
        }

        BigDecimal total = value.toBigDecimal();
        int quantity = 1;
        while (it.hasNext()) {
            State state = it.next().getState();
            if (state instanceof DecimalType) {
                value = (DecimalType) state;
                total = total.add(value.toBigDecimal());
                quantity++;
            }
        }
        BigDecimal average = total.divide(BigDecimal.valueOf(quantity), MathContext.DECIMAL64);

        return new DecimalType(average);
    }

    /**
     * Gets the sum of the state of a given <code>item</code> since a certain point in time.
     * The default persistence service is used.
     *
     * @param item the item for which we will sum its persisted state values since <code>timestamp</code>
     * @param timestamp the point in time from which to start the summation
     * @return the sum of the state values since <code>timestamp</code>, <code>null</code> if the default persistence
     *         service is not available, or {@link DecimalType.ZERO} if no historic states could be found or if the
     *         default persistence service does not refer to a {@link QueryablePersistenceService}
     */
    public static DecimalType sumSince(Item item, AbstractInstant timestamp) {
        if (isDefaultServiceAvailable()) {
            return sumSince(item, timestamp, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Gets the sum of the state of a given <code>item</code> since a certain point in time.
     * The {@link PersistenceService} identified by the <code>serviceName</code> is used.
     *
     * @param item the item for which we will sum its persisted state values since <code>timestamp</code>
     * @param timestamp the point in time from which to start the summation
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return the sum of the state values since the given point in time, or {@link DecimalType.ZERO} if no historic
     *         states could be found for the <code>item</code> or if <code>serviceName</code> does no refer to a
     *         {@link QueryablePersistenceService}
     */
    public static DecimalType sumSince(Item item, AbstractInstant timestamp, String serviceName) {
        Iterable<HistoricItem> result = getAllStatesSince(item, timestamp, serviceName);
        Iterator<HistoricItem> it = result.iterator();

        BigDecimal sum = BigDecimal.ZERO;
        while (it.hasNext()) {
            State state = it.next().getState();
            if (state instanceof DecimalType) {
                sum = sum.add(((DecimalType) state).toBigDecimal());
            }
        }

        return new DecimalType(sum);
    }

    private static Iterable<HistoricItem> getAllStatesSince(Item item, AbstractInstant timestamp, String serviceName) {
        PersistenceService service = services.get(serviceName);
        if (service instanceof QueryablePersistenceService) {
            QueryablePersistenceService qService = (QueryablePersistenceService) service;
            FilterCriteria filter = new FilterCriteria();
            filter.setBeginDate(timestamp.toDate());
            filter.setItemName(item.getName());
            filter.setOrdering(Ordering.ASCENDING);
            return qService.query(filter);
        } else {
            LoggerFactory.getLogger(PersistenceExtensions.class)
                    .warn("There is no queryable persistence service registered with the name '{}'", serviceName);
            return Collections.emptySet();
        }
    }

    /**
     * Query the last update time of a given <code>item</code>. The default persistence service is used.
     *
     * @param item the item for which the last update time is to be returned
     * @return point in time of the last update to <code>item</code>, or <code>null</code> if there are no previously
     *         persisted updates or the default persistence service is not available or a
     *         {@link QueryablePersistenceService}
     */
    public static AbstractInstant lastUpdate(Item item) {
        if (isDefaultServiceAvailable()) {
            return lastUpdate(item, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Query for the last update time of a given <code>item</code>.
     *
     * @param item the item for which the last update time is to be returned
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return last time <code>item</code> was updated, or <code>null</code> if there are no previously
     *         persisted updates or if persistence service given by <code>serviceName</code> does not refer to an
     *         available {@link QueryablePersistenceService}
     */
    public static AbstractInstant lastUpdate(Item item, String serviceName) {
        PersistenceService service = services.get(serviceName);
        if (service instanceof QueryablePersistenceService) {
            QueryablePersistenceService qService = (QueryablePersistenceService) service;
            FilterCriteria filter = new FilterCriteria();
            filter.setItemName(item.getName());
            filter.setOrdering(Ordering.DESCENDING);
            filter.setPageSize(1);
            Iterable<HistoricItem> result = qService.query(filter);
            if (result.iterator().hasNext()) {
                return new DateTime(result.iterator().next().getTimestamp());
            } else {
                return null;
            }
        } else {
            LoggerFactory.getLogger(PersistenceExtensions.class)
                    .warn("There is no queryable persistence service registered with the name '{}'", serviceName);
            return null;
        }
    }

    /**
     * Gets the difference value of the state of a given <code>item</code> since a certain point in time.
     * The default persistence service is used.
     *
     * @param item the item to get the average state value for
     * @param timestamp the point in time from which to compute the delta
     * @return the difference between now and then, or <code>null</code> if there is no default persistence
     *         service available, the default persistence service is not a {@link QueryablePersistenceService}, or if
     *         there is no persisted state for the given <code>item</code> at the given <code>timestamp</code> available
     *         in the default persistence service
     */
    public static DecimalType deltaSince(Item item, AbstractInstant timestamp) {
        if (isDefaultServiceAvailable()) {
            return deltaSince(item, timestamp, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Gets the difference value of the state of a given <code>item</code> since a certain point in time.
     * The {@link PersistenceService} identified by the <code>serviceName</code> is used.
     *
     * @param item the item to get the average state value for
     * @param timestamp the point in time from which to compute the delta
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return the difference between now and then, or <code>null</code> if the given serviceName does not refer to an
     *         available {@link QueryablePersistenceService}, or if there is no persisted state for the given
     *         <code>item</code> at the given <code>timestamp</code> using the persistence service named
     *         <code>serviceName</code>
     */
    public static DecimalType deltaSince(Item item, AbstractInstant timestamp, String serviceName) {
        HistoricItem itemThen = historicState(item, timestamp, serviceName);
        if (itemThen != null) {
            DecimalType valueThen = (DecimalType) itemThen.getState();
            DecimalType valueNow = (DecimalType) item.getStateAs(DecimalType.class);

            if ((valueThen != null) && (valueNow != null)) {
                return new DecimalType(valueNow.toBigDecimal().subtract(valueThen.toBigDecimal()));
            }
        }
        return null;
    }

    /**
     * Gets the evolution rate of the state of a given <code>item</code> since a certain point in time.
     * The {@link PersistenceService} identified by the <code>serviceName</code> is used.
     *
     * @param item the item to get the evolution rate value for
     * @param timestamp the point in time from which to compute the evolution rate
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return the evolution rate in percent (positive and negative) between now and then, or <code>null</code> if
     *         there is no default persistence service available, the default persistence service is not a
     *         {@link QueryablePersistenceService}, or if there is no persisted state for the given <code>item</code> at
     *         the given <code>timestamp</code>, or if there is a state but it is zero (which would cause a
     *         divide-by-zero error)
     */
    public static DecimalType evolutionRate(Item item, AbstractInstant timestamp) {
        if (isDefaultServiceAvailable()) {
            return evolutionRate(item, timestamp, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Gets the evolution rate of the state of a given <code>item</code> since a certain point in time.
     * The {@link PersistenceService} identified by the <code>serviceName</code> is used.
     *
     * @param item the item to get the evolution rate value for
     * @param timestamp the point in time from which to compute the evolution rate
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return the evolution rate in percent (positive and negative) between now and then, or <code>null</code> if
     *         the persistence service given by serviceName is not available or is not a
     *         {@link QueryablePersistenceService}, or if there is no persisted state for the given
     *         <code>item</code> at the given <code>timestamp</code> using the persistence service given by
     *         <code>serviceName</code>, or if there is a state but it is zero (which would cause a divide-by-zero
     *         error)
     */
    public static DecimalType evolutionRate(Item item, AbstractInstant timestamp, String serviceName) {
        HistoricItem itemThen = historicState(item, timestamp, serviceName);
        if (itemThen != null) {
            DecimalType valueThen = (DecimalType) itemThen.getState();
            DecimalType valueNow = (DecimalType) item.getStateAs(DecimalType.class);

            if ((valueThen != null) && (valueThen.toBigDecimal().compareTo(BigDecimal.ZERO) != 0)
                    && (valueNow != null)) {
                // ((now - then) / then) * 100
                return new DecimalType(valueNow.toBigDecimal().subtract(valueThen.toBigDecimal())
                        .divide(valueThen.toBigDecimal(), MathContext.DECIMAL64).movePointRight(2));
            }
        }
        return null;
    }

    /**
     * Returns the previous state of a given <code>item</code>.
     *
     * @param item the item to get the previous state value for
     * @return the previous state or <code>null</code> if no previous state could be found, or if the default
     *         persistence service is not configured or does not refer to a {@link QueryablePersistenceService}
     */
    public static HistoricItem previousState(Item item) {
        return previousState(item, false);
    }

    /**
     * Returns the previous state of a given <code>item</code>.
     *
     * @param item the item to get the previous state value for
     * @param skipEqual if true, skips equal state values and searches the first state not equal the current state
     * @return the previous state or <code>null</code> if no previous state could be found, or if the default
     *         persistence service is not configured or does not refer to a {@link QueryablePersistenceService}
     */
    public static HistoricItem previousState(Item item, boolean skipEqual) {
        if (isDefaultServiceAvailable()) {
            return previousState(item, skipEqual, defaultService);
        } else {
            return null;
        }
    }

    /**
     * Returns the previous state of a given <code>item</code>.
     * The {@link PersistenceService} identified by the <code>serviceName</code> is used.
     *
     * @param item the item to get the previous state value for
     * @param skipEqual if <code>true</code>, skips equal state values and searches the first state not equal the
     *            current state
     * @param serviceName the name of the {@link PersistenceService} to use
     * @return the previous state or <code>null</code> if no previous state could be found, or if the given
     *         <code>serviceName</code> is not available or does not refer to a {@link QueryablePersistenceService}
     */
    public static HistoricItem previousState(Item item, boolean skipEqual, String serviceName) {
        PersistenceService service = services.get(serviceName);
        if (service instanceof QueryablePersistenceService) {
            QueryablePersistenceService qService = (QueryablePersistenceService) service;
            FilterCriteria filter = new FilterCriteria();
            filter.setItemName(item.getName());
            filter.setOrdering(Ordering.DESCENDING);

            filter.setPageSize(skipEqual ? 1000 : 1);
            int startPage = 0;
            filter.setPageNumber(startPage);

            Iterable<HistoricItem> items = qService.query(filter);
            while (items != null) {
                Iterator<HistoricItem> itemIterator = items.iterator();
                int itemCount = 0;
                while (itemIterator.hasNext()) {
                    HistoricItem historicItem = itemIterator.next();
                    itemCount++;
                    if (!skipEqual || (skipEqual && !historicItem.getState().equals(item.getState()))) {
                        return historicItem;
                    }
                }
                if (itemCount == filter.getPageSize()) {
                    filter.setPageNumber(++startPage);
                    items = qService.query(filter);
                } else {
                    items = null;
                }
            }
            return null;

        } else {
            LoggerFactory.getLogger(PersistenceExtensions.class)
                    .warn("There is no queryable persistence service registered with the name '{}'", serviceName);
            return null;
        }
    }

    /**
     * Returns <code>true</code> if a default service is configured and returns <code>false</code> and logs a warning
     * otherwise.
     *
     * @return true if a default service is available
     */
    private static boolean isDefaultServiceAvailable() {
        if (defaultService != null) {
            return true;
        } else {
            LoggerFactory.getLogger(PersistenceExtensions.class)
                    .warn("No default persistence service is configured in the configuration file!");
            return false;
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void updated(Dictionary config) throws ConfigurationException {
        if (config != null) {
            PersistenceExtensions.defaultService = (String) config.get("default");
        }
    }

}
