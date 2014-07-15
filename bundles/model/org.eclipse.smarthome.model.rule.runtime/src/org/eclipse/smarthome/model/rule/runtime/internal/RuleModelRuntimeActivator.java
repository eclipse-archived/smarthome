/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.rule.runtime.internal;

import org.eclipse.smarthome.core.scriptengine.ScriptEngine;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;


/**
 * Extension of the default OSGi bundle activator
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class RuleModelRuntimeActivator implements BundleActivator {

	public static ServiceTracker<ModelRepository, ModelRepository> modelRepositoryTracker;
	public static ServiceTracker<ScriptEngine, ScriptEngine> scriptEngineTracker;

	public void start(BundleContext bc) throws Exception {
		modelRepositoryTracker = new ServiceTracker<ModelRepository, ModelRepository>(bc, ModelRepository.class, null);
		modelRepositoryTracker.open();

		scriptEngineTracker = new ServiceTracker<ScriptEngine, ScriptEngine>(bc, ScriptEngine.class, null);
		scriptEngineTracker.open();		

	}

	public void stop(BundleContext context) throws Exception {
		modelRepositoryTracker.close();
		scriptEngineTracker.close();
	}

}
