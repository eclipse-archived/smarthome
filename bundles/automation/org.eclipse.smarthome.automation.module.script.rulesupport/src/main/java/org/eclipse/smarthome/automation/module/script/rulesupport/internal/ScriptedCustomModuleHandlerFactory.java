/**
 * Copyright (c) 2015-2017 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.internal;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.ScriptedHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link ScriptedCustomModuleHandlerFactory} is used in combination with the
 * {@link ScriptedCustomModuleTypeProvider} to allow scripts to define custom types in the RuleEngine. These
 * registered types can then be used publicly from any Rule-Editor.
 *
 * This class provides the handlers from the script to the RuleEngine. As Jsr223 languages have different needs, it
 * allows these handlers to be defined in different ways.
 *
 * @author Simon Merschjohann
 *
 */
public class ScriptedCustomModuleHandlerFactory extends AbstractScriptedModuleHandlerFactory {
    private final HashMap<String, ScriptedHandler> typesHandlers = new HashMap<>();

    private ServiceRegistration<?> bmhfReg;

    @Override
    public void activate(BundleContext bundleContext) {
        super.activate(bundleContext);

        bmhfReg = bundleContext.registerService(ModuleHandlerFactory.class.getName(), this, null);
    }

    @Override
    public Collection<String> getTypes() {
        return typesHandlers.keySet();
    }

    @Override
    protected ModuleHandler internalCreate(Module module, String ruleUID) {
        ScriptedHandler scriptedHandler = typesHandlers.get(module.getTypeUID());

        return getModuleHandler(module, scriptedHandler);
    }

    public void addModuleHandler(String uid, ScriptedHandler scriptedHandler) {
        typesHandlers.put(uid, scriptedHandler);
    }

    public void removeModuleHandler(String uid) {
        typesHandlers.remove(uid);
    }
}
