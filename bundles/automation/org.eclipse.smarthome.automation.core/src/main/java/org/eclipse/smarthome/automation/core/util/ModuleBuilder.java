/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.core.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.internal.ActionImpl;
import org.eclipse.smarthome.automation.core.internal.ConditionImpl;
import org.eclipse.smarthome.automation.core.internal.ModuleImpl;
import org.eclipse.smarthome.automation.core.internal.TriggerImpl;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This class allows the easy construction of a {@link Module} instance using the builder pattern.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public class ModuleBuilder<T extends Module> {

    private final ModuleImpl module;

    protected ModuleBuilder(ModuleImpl module) {
        this.module = module;
    }

    public static ModuleBuilder<Action> createAction() {
        ActionImpl action = new ActionImpl();
        return new ModuleBuilder<Action>(action);
    }

    public static ModuleBuilder<? extends Module> create(Module module) {
        if (module instanceof Action) {
            return createAction((Action) module);
        } else if (module instanceof Condition) {
            return createCondition((Condition) module);
        } else if (module instanceof Trigger) {
            return createTrigger((Trigger) module);
        } else {
            throw new IllegalArgumentException("Parameter must be an instance of Action, Condition or Trigger.");
        }
    }

    public static ModuleBuilder<Action> createAction(Action action) {
        ActionImpl actionImpl = new ActionImpl();
        fillModuleFields(action, actionImpl);
        actionImpl.setInputs(new HashMap<>(action.getInputs()));
        return new ModuleBuilder<Action>(actionImpl);
    }

    public static ModuleBuilder<Trigger> createTrigger() {
        TriggerImpl trigger = new TriggerImpl();
        return new ModuleBuilder<Trigger>(trigger);
    }

    public static ModuleBuilder<Trigger> createTrigger(Trigger trigger) {
        TriggerImpl triggerImpl = new TriggerImpl();
        fillModuleFields(trigger, triggerImpl);
        return new ModuleBuilder<Trigger>(triggerImpl);
    }

    public static ModuleBuilder<Condition> createCondition() {
        ConditionImpl condition = new ConditionImpl();
        return new ModuleBuilder<Condition>(condition);
    }

    public static ModuleBuilder<Condition> createCondition(Condition condition) {
        ConditionImpl conditionImpl = new ConditionImpl();
        fillModuleFields(condition, conditionImpl);
        conditionImpl.setInputs(new HashMap<>(condition.getInputs()));
        return new ModuleBuilder<Condition>(conditionImpl);
    }

    public ModuleBuilder<T> withId(@Nullable String id) {
        this.module.setId(id);
        return this;
    }

    public ModuleBuilder<T> withTypeUID(@Nullable String typeUID) {
        this.module.setTypeUID(typeUID);
        return this;
    }

    public ModuleBuilder<T> withLabel(@Nullable String label) {
        this.module.setLabel(label);
        return this;
    }

    public ModuleBuilder<T> withDescription(@Nullable String description) {
        this.module.setDescription(description);
        return this;
    }

    public ModuleBuilder<T> withConfiguration(Configuration configuration) {
        this.module.setConfiguration(configuration);
        return this;
    }

    public ModuleBuilder<T> withInputs(@Nullable Map<String, String> inputs) {
        if (inputs == null) {
            return this;
        }
        if (module instanceof TriggerImpl) {
            throw new UnsupportedOperationException();
        }
        if (module instanceof ConditionImpl) {
            ((ConditionImpl) module).setInputs(new HashMap<>(inputs));
        }
        if (module instanceof ActionImpl) {
            ((ActionImpl) module).setInputs(new HashMap<>(inputs));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public T build() {
        return (T) this.module;
    }

    private static void fillModuleFields(Module module, ModuleImpl moduleImpl) {
        moduleImpl.setId(module.getId());
        moduleImpl.setTypeUID(module.getTypeUID());
        moduleImpl.setLabel(module.getLabel());
        moduleImpl.setDescription(module.getDescription());
        moduleImpl.setConfiguration(new Configuration(module.getConfiguration().getProperties()));
    }
}
