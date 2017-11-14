/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.runtime.internal;

import org.eclipse.smarthome.model.core.ModelParser;
import org.eclipse.smarthome.model.thing.ThingStandaloneSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThingRuntimeActivator implements ModelParser {

    private final Logger logger = LoggerFactory.getLogger(ThingRuntimeActivator.class);

    public void activate() throws Exception {
        ThingStandaloneSetup.doSetup();
        logger.debug("Registered 'thing' configuration parser");
    }

    public void deactivate() throws Exception {
        ThingStandaloneSetup.unregister();
    }

    @Override
    public String getExtension() {
        return "things";
    }

}
