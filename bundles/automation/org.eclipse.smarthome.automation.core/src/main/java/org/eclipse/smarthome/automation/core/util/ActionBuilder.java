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
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This class allows the easy construction of a {@link Module} instance using the builder pattern.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public class ActionBuilder<T extends Module> {

    private final Module module;

    protected ActionBuilder(Module module) {
        this.module = module;
    }

    public static ActionBuilder<Action> createAction() {
        Action action = new Action();
        return new ActionBuilder<Action>(action);
    }

    public static ActionBuilder<? extends Module> create(Module module) {
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

    public static ActionBuilder<Action> createAction(Action action) {
        Action Action = new Action();
        fillModuleFields(action, Action);
        Action.setInputs(new HashMap<>(action.getInputs()));
        return new ActionBuilder<Action>(Action);
    }

    public static ActionBuilder<Trigger> createTrigger() {
        Trigger trigger = new Trigger();
        return new ActionBuilder<Trigger>(trigger);
    }

    public static ActionBuilder<Trigger> createTrigger(Trigger trigger) {
        Trigger Trigger = new Trigger();
        fillModuleFields(trigger, Trigger);
        return new ActionBuilder<Trigger>(Trigger);
    }

    public static ActionBuilder<Condition> createCondition() {
        Condition condition = new Condition();
        return new ActionBuilder<Condition>(condition);
    }

    public static ActionBuilder<Condition> createCondition(Condition condition) {
        Condition Condition = new Condition();
        fillModuleFields(condition, Condition);
        Condition.setInputs(new HashMap<>(condition.getInputs()));
        return new ActionBuilder<Condition>(Condition);
    }

    public ActionBuilder<T> withId(@Nullable String id) {
        this.module.setId(id);
        return this;
    }

    public ActionBuilder<T> withTypeUID(@Nullable String typeUID) {
        this.module.setTypeUID(typeUID);
        return this;
    }

    public ActionBuilder<T> withLabel(@Nullable String label) {
        this.module.setLabel(label);
        return this;
    }

    public ActionBuilder<T> withDescription(@Nullable String description) {
        this.module.setDescription(description);
        return this;
    }

    public ActionBuilder<T> withConfiguration(Configuration configuration) {
        this.module.setConfiguration(configuration);
        return this;
    }

    public ActionBuilder<T> withInputs(@Nullable Map<String, String> inputs) {
        if (inputs == null) {
            return this;
        }
        if (module instanceof Trigger) {
            throw new UnsupportedOperationException();
        }
        if (module instanceof Condition) {
            ((Condition) module).setInputs(new HashMap<>(inputs));
        }
        if (module instanceof Action) {
            ((Action) module).setInputs(new HashMap<>(inputs));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public T build() {
        return (T) this.module;
    }

    private static void fillModuleFields(Module module, Module Module) {
        Module.setId(module.getId());
        Module.setTypeUID(module.getTypeUID());
        Module.setLabel(module.getLabel());
        Module.setDescription(module.getDescription());
        Module.setConfiguration(new Configuration(module.getConfiguration().getProperties()));
    }
}
