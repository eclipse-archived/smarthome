/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence.internal;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceManager;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.PersistenceServiceConfiguration;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.persistence.SimpleItemConfiguration;
import org.eclipse.smarthome.core.persistence.config.SimpleAllConfig;
import org.eclipse.smarthome.core.persistence.config.SimpleConfig;
import org.eclipse.smarthome.core.persistence.config.SimpleGroupConfig;
import org.eclipse.smarthome.core.persistence.config.SimpleItemConfig;
import org.eclipse.smarthome.core.persistence.strategy.SimpleCronStrategy;
import org.eclipse.smarthome.core.persistence.strategy.SimpleStrategy;
import org.eclipse.smarthome.core.scheduler.CronExpression;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a persistence manager to manage all persistence services etc.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Markus Rathgeb - Separation of persistence core and model, drop Quartz usage.
 */
public class PersistenceManagerImpl implements PersistenceManager, ItemRegistryChangeListener, StateChangeListener {

    private final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);

    // the scheduler used for timer events
    private ExpressionThreadPoolExecutor scheduler;

    private ItemRegistry itemRegistry;

    final Map<String, PersistenceService> persistenceServices = new HashMap<>();
    final Map<String, PersistenceServiceConfiguration> persistenceServiceConfigs = new HashMap<>();
    private final Map<String, Set<Runnable>> persistenceJobs = new HashMap<>();

    public PersistenceManagerImpl() {
    }

    protected void activate() {
        scheduler = ExpressionThreadPoolManager.getExpressionScheduledPool("persist");
    }

    protected void deactivate() {
        scheduler.shutdown();
        scheduler = null;
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        itemRegistry.addRegistryChangeListener(this);
        allItemsChanged(null);
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        itemRegistry.removeRegistryChangeListener(this);
        this.itemRegistry = null;
    }

    protected void addPersistenceService(PersistenceService persistenceService) {
        logger.debug("Initializing {} persistence service.", persistenceService.getId());
        persistenceServices.put(persistenceService.getId(), persistenceService);
        stopEventHandling(persistenceService.getId());
        startEventHandling(persistenceService.getId());
    }

    protected void removePersistenceService(PersistenceService persistenceService) {
        stopEventHandling(persistenceService.getId());
        persistenceServices.remove(persistenceService.getId());
    }

    /**
     * Calls all persistence services which use change or update policy for the given item
     *
     * @param item the item to persist
     * @param onlyChanges true, if it has the change strategy, false otherwise
     */
    private void handleStateEvent(Item item, boolean onlyChanges) {
        synchronized (persistenceServiceConfigs) {
            for (Entry<String, PersistenceServiceConfiguration> entry : persistenceServiceConfigs.entrySet()) {
                final String serviceName = entry.getKey();
                final PersistenceServiceConfiguration config = entry.getValue();
                if (persistenceServices.containsKey(serviceName)) {
                    for (SimpleItemConfiguration itemConfig : config.getConfigs()) {
                        if (hasStrategy(serviceName, itemConfig,
                                onlyChanges ? SimpleStrategy.Globals.CHANGE : SimpleStrategy.Globals.UPDATE)) {
                            if (appliesToItem(itemConfig, item)) {
                                persistenceServices.get(serviceName).store(item, itemConfig.getAlias());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if a given persistence configuration entry has a certain strategy for the given service
     *
     * @param serviceName the service to check the configuration for
     * @param itemConfig the persistence configuration entry
     * @param strategy the strategy to check for
     * @return true, if it has the given strategy
     */
    private boolean hasStrategy(String serviceName, SimpleItemConfiguration itemConfig, SimpleStrategy strategy) {
        final PersistenceServiceConfiguration config = persistenceServiceConfigs.get(serviceName);
        if (config.getDefaults().contains(strategy) && itemConfig.getStrategies().isEmpty()) {
            return true;
        } else {
            for (SimpleStrategy s : itemConfig.getStrategies()) {
                if (s.equals(strategy)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Checks if a given persistence configuration entry is relevant for an item
     *
     * @param config the persistence configuration entry
     * @param item to check if the configuration applies to
     * @return true, if the configuration applies to the item
     */
    private boolean appliesToItem(SimpleItemConfiguration config, Item item) {
        for (SimpleConfig itemCfg : config.getItems()) {
            if (itemCfg instanceof SimpleAllConfig) {
                return true;
            }
            if (itemCfg instanceof SimpleItemConfig) {
                SimpleItemConfig singleItemConfig = (SimpleItemConfig) itemCfg;
                if (item.getName().equals(singleItemConfig.getItem())) {
                    return true;
                }
            }
            if (itemCfg instanceof SimpleGroupConfig) {
                SimpleGroupConfig groupItemCfg = (SimpleGroupConfig) itemCfg;
                String groupName = groupItemCfg.getGroup();
                try {
                    Item gItem = itemRegistry.getItem(groupName);
                    if (gItem instanceof GroupItem) {
                        GroupItem groupItem = (GroupItem) gItem;
                        if (groupItem.getAllMembers().contains(item)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return false;
    }

    /**
     * Retrieves all items for which the persistence configuration applies to.
     *
     * @param config the persistence configuration entry
     * @return all items that this configuration applies to
     */
    Iterable<Item> getAllItems(SimpleItemConfiguration config) {
        // first check, if we should return them all
        for (Object itemCfg : config.getItems()) {
            if (itemCfg instanceof SimpleAllConfig) {
                return itemRegistry.getItems();
            }
        }

        // otherwise, go through the detailed definitions
        Set<Item> items = new HashSet<Item>();
        for (Object itemCfg : config.getItems()) {
            if (itemCfg instanceof SimpleItemConfig) {
                SimpleItemConfig singleItemConfig = (SimpleItemConfig) itemCfg;
                try {
                    Item item = itemRegistry.getItem(singleItemConfig.getItem());
                    items.add(item);
                } catch (ItemNotFoundException e) {
                    logger.debug("Item '{}' does not exist.", singleItemConfig.getItem());
                }
            }
            if (itemCfg instanceof SimpleGroupConfig) {
                SimpleGroupConfig groupItemCfg = (SimpleGroupConfig) itemCfg;
                String groupName = groupItemCfg.getGroup();
                try {
                    Item gItem = itemRegistry.getItem(groupName);
                    if (gItem instanceof GroupItem) {
                        GroupItem groupItem = (GroupItem) gItem;
                        items.addAll(groupItem.getAllMembers());
                    }
                } catch (ItemNotFoundException e) {
                    logger.debug("Item group '{}' does not exist.", groupName);
                }
            }
        }
        return items;
    }

    /**
     * Handles the "restoreOnStartup" strategy for the item.
     * If the item state is still undefined when entering this method, all persistence configurations are checked,
     * if they have the "restoreOnStartup" strategy configured for the item. If so, the item state will be set
     * to its last persisted value.
     *
     * @param item the item to restore the state for
     */
    private void initialize(Item item) {
        // get the last persisted state from the persistence service if no state is yet set
        if (item.getState().equals(UnDefType.NULL) && item instanceof GenericItem) {
            for (Entry<String, PersistenceServiceConfiguration> entry : persistenceServiceConfigs.entrySet()) {
                final String serviceName = entry.getKey();
                final PersistenceServiceConfiguration config = entry.getValue();
                for (SimpleItemConfiguration itemConfig : config.getConfigs()) {
                    if (hasStrategy(serviceName, itemConfig, SimpleStrategy.Globals.RESTORE)) {
                        if (appliesToItem(itemConfig, item)) {
                            PersistenceService service = persistenceServices.get(serviceName);
                            if (service instanceof QueryablePersistenceService) {
                                QueryablePersistenceService queryService = (QueryablePersistenceService) service;
                                FilterCriteria filter = new FilterCriteria().setItemName(item.getName()).setPageSize(1);
                                Iterable<HistoricItem> result = queryService.query(filter);
                                Iterator<HistoricItem> it = result.iterator();
                                if (it.hasNext()) {
                                    HistoricItem historicItem = it.next();
                                    GenericItem genericItem = (GenericItem) item;
                                    genericItem.removeStateChangeListener(this);
                                    genericItem.setState(historicItem.getState());
                                    genericItem.addStateChangeListener(this);
                                    logger.debug("Restored item state from '{}' for item '{}' -> '{}'",
                                            new Object[] {
                                                    DateFormat.getDateTimeInstance()
                                                            .format(historicItem.getTimestamp()),
                                                    item.getName(), historicItem.getState().toString() });
                                    return;
                                }
                            } else if (service != null) {
                                logger.warn(
                                        "Failed to restore item states as persistence service '{}' can not be queried.",
                                        serviceName);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates and schedules a new quartz-job.
     *
     * @param modelName the name of the model
     * @param strategies a collection of strategies
     */
    private void createTimers(final String modelName, List<SimpleStrategy> strategies) {
        for (SimpleStrategy strategy : strategies) {
            if (strategy instanceof SimpleCronStrategy) {
                SimpleCronStrategy cronStrategy = (SimpleCronStrategy) strategy;
                String cronExpression = cronStrategy.getCronExpression();
                final CronExpression expression;
                try {
                    expression = new CronExpression(cronExpression);
                } catch (final ParseException ex) {
                    logger.warn("Cannot parse cron expression ({}).", cronExpression, ex);
                    continue;
                }

                final PersistItemsJob job = new PersistItemsJob(this, modelName, cronStrategy.getName());
                if (persistenceJobs.containsKey(modelName)) {
                    persistenceJobs.get(modelName).add(job);
                } else {
                    final Set<Runnable> jobs = new HashSet<>();
                    jobs.add(job);
                    persistenceJobs.put(modelName, jobs);
                }

                scheduler.schedule(job, expression);
                logger.debug("Scheduled strategy {} with cron expression {}", cronStrategy.getName(), cronExpression);
            }
        }
    }

    /**
     * Delete all {@link Job}s of the group <code>persistModelName</code>
     *
     * @throws SchedulerException if there is an internal Scheduler error.
     */
    private void removeTimers(String persistModelName) {
        if (!persistenceJobs.containsKey(persistModelName)) {
            return;
        }
        for (final Runnable job : persistenceJobs.get(persistModelName)) {
            boolean success = scheduler.remove(job);
            if (success) {
                logger.debug("Removed scheduled cron job for dbId '{}'", persistModelName);
            } else {
                logger.warn("Failed to delete cron job for dbId '{}'", persistModelName);
            }
        }
        persistenceJobs.remove(persistModelName);
    }

    /*
     * PersistenceManager
     */

    @Override
    public void addConfig(final String dbId, final PersistenceServiceConfiguration config) {
        synchronized (persistenceServiceConfigs) {
            this.persistenceServiceConfigs.put(dbId, config);
            if (itemRegistry != null && persistenceServices.containsKey(dbId)) {
                startEventHandling(dbId);
            }
        }
    }

    @Override
    public void removeConfig(final String dbId) {
        synchronized (persistenceServiceConfigs) {
            stopEventHandling(dbId);
            this.persistenceServiceConfigs.remove(dbId);
        }
    }

    @Override
    public void startEventHandling(final String dbId) {
        synchronized (persistenceServiceConfigs) {
            final PersistenceServiceConfiguration config = persistenceServiceConfigs.get(dbId);
            if (config == null) {
                return;
            }

            if (itemRegistry != null) {
                for (SimpleItemConfiguration itemConfig : config.getConfigs()) {
                    if (hasStrategy(dbId, itemConfig, SimpleStrategy.Globals.RESTORE)) {
                        for (Item item : getAllItems(itemConfig)) {
                            initialize(item);
                        }
                    }
                }
            }
            createTimers(dbId, config.getStrategies());
        }
    }

    @Override
    public void stopEventHandling(String modelName) {
        synchronized (persistenceServiceConfigs) {
            removeTimers(modelName);
        }
    }

    /*
     * ItemRegistryChangeListener
     */

    @Override
    public void allItemsChanged(Collection<String> oldItemNames) {
        for (Item item : itemRegistry.getItems()) {
            added(item);
        }
    }

    @Override
    public void added(Item item) {
        initialize(item);
        if (item instanceof GenericItem) {
            GenericItem genericItem = (GenericItem) item;
            genericItem.addStateChangeListener(this);
        }
    }

    @Override
    public void removed(Item item) {
        if (item instanceof GenericItem) {
            GenericItem genericItem = (GenericItem) item;
            genericItem.removeStateChangeListener(this);
        }
    }

    @Override
    public void updated(Item oldItem, Item item) {
        // not needed here
    }

    /*
     * StateChangeListener
     */

    @Override
    public void stateChanged(Item item, State oldState, State newState) {
        handleStateEvent(item, true);
    }

    @Override
    public void stateUpdated(Item item, State state) {
        handleStateEvent(item, false);
    }

}
