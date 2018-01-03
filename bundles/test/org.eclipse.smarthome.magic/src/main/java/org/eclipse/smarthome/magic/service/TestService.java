/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.magic.service;

import java.util.Map;
import java.util.Map.Entry;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Triller - Initial contribution
 *
 */
// @Component(configurationPolicy = ConfigurationPolicy.REQUIRE, service = TestService.class, configurationPid =
// "org.eclipse.smarthome.magicMultiInstanceA")
@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "org.eclipse.smarthome.magicMultiInstanceA")
public class TestService {

    private final Logger logger = LoggerFactory.getLogger(TestService.class);

    // @Activate
    // public void activate() {
    // logger.debug("\n ACTIVATE2 !!!");
    // }

    @Activate
    public void activate(Map<String, Object> properties) {
        logger.debug("\nactivate");
        for (Entry<String, Object> e : properties.entrySet()) {
            logger.debug(e.getKey() + " : " + e.getValue());
        }
    }

    @Modified
    public void modified(Map<String, Object> properties) {
        logger.debug("\nmodified");
        for (Entry<String, Object> e : properties.entrySet()) {
            logger.debug(e.getKey() + " : " + e.getValue());
        }
    }
}