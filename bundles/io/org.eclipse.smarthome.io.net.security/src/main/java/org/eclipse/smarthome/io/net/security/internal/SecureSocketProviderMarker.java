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
package org.eclipse.smarthome.io.net.security.internal;

import org.eclipse.smarthome.config.core.ConfigurableService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = { SecureSocketProviderMarker.class }, property = {
        Constants.SERVICE_PID + "=org.eclipse.smarthome.secureSocketProviderInstance",
        ConfigurableService.SERVICE_PROPERTY_FACTORY_SERVICE + "=true",
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=secureSocketCertificate",
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=Security",
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=Secure socket certificates" })
public class SecureSocketProviderMarker {
    // this is a marker service and represents a service factory so multiple configuration instances of type
    // "org.eclipse.smarthome.secureSocketProviderInstance" can be created.
}
