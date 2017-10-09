/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Plamen Peev - Bosch Software Innovations GmbH - Please refer to git log
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.sample.moduletype.demo.internal.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.sample.moduletype.demo.internal.handlers.CompareCondition;
import org.eclipse.smarthome.automation.sample.moduletype.demo.internal.handlers.ConsolePrintAction;
import org.eclipse.smarthome.automation.sample.moduletype.demo.internal.handlers.ConsoleTrigger;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a factory for creating {@link ConsoleTrigger}, {@link CompareCondition} and {@link ConsolePrintAction}
 * objects.
 * 
 * @author Plamen Peev - Initial contribution
 */
public class HandlerFactory extends BaseModuleHandlerFactory implements ModuleHandlerFactory {

    /**
     * This field contains the name of this factory
     */
    public final static String MODULE_HANDLER_FACTORY_NAME = "[SampleDemoFactory]";

    /**
     * This field contains the types that are supported by this factory.
     */
    private final static Collection<String> types;

    /**
     * For error logging if there is a query for a type that is not supported.
     */
    private final static Logger logger;

    /**
     * This blocks fills the Collection ,which contains the types supported by this factory, with supported types and
     * creates a Logger instance for logging errors occurred during the handler creation.
     */
    static {
        final List<String> temp = new ArrayList<String>();
        temp.add(CompareCondition.UID);
        temp.add(ConsoleTrigger.UID);
        temp.add(ConsolePrintAction.UID);
        types = Collections.unmodifiableCollection(temp);

        logger = LoggerFactory.getLogger(HandlerFactory.class);
    }

    /**
     * This method must deliver the correct handler if this factory can create it or log an error otherwise.
     * It recognises the correct type by {@link Module}'s UID.
     */
    @Override
    protected ModuleHandler internalCreate(Module module, String ruleUID) {
        if (CompareCondition.UID.equals(module.getTypeUID())) {
            return new CompareCondition((Condition) module);
        } else if (ConsolePrintAction.UID.equals(module.getTypeUID())) {
            return new ConsolePrintAction((Action) module, ruleUID);
        } else if (ConsoleTrigger.UID.equals(module.getTypeUID())) {
            return new ConsoleTrigger((Trigger) module, bundleContext);
        } else {
            logger.error(MODULE_HANDLER_FACTORY_NAME + "Not supported moduleHandler: {}", module.getTypeUID());
        }

        return null;
    }

    /**
     * Returns a {@link Collection} that contains the UIDs of the module types for which this factory can create
     * handlers.
     */
    @Override
    public Collection<String> getTypes() {
        return types;
    }

    /**
     * This method is called when all of the services required by this factory are available.
     *
     * @param componentContext - the {@link ComponentContext} of the HandlerFactory component.
     */
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext.getBundleContext());
    }

    /**
     * This method is called when a service that is required from this factory becomes unavailable.
     *
     * @param componentContext - the {@link ComponentContext} of the HandlerFactory component.
     */
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate();
    }
}
