/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence.internal;

import java.util.List;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.PersistenceServiceConfiguration;
import org.eclipse.smarthome.core.persistence.SimpleItemConfiguration;
import org.eclipse.smarthome.core.persistence.strategy.SimpleStrategy;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Quartz {@link Job}-Interface. It takes a PersistenceModel and a CronStrategy,
 * scans through the relevant configurations and persists the concerned items.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class PersistItemsJob implements Job {

    private final Logger logger = LoggerFactory.getLogger(PersistItemsJob.class);

    public static final String JOB_DATA_PERSISTMODEL = "model";
    public static final String JOB_DATA_STRATEGYNAME = "strategy";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap jdm = context.getJobDetail().getJobDataMap();
        PersistenceManagerImpl persistenceManager = PersistenceManagerImpl.instance;
        final String modelName = (String) jdm.get(JOB_DATA_PERSISTMODEL);
        final String strategyName = (String) jdm.get(JOB_DATA_STRATEGYNAME);

        if (persistenceManager != null) {
            synchronized (persistenceManager.persistenceServiceConfigs) {
                final PersistenceService persistenceService = persistenceManager.persistenceServices.get(modelName);
                final PersistenceServiceConfiguration config = persistenceManager.persistenceServiceConfigs
                        .get(modelName);

                if (persistenceService != null) {
                    for (SimpleItemConfiguration itemConfig : config.getConfigs()) {
                        if (hasStrategy(config.getDefaults(), itemConfig, strategyName)) {
                            for (Item item : persistenceManager.getAllItems(itemConfig)) {
                                long startTime = System.currentTimeMillis();
                                persistenceService.store(item, itemConfig.getAlias());
                                logger.trace("Storing item '{}' with persistence service '{}' took {}ms", new Object[] {
                                        item.getName(), modelName, System.currentTimeMillis() - startTime });
                            }
                        }

                    }
                }
            }
        } else {
            logger.warn("Persistence manager is not available!");
        }
    }

    private boolean hasStrategy(List<SimpleStrategy> defaults, SimpleItemConfiguration config, String strategyName) {
        // check if the strategy is directly defined on the config
        for (SimpleStrategy strategy : config.getStrategies()) {
            if (strategyName.equals(strategy.getName())) {
                return true;
            }
        }
        // if no strategies are given, check the default strategies to use
        if (config.getStrategies().isEmpty() && isDefault(defaults, strategyName)) {
            return true;
        }
        return false;
    }

    private boolean isDefault(List<SimpleStrategy> defaults, String strategyName) {
        for (SimpleStrategy strategy : defaults) {
            if (strategy.getName().equals(strategyName)) {
                return true;
            }
        }
        return false;
    }

}