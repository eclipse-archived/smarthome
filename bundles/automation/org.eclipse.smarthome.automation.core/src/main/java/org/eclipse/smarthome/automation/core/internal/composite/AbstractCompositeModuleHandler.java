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
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
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
public class AbstractCompositeModuleHandler<M extends Module, MT extends ModuleType, H extends ModuleHandler>
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
     * This method creates internal composite context which will be used as context passed to the child
     * handlers. The composite context is base on the rule context, but it also has configuration properties and input
     * values of parent module (the module which type is composite one). The keys in composite context of parent input
     * values and config values are same with '$' prefix.
     *
     * @param context rule context pass to the parent module
     * @return context which combines rule context and input and configuration properties of the parent module.
     */
    protected Map<String, ?> getCompositeContext(Map<String, ?> context) {
        Map<String, Object> result = new HashMap<>(context);
        for (Entry<String, Object> config : module.getConfiguration().entrySet()) {
            result.put("$" + config.getKey(), config.getValue());
        }

        Map<String, String> inputs = null;

        if (module instanceof Condition) {
            inputs = ((Condition) module).getInputs();
        } else if (module instanceof Action) {
            inputs = ((Action) module).getInputs();
        }

        if (inputs != null) {
            for (Entry<String, String> input : inputs.entrySet()) {
                Object o = context.get(input.getValue());
                result.put("$" + input.getKey(), o);
            }
        }
        return result;
    }

    /**
     * This method updates (changes) configuration properties of the child module base on the composite context values.
     * It resolve references of child module configuration properties to inputs and configuration properties of
     * parent module.
     * For example: if a child configuration property has a value '$name' the method looks for such key in composite
     * context and replace the child's configuration value.
     *
     * @param child child module defined by composite module type
     * @param compositeContext context containing rule context and inputs and configuration values of parent module.
     */
    protected void updateChildConfig(Module child, Map<String, ?> compositeContext) {
        for (Entry<String, Object> config : child.getConfiguration().entrySet()) {
            Object o = config.getValue();
            if (o instanceof String) {
                String key = (String) o;
                if (isReference(key)) {
                    Object contextValue = compositeContext.get(key);
                    config.setValue(contextValue);
                }
            }
        }
    }

    /**
     * Check if the string argument is a reference to the composite context or it is a real value.
     *
     * @param ref a string value.
     * @return true when the string value is a reference to the context value
     */
    protected boolean isReference(String ref) {
        return ref.startsWith("$") && ref.length() > 1;
    }

    @Override
    public void dispose() {
        if (moduleHandlerMap != null) {
            moduleHandlerMap.clear();
            moduleHandlerMap = null;
        }
    }

}
