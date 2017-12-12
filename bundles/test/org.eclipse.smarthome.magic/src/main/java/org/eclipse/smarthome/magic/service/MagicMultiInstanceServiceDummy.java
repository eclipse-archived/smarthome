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
package org.eclipse.smarthome.magic.service;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.MultipleInstanceServiceInfo;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@Component(immediate = true)
@NonNullByDefault
public class MagicMultiInstanceServiceDummy implements MultipleInstanceServiceInfo {

    @Override
    public String getLabel() {
        return "MagicMultiInstanceService";
    }

    @Override
    public String getCategory() {
        return "test";
    }

    @Override
    public String getServicePID() {
        // we are responsible of creating multiple instances of MagicMultiInstanceService
        return "org.eclipse.smarthome.magicMultiInstance";
    }

    @Override
    public String getConfigDescriptionUri() {
        return "test:multipleMagic";
    }

}
