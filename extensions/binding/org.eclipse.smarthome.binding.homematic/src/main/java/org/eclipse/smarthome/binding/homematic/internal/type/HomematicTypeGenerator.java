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
package org.eclipse.smarthome.binding.homematic.internal.type;

import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;

/**
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface HomematicTypeGenerator {

    /**
     * Initializes the type generator.
     */
    public void initialize();

    /**
     * Generates the ThingType and ChannelTypes for the given device.
     */
    public void generate(HmDevice device);

    /**
     * Validates all devices for multiple firmware versions. Different firmware versions for the same device may have
     * different datapoints which may cause warnings in the logfile.
     */
    public void validateFirmwares();

}
