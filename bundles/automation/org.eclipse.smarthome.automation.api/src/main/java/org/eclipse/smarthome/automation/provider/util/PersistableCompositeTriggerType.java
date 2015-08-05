/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.provider.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.TriggerType;

public class PersistableCompositeTriggerType extends TriggerType {

    public List<PersistableTrigger> modules;

    public PersistableCompositeTriggerType(CompositeTriggerType compositeTrigger) {
        super(compositeTrigger.getUID(), compositeTrigger.getConfigurationDescription(), compositeTrigger.getLabel(),
                compositeTrigger.getDescription(), compositeTrigger.getTags(), compositeTrigger.getVisibility(),
                compositeTrigger.getOutputs());
        modules = new ArrayList<PersistableTrigger>();
        List<Trigger> triggers = compositeTrigger.getModules();
        for (Trigger trigger : triggers) {
            modules.add(new PersistableTrigger(trigger));
        }
    }

    public static Set<CompositeTriggerType> createFrom(Set<PersistableCompositeTriggerType> persSet,
            AutomationFactory factory) {
        Set<CompositeTriggerType> cttSet = new HashSet<CompositeTriggerType>();
        for (PersistableCompositeTriggerType pctt : persSet) {
            cttSet.add(pctt.createCompositeTriggerType(factory));
        }
        return cttSet;
    }

    public CompositeTriggerType createCompositeTriggerType(AutomationFactory factory) {
        List<Trigger> modules = new ArrayList<Trigger>();
        for (PersistableTrigger pTrigger : this.modules) {
            modules.add(pTrigger.createTrigger(factory));
        }
        return new CompositeTriggerType(getUID(), getConfigurationDescription(), getLabel(), getDescription(),
                getTags(), getVisibility(), getOutputs(), modules);
    }

}
