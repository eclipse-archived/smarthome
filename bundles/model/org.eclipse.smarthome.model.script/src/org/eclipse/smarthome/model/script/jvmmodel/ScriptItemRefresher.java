/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.jvmmodel;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ScriptItemRefresher} is responsible for reloading script resources every time an item is added or removed.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Kai Kreuzer - added delayed execution
 *
 */
public class ScriptItemRefresher implements ItemRegistryChangeListener {

    private final Logger logger = LoggerFactory.getLogger(ScriptItemRefresher.class);

    // delay before rule resources are refreshed after items or services have changed
    private static final long REFRESH_DELAY = 2000;

    ModelRepository modelRepository;
    private ItemRegistry itemRegistry;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> job;

    public void setModelRepository(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public void unsetModelRepository(ModelRepository modelRepository) {
        this.modelRepository = null;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        this.itemRegistry.addRegistryChangeListener(this);
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry.removeRegistryChangeListener(this);
        this.itemRegistry = null;
    }

    protected void addActionService(ActionService actionService) {
        scheduleScriptRefresh();
    }

    protected void removeActionService(ActionService actionService) {
        scheduleScriptRefresh();
    }

    @Override
    public void added(Item element) {
        scheduleScriptRefresh();
    }

    @Override
    public void removed(Item element) {
        scheduleScriptRefresh();
    }

    @Override
    public void updated(Item oldElement, Item element) {

    }

    @Override
    public void allItemsChanged(Collection<String> oldItemNames) {
        scheduleScriptRefresh();
    }

    private synchronized void scheduleScriptRefresh() {
        if (job != null && !job.isDone()) {
            job.cancel(false);
        }
        job = scheduler.schedule(runnable, REFRESH_DELAY, TimeUnit.MILLISECONDS);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                modelRepository.reloadAllModelsOfType("script");
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            }
        }
    };
}
