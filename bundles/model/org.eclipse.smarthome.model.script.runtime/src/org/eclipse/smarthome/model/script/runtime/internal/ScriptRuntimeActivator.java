/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.runtime.internal;

import org.eclipse.smarthome.model.script.IServiceFactory;
import org.eclipse.smarthome.model.script.runtime.ScriptRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptRuntimeActivator implements ScriptRuntime {

    private final Logger logger = LoggerFactory.getLogger(ScriptRuntimeActivator.class);

    @SuppressWarnings("unused")
    private IServiceFactory serviceFactory;

    public void activate() throws Exception {
        ScriptRuntimeStandaloneSetup.doSetup();
        logger.debug("Registered 'script' configuration parser");
    }

    public void deactivate() throws Exception {
    }

    /**
     * Set ServiceFactory through DS to make sure it is initialized before the runtime is used.
     * 
     * @param serviceFactory ServiceFactory component
     */
    public void setServiceFactory(IServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    public void unsetServiceFactory(IServiceFactory serviceFactory) {
        this.serviceFactory = null;
    }

}
