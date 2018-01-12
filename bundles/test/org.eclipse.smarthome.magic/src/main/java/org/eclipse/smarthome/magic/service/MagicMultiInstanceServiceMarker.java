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

import org.osgi.service.component.annotations.Component;

/**
 *
 * @author Stefan Triller - Initial contribution
 *
 */

@Component(immediate = true, service = MagicMultiInstanceServiceMarker.class, property = {
        "service.pid=org.eclipse.smarthome.magicMultiInstance", "esh.factoryservice=true",
        "service.config.label=MagicMultiInstanceService", "service.config.category=test",
        "service.config.description.uri=test:multipleMagic" })
public class MagicMultiInstanceServiceMarker {
    // this is a marker service and represents a service factory so multiple configuration instances of type
    // "org.eclipse.smarthome.magicMultiInstance" can be created.
}
