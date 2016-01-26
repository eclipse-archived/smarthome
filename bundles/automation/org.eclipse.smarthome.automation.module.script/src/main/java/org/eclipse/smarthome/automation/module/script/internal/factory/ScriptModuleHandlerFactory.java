/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.internal.factory;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.module.script.internal.handler.ScriptActionHandler;
import org.eclipse.smarthome.automation.module.script.internal.handler.ScriptConditionHandler;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This HandlerFactory creates ModuleHandlers for scripts.
 *
 * @author Kai Kreuzer
 *
 */
public class ScriptModuleHandlerFactory extends BaseModuleHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(ScriptModuleHandlerFactory.class);

    private static final Collection<String> types = Arrays
            .asList(new String[] { ScriptActionHandler.SCRIPT_ACTION_ID, ScriptConditionHandler.SCRIPT_CONDITION });

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
        String moduleTypeUID = module.getTypeUID();
        if (moduleTypeUID != null) {
            if (ScriptConditionHandler.SCRIPT_CONDITION.equals(moduleTypeUID) && module instanceof Condition) {
                ScriptConditionHandler handler = new ScriptConditionHandler((Condition) module);
                return handler;
            } else if (ScriptActionHandler.SCRIPT_ACTION_ID.equals(moduleTypeUID) && module instanceof Action) {
                ScriptActionHandler handler = new ScriptActionHandler((Action) module);
                return handler;
            } else {
                logger.error("The ModuleHandler is not supported: {}", moduleTypeUID);
            }

        } else {
            logger.error("ModuleType is not registered: {}", moduleTypeUID);
        }
        return null;
    }

}
