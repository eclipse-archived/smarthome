/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for providing easy access to script services.
 *
 * @author Davy Vanherbergen - Initial contribution
 * @author Kai Kreuzer - renamed and removed interface
 */
public class ScriptServiceUtil {

    private final Logger logger = LoggerFactory.getLogger(ScriptServiceUtil.class);

    private static ScriptServiceUtil instance;

    private ItemRegistry itemRegistry;

    private ThingRegistry thingRegistry;

    private EventPublisher eventPublisher;

    private ModelRepository modelRepository;

    private final AtomicReference<ScriptEngine> scriptEngine = new AtomicReference<>();

    public List<ActionService> actionServices = new CopyOnWriteArrayList<ActionService>();

    public void activate(final BundleContext bc) {
        if (instance != null) {
            throw new IllegalStateException("ScriptServiceUtil should only be activated once!");
        }
        instance = this;
        logger.debug("ScriptServiceUtil started");
    }

    public void deactivate() {
        logger.debug("ScriptServiceUtil stopped");
        instance = null;
    }

    private static ScriptServiceUtil getInstance() {
        if (instance == null) {
            // TODO remove the logging once #3562 got resolved
            Logger logger = LoggerFactory.getLogger(ScriptServiceUtil.class);
            Bundle bundle = FrameworkUtil.getBundle(ScriptServiceUtil.class);
            BundleContext context = bundle.getBundleContext();
            if (context != null) {
                logger.debug(
                        "ScriptServiceUtil is not initialized!\n  ThingRegistry: {}\n  ItemRegistry: {}\n  EventPublisher: {}\n  ModelRepository: {}",
                        context.getServiceReference("org.eclipse.smarthome.core.thing.ThingRegistry"),
                        context.getServiceReference("org.eclipse.smarthome.core.items.ItemRegistry"),
                        context.getServiceReference("org.eclipse.smarthome.core.events.EventPublisher"),
                        context.getServiceReference("org.eclipse.smarthome.model.core.ModelRepository"));
                logger.debug("Bundle Versions:\n  o.e.sh.model.rule.runtime: {}\n  o.e.sh.model.core: {}",
                        getVersion(context, "org.eclipse.smarthome.model.rule.runtime"),
                        getVersion(context, "org.eclipse.smarthome.model.core"));
            } else {
                logger.debug("Bundle {} is not started", bundle.getSymbolicName());
            }
            throw new IllegalStateException("ScriptServiceUtil not initialized yet!");
        }
        return instance;
    }

    private static String getVersion(BundleContext context, String bsn) {
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getSymbolicName().equals(bsn)) {
                return bundle.getVersion().toString();
            }
        }
        return null;
    }

    public static ItemRegistry getItemRegistry() {
        return getInstance().itemRegistry;
    }

    public ItemRegistry getItemRegistryInstance() {
        return itemRegistry;
    }

    public ThingRegistry getThingRegistryInstance() {
        return thingRegistry;
    }

    public static EventPublisher getEventPublisher() {
        return getInstance().eventPublisher;
    }

    public static ModelRepository getModelRepository() {
        return getInstance().modelRepository;
    }

    public ModelRepository getModelRepositoryInstance() {
        return modelRepository;
    }

    public static ScriptEngine getScriptEngine() {
        return getInstance().scriptEngine.get();
    }

    public static List<ActionService> getActionServices() {
        return getInstance().actionServices;
    }

    public List<ActionService> getActionServiceInstances() {
        return actionServices;
    }

    public void addActionService(ActionService actionService) {
        this.actionServices.add(actionService);
    }

    public void removeActionService(ActionService actionService) {
        this.actionServices.remove(actionService);
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    public void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    public void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    public void setModelRepository(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public void unsetModelRepository(ModelRepository modelRepository) {
        this.modelRepository = null;
    }

    public void setScriptEngine(ScriptEngine scriptEngine) {
        // injected as a callback from the script engine, not via DS as it is a circular dependency...
        this.scriptEngine.set(scriptEngine);
    }

    public void unsetScriptEngine(ScriptEngine scriptEngine) {
        // uninjected as a callback from the script engine, not via DS as it is a circular dependency...
        this.scriptEngine.compareAndSet(scriptEngine, null);
    }

}
