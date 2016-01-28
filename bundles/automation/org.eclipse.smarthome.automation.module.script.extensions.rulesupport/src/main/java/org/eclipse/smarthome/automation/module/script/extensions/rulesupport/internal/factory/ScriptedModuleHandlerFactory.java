/**
 * Copyright (c) 2015-2016 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.handler.ScriptedHandler;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.handler.SimpleActionHandlerWrapper;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.handler.SimpleConditionHandlerWrapper;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedActionHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedConditionHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.modulehandler.ScriptedTriggerHandlerFactory;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleActionHandler;
import org.eclipse.smarthome.automation.module.script.extensions.rulesupport.internal.shared.simple.SimpleConditionHandler;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptedModuleHandlerFactory extends BaseModuleHandlerFactory implements ModuleTypeProvider {
    private Logger logger = LoggerFactory.getLogger(ScriptedModuleHandlerFactory.class);

    private Collection<String> types = null;

    private final HashMap<String, ScriptedHandler> typesHandlers = new HashMap<>();
    private final HashMap<String, ScriptedHandler> privateTypes = new HashMap<>();
    private final HashMap<String, ModuleType> modulesTypes = new HashMap<>();

    private int nextId = 0;

    private ServiceRegistration<?> mtpReg;
    private ServiceRegistration<?> bmhfReg;

    public ScriptedModuleHandlerFactory(BundleContext bc) throws InvalidSyntaxException {
        super(bc);

        types = new HashSet<String>();

        types.add("ScriptedAction");
        types.add("ScriptedCondition");

        bmhfReg = bundleContext.registerService(ModuleHandlerFactory.class.getName(), this, null);
    }

    @Override
    public Collection<String> getTypes() {
        return types;
    }

    @Override
    protected ModuleHandler internalCreate(Module module, String ruleUID) {
        ModuleHandler moduleHandler = null;

        ScriptedHandler scriptedHandler = null;
        if (module.getTypeUID().equals("ScriptedAction") || module.getTypeUID().equals("ScriptedCondition")) {
            try {
                scriptedHandler = privateTypes.get(module.getConfiguration().get("privId"));
            } catch (Exception e) {
                logger.info("ScriptedHandler not found");
            }
        } else {
            scriptedHandler = typesHandlers.get(module.getTypeUID());
        }

        if (scriptedHandler instanceof SimpleActionHandler) {
            moduleHandler = new SimpleActionHandlerWrapper((Action) module, (SimpleActionHandler) scriptedHandler);
        } else if (scriptedHandler instanceof SimpleConditionHandler) {
            moduleHandler = new SimpleConditionHandlerWrapper((Condition) module,
                    (SimpleConditionHandler) scriptedHandler);
        } else if (scriptedHandler instanceof ScriptedActionHandlerFactory) {
            moduleHandler = ((ScriptedActionHandlerFactory) scriptedHandler).get((Action) module);
        } else if (scriptedHandler instanceof ScriptedTriggerHandlerFactory) {
            moduleHandler = ((ScriptedTriggerHandlerFactory) scriptedHandler).get((Trigger) module);
        } else if (scriptedHandler instanceof ScriptedConditionHandlerFactory) {
            moduleHandler = ((ScriptedConditionHandlerFactory) scriptedHandler).get((Condition) module);
        } else {
            logger.error("Not supported moduleHandler: {}", module.getTypeUID());
        }

        if (moduleHandler != null) {
            handlers.put(ruleUID + module.getId(), moduleHandler);
        }

        return moduleHandler;
    }

    @Override
    public void ungetHandler(Module module, String ruleUID, ModuleHandler hdlr) {
        super.ungetHandler(module, ruleUID, hdlr);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> T getModuleType(String UID, Locale locale) {
        ModuleType handler = modulesTypes.get(UID);

        return (T) handler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> Collection<T> getModuleTypes(Locale locale) {
        return (Collection<T>) modulesTypes.values();
    }

    public void addModuleType(ModuleType moduleType) {
        modulesTypes.put(moduleType.getUID(), moduleType);

        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(REG_PROPERTY_MODULE_TYPES, modulesTypes.keySet());
        if (mtpReg == null) {
            mtpReg = bundleContext.registerService(ModuleTypeProvider.class.getName(), this, properties);
        } else {
            mtpReg.setProperties(properties);
        }
    }

    public void addModuleHandler(String uid, ScriptedHandler scriptedHandler) {
        types.add(uid);
        typesHandlers.put(uid, scriptedHandler);
        updateModuleHandlerOffer(uid, null);
    }

    public void addModule(ModuleType moduleType, ScriptedHandler scriptedHandler) {
        modulesTypes.put(moduleType.getUID(), moduleType);
        types.add(moduleType.getUID());

        typesHandlers.put(moduleType.getUID(), scriptedHandler);

        updateModuleHandlerOffer(moduleType.getUID(), null);
    }

    public void removeModule(String UID) {
        boolean doneSomething = false;
        doneSomething = modulesTypes.remove(UID) != null || doneSomething;
        doneSomething = types.remove(UID) || doneSomething;
        doneSomething = typesHandlers.remove(UID) != null || doneSomething;

        if (doneSomething) {
            updateModuleHandlerOffer(null, UID);
        }
    }

    public String addHandler(String privId, ScriptedHandler scriptedHandler) {
        privateTypes.put(privId, scriptedHandler);
        return privId;
    }

    public String addHandler(ScriptedHandler scriptedHandler) {
        String privId = "i" + (nextId++);
        privateTypes.put(privId, scriptedHandler);
        return privId;
    }

    public void removeHandler(String privId) {
        privateTypes.remove(privId);
    }

    private void updateModuleHandlerOffer(String addedModuleType, String removedModuleType) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>();

        if (addedModuleType != null) {
            Collection<String> elems = new ArrayList<String>(1);
            elems.add(addedModuleType);
            properties.put("added_handlers", elems);
        }
        if (removedModuleType != null) {
            Collection<String> elems = new ArrayList<String>(1);
            elems.add(removedModuleType);
            properties.put("removedhandlers", elems);
        }

        bmhfReg.setProperties(properties);

        properties = new Hashtable<String, Object>();
        properties.put(REG_PROPERTY_MODULE_TYPES, modulesTypes.keySet());
        if (mtpReg == null) {
            mtpReg = bundleContext.registerService(ModuleTypeProvider.class.getName(), this, properties);
        } else {
            mtpReg.setProperties(properties);
        }
    }
}
