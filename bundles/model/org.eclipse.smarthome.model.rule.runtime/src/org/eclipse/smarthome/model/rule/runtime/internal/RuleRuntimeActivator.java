/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.rule.runtime.internal;

import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.rule.RulesStandaloneSetup;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the default OSGi bundle activator
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class RuleRuntimeActivator implements BundleActivator {

    private final Logger logger = LoggerFactory.getLogger(RuleRuntimeActivator.class);

    public static ServiceTracker<ModelRepository, ModelRepository> modelRepositoryTracker;
    public static ServiceTracker<ScriptEngine, ScriptEngine> scriptEngineTracker;

    @Override
    public void start(BundleContext bc) throws Exception {

        RulesStandaloneSetup.doSetup();
        logger.debug("Registered 'rule' configuration parser");
        modelRepositoryTracker = new ServiceTracker<ModelRepository, ModelRepository>(bc, ModelRepository.class, null);
        modelRepositoryTracker.open();

        scriptEngineTracker = new ServiceTracker<ScriptEngine, ScriptEngine>(bc, ScriptEngine.class, null);
        scriptEngineTracker.open();

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        modelRepositoryTracker.close();
        scriptEngineTracker.close();
    }

}
