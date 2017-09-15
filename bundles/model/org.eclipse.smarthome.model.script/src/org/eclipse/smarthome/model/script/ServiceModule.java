/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.script.engine.IActionServiceProvider;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.smarthome.model.script.internal.engine.ServiceTrackerActionServiceProvider;
import org.eclipse.smarthome.model.script.script.Script;
import org.eclipse.smarthome.model.script.script.impl.ScriptImpl;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Guice module that binds Eclipse SmartHome services
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class ServiceModule implements Module {

    private final ScriptServiceUtil scriptServiceUtil;
    private final ScriptEngine scriptEngine;

    public ServiceModule(ScriptServiceUtil scriptServiceUtil, ScriptEngine scriptEngine) {
        this.scriptServiceUtil = scriptServiceUtil;
        this.scriptEngine = scriptEngine;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(ItemRegistry.class).toInstance(scriptServiceUtil.getItemRegistryInstance());
        binder.bind(ThingRegistry.class).toInstance(scriptServiceUtil.getThingRegistryInstance());
        binder.bind(ModelRepository.class).toInstance(scriptServiceUtil.getModelRepositoryInstance());
        binder.bind(ScriptEngine.class).toInstance(scriptEngine);
        binder.bind(IActionServiceProvider.class)
                .toInstance(new ServiceTrackerActionServiceProvider(scriptServiceUtil));
        binder.bind(Script.class).to(ScriptImpl.class);
    }

}
