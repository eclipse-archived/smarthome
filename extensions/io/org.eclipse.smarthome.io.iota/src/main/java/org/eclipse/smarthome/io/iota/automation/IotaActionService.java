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
package org.eclipse.smarthome.io.iota.automation;

import java.util.Dictionary;

import org.eclipse.smarthome.io.iota.utils.IotaUtilsImpl;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Register the OSGi component for providing action rules
 * 
 * @author Theo Giovanna - Initial contribution
 */
@Component(service = { ActionService.class,
        ManagedService.class }, configurationPid = "org.eclipse.smarthome.iota.automation.IotaActionService", immediate = true)
public class IotaActionService implements ActionService, ManagedService {

    private final Logger logger = LoggerFactory.getLogger(IotaActions.class);

    @Override
    public String getActionClassName() {
        return IotaActions.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return IotaActions.class;
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties != null) {
            if (properties.get("seeds") != null) {
                String[] seeds = ((String) properties.get("seeds")).split(",");
                for (String seed : seeds) {
                    try {
                        String seedAddress = (String) properties.get(String.format("%s.address", seed));
                        String protocol = (String) properties.get(String.format("%s.protocol", seed));
                        String host = (String) properties.get(String.format("%s.host", seed));
                        String port = (String) properties.get(String.format("%s.port", seed));

                        IotaActions.addSeedAddressBySeedName(seed, seedAddress);
                        IotaActions.addUtilsBySeed(seed, new IotaUtilsImpl(protocol, host, Integer.parseInt(port)));

                        logger.debug("Retrieved seed config: {}", seed);
                    } catch (NullPointerException e) {
                        logger.warn("Invalid configuration detected: {}. Payments will not be processed", e);
                    }
                }
            }
        } else {
            logger.warn("Invalid configuration detected. Payments will not be processed");
        }
    }

}
