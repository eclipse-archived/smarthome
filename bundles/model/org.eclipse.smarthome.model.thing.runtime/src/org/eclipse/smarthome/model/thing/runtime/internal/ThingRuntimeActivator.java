/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.runtime.internal;

import org.eclipse.smarthome.model.thing.ThingStandaloneSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThingRuntimeActivator {

    private final Logger logger = LoggerFactory.getLogger(ThingRuntimeActivator.class);

    public void activate() throws Exception {
        ThingStandaloneSetup.doSetup();
        logger.debug("Registered 'thing' configuration parser");
    }

    public void deactivate() throws Exception {
    }

}
