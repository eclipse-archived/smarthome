/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.timer.factory;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.module.timer.handler.TimerTriggerHandler;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This HandlerFactory creates TimerTriggerHandlers to control items within the
 * RuleEngine.
 *
 * @author Christoph Knauf - initial contribution
 *
 */
public class TimerModuleHandlerFactory extends BaseModuleHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(TimerModuleHandlerFactory.class);

    private static final Collection<String> types = Arrays.asList(new String[] { TimerTriggerHandler.MODULE_TYPE_ID });

    @Override
    public void activate(BundleContext bundleContext) {
        super.activate(bundleContext);
    }

    @Override
    public Collection<String> getTypes() {
        return types;
    }

    @Override
    protected ModuleHandler internalCreate(Module module, String ruleUID) {
        logger.trace("create {} -> {}", module.getId(), module.getTypeUID());
        ModuleHandler handler = handlers.get(ruleUID + module.getId());
        String moduleTypeUID = module.getTypeUID();

        if (TimerTriggerHandler.MODULE_TYPE_ID.equals(moduleTypeUID) && module instanceof Trigger) {
            TimerTriggerHandler timerTriggerHandler = handler != null && handler instanceof TimerTriggerHandler
                    ? (TimerTriggerHandler) handler : null;
            if (timerTriggerHandler == null) {
                timerTriggerHandler = new TimerTriggerHandler((Trigger) module);
                handlers.put(ruleUID + module.getId(), timerTriggerHandler);
            }
            return timerTriggerHandler;
        } else {
            logger.error("The ModuleHandler is not supported:" + moduleTypeUID);
        }
        return null;
    }
}
