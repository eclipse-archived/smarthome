/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.sample.handler.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module Handler Factory sample implementation
 *
 * @author Vasil Ilchev - Initial Contribution
 */
public class SampleHandlerFactory implements ModuleHandlerFactory, ServiceTrackerCustomizer {
    public static final String SUPPORTED_TRIGGER = "SampleTrigger";
    public static final String SUPPORTED_CONDITION = "SampleCondition";
    public static final String SUPPORTED_ACTION = "SampleAction";
    public static final String MODULE_SPI_FACTORY_NAME = "[SampleHandlerFactory]";
    public static final String UID_SEPARATOR = ":";
    private BundleContext bc;
    private Logger log;
    private ModuleTypeRegistry moduleTypeRegistry;
    private ServiceTracker moduleTypeRegistryTracker;
    private ServiceReference moduleTypeRegistryRef;
    private List<SampleTriggerHandler> createdTriggerHandler;
    private static final Collection<String> types;
    private ServiceRegistration serviceReg;

    static {
        List temp = new ArrayList();
        temp.add(SUPPORTED_TRIGGER);
        temp.add(SUPPORTED_CONDITION);
        temp.add(SUPPORTED_ACTION);
        types = Collections.unmodifiableCollection(temp);
    }

    public SampleHandlerFactory(BundleContext bc) {
        this.bc = bc;
        moduleTypeRegistryTracker = new ServiceTracker(bc, ModuleTypeRegistry.class.getName(), this);
        moduleTypeRegistryTracker.open();
        log = LoggerFactory.getLogger(SampleHandlerFactory.class);
        createdTriggerHandler = new ArrayList<SampleTriggerHandler>();
        serviceReg = bc.registerService(ModuleHandlerFactory.class.getName(), this, null);
    }

    protected void disposeHandler(ModuleHandler handler) {
        createdTriggerHandler.remove(handler);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        Object result = null;
        if (moduleTypeRegistryRef == null && reference != null) {
            result = moduleTypeRegistry = (ModuleTypeRegistry) bc.getService(reference);
        }
        return result;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        // do nothing
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        if (moduleTypeRegistry == service) {
            // dispose();
        }
    }

    @Override
    public Collection<String> getTypes() {
        return types;
    }

    @Override
    public <T extends ModuleHandler> T create(Module module) {
        ModuleHandler moduleHandler = null;
        if (moduleTypeRegistry != null) {
            String typeUID = module.getTypeUID();
            // calculate fiHandler
            String handlerId = getHandlerUID(typeUID);
            ModuleType moduleType = moduleTypeRegistry.get(handlerId, null);
            if (moduleType != null) {
                // create needed handler
                if (SUPPORTED_TRIGGER.equals(handlerId)) {
                    moduleHandler = new SampleTriggerHandler(this, (Trigger) module, (TriggerType) moduleType, bc, log);
                    createdTriggerHandler.add((SampleTriggerHandler) moduleHandler);
                } else if (SUPPORTED_CONDITION.equals(handlerId)) {
                    moduleHandler = new SampleConditionHandler(this, (Condition) module, (ConditionType) moduleType, bc);
                } else if (SUPPORTED_ACTION.equals(handlerId)) {
                    moduleHandler = new SampleActionHandler(this, (Action) module, (ActionType) moduleType, bc);
                } else {
                    log.error(MODULE_SPI_FACTORY_NAME + "Not supported moduleHandler: " + handlerId);
                }
            } else {
                log.error(MODULE_SPI_FACTORY_NAME + "Not supported moduleType: " + typeUID);
            }
        } else {
            log.error(MODULE_SPI_FACTORY_NAME + "ModuleTypeRegistry service is not available");
        }
        return (T) moduleHandler;
    }

    /**
     * Release used resources
     */
    public void dispose() {
        if (moduleTypeRegistryRef != null) {
            bc.ungetService(moduleTypeRegistryRef);
            moduleTypeRegistryRef = null;
        }
        if (moduleTypeRegistryTracker != null) {
            moduleTypeRegistryTracker.close();
        }
        moduleTypeRegistry = null;
        if (serviceReg != null) {
            serviceReg.unregister();
            serviceReg = null;
        }
        createdTriggerHandler.clear();
    }

    public List<SampleTriggerHandler> getCreatedTriggerHandler() {
        return createdTriggerHandler;
    }

    private String getHandlerUID(String moduleTypeUID) {
        StringTokenizer tokenizer = new StringTokenizer(moduleTypeUID, UID_SEPARATOR);
        return tokenizer.nextToken();
    }
}
