/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.internal.core.RuleEngine;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common implementation for {@link ModuleHandlerFactory}s.
 * Implementors must take care of registering the factory as service themselves.
 *
 * @author Vasil Ilchev - Initial Contribution
 * @author Benedikt Niehues - Removed service registration of this
 * @author Kai Kreuzer - refactored and simplified customized module handling
 *
 */
@SuppressWarnings("rawtypes")
public class CustomizedModuleHandlerFactory extends BaseModuleHandlerFactory
        implements ModuleHandlerFactory, ServiceTrackerCustomizer {
    public static final String MODULE_TYPE_UID_SEPARATOR = ":";
    private ServiceTracker serviceTracker;
    private ServiceReference moduleTypeRegistryRef;
    private ModuleTypeRegistry moduleTypeRegistry;
    private Logger log;
    private RuleEngine ruleEngine;

    @SuppressWarnings("unchecked")
    public CustomizedModuleHandlerFactory(BundleContext bundleContext, RuleEngine re) {
        super(bundleContext);
        log = LoggerFactory.getLogger(CustomizedModuleHandlerFactory.class);
        serviceTracker = new ServiceTracker(bundleContext, ModuleTypeRegistry.class.getName(), this);
        serviceTracker.open();
        this.ruleEngine = re;
    }

    @Override
    public ModuleHandler internalCreate(Module module, String ruleUID) {
        ModuleHandler handler = null;
        if (module != null) {
            if (moduleTypeRegistry != null) {
                String typeUID = module.getTypeUID();
                String parentModuleTypeUID = getParentModuleTypeUID(typeUID);
                Module parentModule = createParentModule(module, parentModuleTypeUID);
                ModuleHandler parentHandler = createParentHandler(parentModule, ruleUID);
                List<ModuleType> moduleTypes = getAllModuleTypes(typeUID);
                if (moduleTypes != null) {
                    handler = createCustomizedHandler(parentHandler, parentModule, module, moduleTypes);
                    if (handler != null) {
                        handlers.put(ruleUID + module.getId(), handler);
                    } else {
                        log.error("The Factory was not able to create ModuleHandler for Module with typeUID:"
                                + module.getTypeUID());
                    }
                } else {
                    log.error("Failed to retrieve all ModuleTypes from: " + typeUID);
                }
            } else {
                log.error("ModuleTypeRegistry service is not available");
            }
        } else {
            log.error("Module must not be null.");
        }
        return handler;
    }

    private ModuleHandler createCustomizedHandler(ModuleHandler parentHandler, Module parentModule, Module module,
            List<ModuleType> moduleTypes) {
        if (parentHandler instanceof TriggerHandler) {
            return new CustomizedTriggerHandler((TriggerHandler) parentHandler, (Trigger) parentModule,
                    (Trigger) module, moduleTypes);
        } else if (parentHandler instanceof ConditionHandler) {
            return new CustomizedConditionHandler((ConditionHandler) parentHandler, (Condition) parentModule,
                    (Condition) module, moduleTypes);
        } else if (parentHandler instanceof ActionHandler) {
            return new CustomizedActionHandler((ActionHandler) parentHandler, (Action) parentModule, (Action) module,
                    moduleTypes);
        }
        return null;
    }

    private ModuleHandler createParentHandler(Module parentModule, String rUID) {
        return ruleEngine.getModuleHandler(parentModule, rUID);
    }

    private Module createParentModule(Module module, String parentModuleTypeUID) {
        if (module instanceof Action) {
            return new Action("ParentOf" + module.getId(), parentModuleTypeUID, module.getConfiguration(), null);
        } else if (module instanceof Condition) {
            return new Condition("ParentOf" + module.getId(), parentModuleTypeUID, module.getConfiguration(), null);
        } else if (module instanceof Trigger) {
            return new Trigger("ParentOf" + module.getId(), parentModuleTypeUID, module.getConfiguration());
        }
        return null;
    }

    @Override
    public void dispose() {
        if (moduleTypeRegistryRef != null) {
            try {
                bundleContext.ungetService(moduleTypeRegistryRef);
            } catch (IllegalStateException e) {
            }
        }
        serviceTracker.close();
        for (ModuleHandler handler : handlers.values()) {
            if (handler != null) {
                handler.dispose();
            }
        }
        handlers.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object addingService(ServiceReference reference) {
        Object service = null;
        if (moduleTypeRegistryRef == null && reference != null) {
            moduleTypeRegistryRef = reference;
            service = bundleContext.getService(reference);
            if (service != null) {
                moduleTypeRegistry = (ModuleTypeRegistry) service;
            }
        }

        return service;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        if (moduleTypeRegistryRef == reference) {
            moduleTypeRegistryRef = null;
            moduleTypeRegistry = null;
        }
        bundleContext.ungetService(reference);
    }

    /**
     * Retrieves all ModuleTypes for given ModuleTypeUID
     *
     * @param moduleTypeUID the source module type
     * @return
     * @return list of all module types in the hierarchy
     */

    private List<ModuleType> getAllModuleTypes(String moduleTypeUID) {
        List<ModuleType> allModuleTypes = new ArrayList<ModuleType>();
        String currentModuleTypeUID = moduleTypeUID;
        ModuleType currentModuleType;
        do {
            currentModuleType = moduleTypeRegistry.get(currentModuleTypeUID);
            if (currentModuleType != null) {
                allModuleTypes.add(currentModuleType);
                currentModuleTypeUID = getParentModuleTypeUID(currentModuleTypeUID);
            } else {// error case
                allModuleTypes = null;
                log.error("From ModuleType uid=" + moduleTypeUID + " -> ModuleType uid=" + currentModuleTypeUID
                        + " is not available.");
                break;
            }
        } while (currentModuleTypeUID != null);// while there is parent ModuleType

        return allModuleTypes;
    }

    /**
     * Gets parent moduleTypeUID if passed moduleTypeUID has parent
     *
     * @param childModuleTypeUID the UID of the moduleType
     * @return parent module type UID if passed moduleType has parent, null otherwise
     */
    private String getParentModuleTypeUID(String childModuleTypeUID) {
        String parentModuleTypeUID = null;
        if (childModuleTypeUID.indexOf(MODULE_TYPE_UID_SEPARATOR) != -1) {
            parentModuleTypeUID = childModuleTypeUID.substring(0,
                    childModuleTypeUID.lastIndexOf(MODULE_TYPE_UID_SEPARATOR));
        }
        return parentModuleTypeUID;
    }

    @Override
    public Collection<String> getTypes() {
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void ungetHandler(Module module, String ruleUID, ModuleHandler hdlr) {
        ModuleHandler pHandler = ((AbstractCustomizedModuleHandler<ModuleHandler, Module>) hdlr).parentHandler;
        Module pModule = ((AbstractCustomizedModuleHandler<ModuleHandler, Module>) hdlr).parentModule;
        ModuleHandlerFactory pFactory = ruleEngine.getModuleHandlerFactory(pModule);
        pFactory.ungetHandler(pModule, ruleUID, pHandler);
        super.ungetHandler(module, ruleUID, hdlr);
    }

}
