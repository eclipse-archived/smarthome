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
import org.eclipse.smarthome.automation.parser.Converter;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
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
    private static final String FILTER = //
    "(|(" + Constants.OBJECTCLASS + '=' + ModuleTypeRegistry.class.getName() + ')' + //$NON-NLS-1$
            '(' + Constants.OBJECTCLASS + '=' + Converter.class.getName() + "))";

    public static final String SUPPORTED_TRIGGER = "SampleTrigger";
    public static final String SUPPORTED_CONDITION = "SampleCondition";
    public static final String SUPPORTED_ACTION = "SampleAction";
    public static final String MODULE_HANDLER_FACTORY_NAME = "[SampleHandlerFactory]";
    public static final String UID_SEPARATOR = ":";
    private BundleContext bc;
    private Logger log;
    private static ModuleTypeRegistry moduleTypeRegistry;
    private static Converter converter;
    private ServiceTracker tracker;
    private ServiceReference moduleTypeRegistryRef;
    private ServiceReference converterRef;
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

    public SampleHandlerFactory(BundleContext bc) throws InvalidSyntaxException {
        this.bc = bc;
        createdTriggerHandler = new ArrayList<SampleTriggerHandler>();
        log = LoggerFactory.getLogger(SampleHandlerFactory.class);

        tracker = new ServiceTracker(bc, bc.createFilter(FILTER), this);
        tracker.open();
    }

    protected void disposeHandler(ModuleHandler handler) {
        createdTriggerHandler.remove(handler);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        Object result = null;
        if (reference != null) {
            result = bc.getService(reference);
            if (result != null) {
                if (moduleTypeRegistryRef == null && result instanceof ModuleTypeRegistry) {
                    moduleTypeRegistryRef = reference;
                    moduleTypeRegistry = (ModuleTypeRegistry) result;
                }
                if (converterRef == null && result instanceof Converter) {
                    converterRef = reference;
                    converter = (Converter) result;
                }
            }
        }
        if (serviceReg == null && moduleTypeRegistry != null) {
            serviceReg = bc.registerService(ModuleHandlerFactory.class.getName(), this, null);
        }

        return result;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        // do nothing
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        if (converter == service) {
            bc.ungetService(converterRef);
            converterRef = null;
            converter = null;
        }
        if (moduleTypeRegistry == service) {
            dispose0();
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
            String handlerId = getHandlerUID(typeUID);
            ModuleType moduleType = moduleTypeRegistry.get(handlerId);
            if (moduleType != null) {
                // create needed handler
                if (SUPPORTED_TRIGGER.equals(handlerId)) {
                    moduleHandler = new SampleTriggerHandler(this, (Trigger) module, (TriggerType) moduleType, log);
                    createdTriggerHandler.add((SampleTriggerHandler) moduleHandler);
                } else if (SUPPORTED_CONDITION.equals(handlerId)) {
                    moduleHandler = new SampleConditionHandler(this, (Condition) module, (ConditionType) moduleType);
                } else if (SUPPORTED_ACTION.equals(handlerId)) {
                    moduleHandler = new SampleActionHandler(this, (Action) module, (ActionType) moduleType);
                } else {
                    log.error(MODULE_HANDLER_FACTORY_NAME + "Not supported moduleHandler: " + handlerId);
                }
            } else {
                log.error(MODULE_HANDLER_FACTORY_NAME + "Not supported moduleType: " + typeUID);
            }
        } else {
            log.error(MODULE_HANDLER_FACTORY_NAME + "ModuleTypeRegistry service is not available");
        }
        return (T) moduleHandler;
    }

    /**
     * Release used resources
     */
    public void dispose() {
        if (tracker != null) {
            tracker.close();
        }
        dispose0();
    }

    public void dispose0() {
        if (moduleTypeRegistryRef != null) {
            bc.ungetService(moduleTypeRegistryRef);
            moduleTypeRegistryRef = null;
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

    static ModuleTypeRegistry getModuleTypeRegistry() {
        return moduleTypeRegistry;
    }

    static Converter getConverter() {
        return converter;
    }
}
