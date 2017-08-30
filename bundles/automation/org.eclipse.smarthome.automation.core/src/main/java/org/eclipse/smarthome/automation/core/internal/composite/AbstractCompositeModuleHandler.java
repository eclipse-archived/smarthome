/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal.composite;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.internal.ReferenceResolverUtil;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * This class is base implementation of all system composite module handlers: {@link CompositeTriggerHandler},
 * {@link CompositeConditionHandler} and {@link CompositeActionHandler}. The instances of these handlers are created by
 * {@link CompositeModuleHandlerFactory}.
 * The composite module handlers have to serve modules of composite module types. These handlers are responsible to
 * propagate configuration properties and input values of composite module to the child modules defined by the composite
 * module type and to call the handlers which are responsible for the child modules.
 *
 *
 * @author Yordan Mihaylov - Initial Contribution
 *
 * @param <M> type of module. It can be {@link Trigger}, {@link Condition} or {@link Action}
 * @param <MT> type of module type. It can be {@link TriggerType}, {@link ConditionType} or {@link ActionType}
 * @param <H> type of module handler. It can be {@link TriggerHandler}, {@link ConditionHandler} or
 *            {@link ActionHandler}
 */
public abstract class AbstractCompositeModuleHandler<M extends Module, MT extends ModuleType, H extends ModuleHandler>
        implements ModuleHandler {

    protected LinkedHashMap<M, H> moduleHandlerMap;
    protected M module;
    protected MT moduleType;

    /**
     * This constructor creates composite module handler base on composite module, module type of the module and map of
     * pairs of child module instances and corresponding handlers.
     *
     * @param module module of composite type.
     * @param moduleType composite module type. This is the type of module.
     * @param mapModuleToHandler map containing pairs of child modules instances (defined by module type) and their
     *            handlers
     */
    public AbstractCompositeModuleHandler(M module, MT moduleType, LinkedHashMap<M, H> mapModuleToHandler) {
        this.module = module;
        this.moduleType = moduleType;
        this.moduleHandlerMap = mapModuleToHandler;
    }

    /**
     * Creates internal composite context which will be used for resolving child module's context.
     *
     * @param context contains composite inputs and composite configuration.
     * @return context that will be passed to the child module
     */
    protected Map<String, Object> getCompositeContext(Map<String, ?> context) {
        Map<String, Object> result = new HashMap<String, Object>(context);
        result.putAll(module.getConfiguration().getProperties());
        return result;
    }

    /**
     * Creates child context that will be passed to the child handler.
     *
     * @param child Composite Module's child
     * @param compositeContext context with which child context will be resolved.
     * @return child context ready to be passed to the child for execution.
     */
    protected Map<String, Object> getChildContext(Module child, Map<String, ?> compositeContext) {
        return ReferenceResolverUtil.getCompositeChildContext(child, compositeContext);
    }

    @Override
    public void dispose() {
        List<M> children = getChildren();
        for (M child : children) {
            ModuleHandler childHandler = moduleHandlerMap.remove(child);
            if (childHandler != null) {
                childHandler.dispose();
            }
        }
        moduleHandlerMap = null;
    }

    protected abstract List<M> getChildren();

}
