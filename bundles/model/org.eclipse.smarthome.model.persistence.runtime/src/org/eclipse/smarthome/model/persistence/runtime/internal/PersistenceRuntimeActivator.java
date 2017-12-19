/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
        PersistenceStandaloneSetup.unregister();
    }

    @Override
    public String getExtension() {
        return "persist";
    }

}
