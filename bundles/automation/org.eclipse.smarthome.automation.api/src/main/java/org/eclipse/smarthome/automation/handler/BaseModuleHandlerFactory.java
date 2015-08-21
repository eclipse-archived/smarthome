/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common implementation for {@link ModuleHandlerFactory}s.
 *
 * @author Vasil Ilchev - Initial Contribution
 *
 */
@SuppressWarnings("rawtypes")
public abstract class BaseModuleHandlerFactory implements ModuleHandlerFactory, ServiceTrackerCustomizer {
    public static final String MODULE_TYPE_UID_SEPARATOR = ":";
    private BundleContext bundleContext;
    private ServiceTracker serviceTracker;
    private ServiceReference moduleTypeRegistryRef;
    private ServiceRegistration serviceReg;
    private ModuleTypeRegistry moduleTypeRegistry;
    private Logger log;
    private List<ModuleHandler> createdHandlers;

    @SuppressWarnings("unchecked")
    public BaseModuleHandlerFactory(BundleContext bundleContext) {
        if (bundleContext == null) {
            throw new IllegalArgumentException("BundleContext must not be null.");
        }
        this.bundleContext = bundleContext;
        log = LoggerFactory.getLogger(BaseModuleHandlerFactory.class);
        createdHandlers = new ArrayList();
        serviceReg = bundleContext.registerService(ModuleHandlerFactory.class.getName(), this, null);
        serviceTracker = new ServiceTracker(bundleContext, ModuleTypeRegistry.class.getName(), this);
        serviceTracker.open();
    }

    @Override
    public ModuleHandler create(Module module) {
        ModuleHandler moduleHandler = null;
        if (module != null) {
            if (moduleTypeRegistry != null) {
                String typeUID = module.getTypeUID();
                String systemModuleTypeUID = getSystemModuleTypeUID(typeUID);
                List<ModuleType> moduleTypes = getAllModuleTypes(typeUID);
                if (moduleTypes != null) {
                    moduleHandler = createModuleHandlerInternal(module, systemModuleTypeUID, moduleTypes);
                    if (moduleHandler != null) {
                        createdHandlers.add(moduleHandler);
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
        return moduleHandler;
    }

    public void dispose() {
        try {
            bundleContext.ungetService(moduleTypeRegistryRef);
        } catch (IllegalStateException e) {
        }
        serviceReg.unregister();
        serviceTracker.close();
        for (ModuleHandler moduleHandler : createdHandlers) {
            moduleHandler.dispose();
        }
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

    protected abstract ModuleHandler createModuleHandlerInternal(Module module, String systemModuleTypeUID,
            List<ModuleType> moduleTypes);

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

    protected String getSystemModuleTypeUID(String moduleTypeUID) {
        StringTokenizer tokenizer = new StringTokenizer(moduleTypeUID, MODULE_TYPE_UID_SEPARATOR);
        return tokenizer.nextToken();
    }
}
