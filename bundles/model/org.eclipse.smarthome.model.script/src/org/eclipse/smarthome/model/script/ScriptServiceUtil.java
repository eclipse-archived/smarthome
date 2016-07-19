/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Utility class for providing easy access to script services.
 *
 * @author Davy Vanherbergen - Initial contribution
 * @author Kai Kreuzer - renamed and removed interface
 */
public class ScriptServiceUtil {

    private static ScriptServiceUtil instance;

    private ItemRegistry itemRegistry;

    private EventPublisher eventPublisher;

    private ModelRepository modelRepository;

    private ScriptEngine scriptEngine;

    @SuppressWarnings("rawtypes")
    private ServiceTracker scriptEngineTracker;

    public List<ActionService> actionServices = new CopyOnWriteArrayList<ActionService>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void activate(final BundleContext bc) {
        if (instance != null) {
            throw new IllegalStateException("ScriptServiceUtil should only be activated once!");
        }
        instance = this;

        scriptEngineTracker = new ServiceTracker(bc, ScriptEngine.class.getName(), new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference reference) {
                Object service = bc.getService(reference);
                if (service instanceof ScriptEngine) {
                    instance.scriptEngine = (ScriptEngine) service;
                }
                return null;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (service instanceof ScriptEngine) {
                    instance.scriptEngine = null;
                }
            }
        });
        scriptEngineTracker.open();
    }

    public void deactivate() {
        scriptEngineTracker.close();
        instance = null;
    }

    private static ScriptServiceUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ScriptServiceUtil not initialized yet!");
        }
        return instance;
    }

    public static ItemRegistry getItemRegistry() {
        return getInstance().itemRegistry;
    }

    public static EventPublisher getEventPublisher() {
        return getInstance().eventPublisher;
    }

    public static ModelRepository getModelRepository() {
        return getInstance().modelRepository;
    }

    public static ScriptEngine getScriptEngine() {
        return getInstance().scriptEngine;
    }

    public static List<ActionService> getActionServices() {
        return getInstance().actionServices;
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

}
