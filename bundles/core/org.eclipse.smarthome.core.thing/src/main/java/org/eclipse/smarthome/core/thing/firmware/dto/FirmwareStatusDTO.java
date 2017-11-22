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
package org.eclipse.smarthome.core.thing.firmware.dto;

/**
 * This is a data transfer object that is used to serialize firmware status information.
 *
 * @author Aoun Bukhari - Initial contribution
 *
 */
public class FirmwareStatusDTO {
    public final String status;
    public final String updatableVersion;

    public FirmwareStatusDTO(String status, String updatableVersion) {
        this.status = status;
        this.updatableVersion = updatableVersion;
    }
}
