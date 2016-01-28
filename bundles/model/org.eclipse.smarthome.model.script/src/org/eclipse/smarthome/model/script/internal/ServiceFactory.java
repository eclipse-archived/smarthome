/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.script.IServiceFactory;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.smarthome.model.script.engine.action.ActionService;

/**
 * Utility class for providing easy access to services.
 *
 * @author Davy Vanherbergen
 */
public class ServiceFactory implements IServiceFactory {

    private static ServiceFactory instance;

    private ItemRegistry itemRegistry;

    private EventPublisher eventPublisher;

    private ModelRepository modelRepository;

    private ScriptEngine scriptEngine;

    public List<ActionService> actionServices = new CopyOnWriteArrayList<ActionService>();

    public ServiceFactory() {
        if (instance != null) {
            throw new IllegalStateException("ServiceFactory should only be initiated once!");
        }
        instance = this;
    }

    private static ServiceFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ServiceFactory not initialized yet!");
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

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.model.script.internal.IServiceFactory#addActionService(org.eclipse.smarthome.model.script.engine.action.ActionService)
     */
    @Override
    public void addActionService(ActionService actionService) {
        this.actionServices.add(actionService);
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.model.script.internal.IServiceFactory#removeActionService(org.eclipse.smarthome.model.script.engine.action.ActionService)
     */
    @Override
    public void removeActionService(ActionService actionService) {
        this.actionServices.remove(actionService);
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.model.script.internal.IServiceFactory#setItemRegistry(org.eclipse.smarthome.core.items.ItemRegistry)
     */
    @Override
    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.model.script.internal.IServiceFactory#unsetItemRegistry(org.eclipse.smarthome.core.items.ItemRegistry)
     */
    @Override
    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.model.script.internal.IServiceFactory#setEventPublisher(org.eclipse.smarthome.core.events.EventPublisher)
     */
    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.model.script.internal.IServiceFactory#unsetEventPublisher(org.eclipse.smarthome.core.events.EventPublisher)
     */
    @Override
    public void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.model.script.internal.IServiceFactory#setModelRepository(org.eclipse.smarthome.model.core.ModelRepository)
     */
    @Override
    public void setModelRepository(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.model.script.internal.IServiceFactory#unsetModelRepository(org.eclipse.smarthome.model.core.ModelRepository)
     */
    @Override
    public void unsetModelRepository(ModelRepository modelRepository) {
        this.modelRepository = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.model.script.internal.IServiceFactory#setScriptEngine(org.eclipse.smarthome.model.script.engine.ScriptEngine)
     */
    @Override
    public void setScriptEngine(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.model.script.internal.IServiceFactory#unsetScriptEngine(org.eclipse.smarthome.model.script.engine.ScriptEngine)
     */
    @Override
    public void unsetScriptEngine(ScriptEngine scriptEngine) {
        this.scriptEngine = null;
    }
}
