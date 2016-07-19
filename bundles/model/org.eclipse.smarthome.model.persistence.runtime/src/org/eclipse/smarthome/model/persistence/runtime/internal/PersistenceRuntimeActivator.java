/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.persistence.runtime.internal;

import org.eclipse.smarthome.model.core.ModelParser;
import org.eclipse.smarthome.model.persistence.PersistenceStandaloneSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceRuntimeActivator implements ModelParser {

    private final Logger logger = LoggerFactory.getLogger(PersistenceRuntimeActivator.class);

    public void activate() throws Exception {
        PersistenceStandaloneSetup.doSetup();
        logger.debug("Registered 'persistence' configuration parser");
    }

    public void deactivate() throws Exception {
    }

    @Override
    public String getExtension() {
        return "persist";
    }

}
