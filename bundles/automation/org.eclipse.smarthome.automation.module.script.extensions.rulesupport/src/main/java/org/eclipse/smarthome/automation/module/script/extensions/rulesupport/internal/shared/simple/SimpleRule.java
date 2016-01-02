/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.RuleClassInterface;

public abstract class SimpleRule extends SimpleActionHandler implements RuleClassInterface {
    private String name;
    private String uid;

    private Visibility visibility = Visibility.HIDDEN;

    private List<Action> actions = new ArrayList<Action>();
    private List<Trigger> triggers = new ArrayList<Trigger>();
    private List<Condition> conditions = new ArrayList<Condition>();

    public void setName(String name) {
        this.name = name;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public void setTriggers(List<Trigger> triggers) {
        this.triggers = triggers;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public List<Trigger> getTriggers() {
        return triggers;
    }

    @Override
    public List<Condition> getConditions() {
        return conditions;
    }

    @Override
    public List<Action> getActions() {
        return actions; // will be extended by SimpleActionHandler
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }
}
