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
 * This is a data transfer object that is used to serialize firmware information.
 *
 * @author Aoun Bukhari - Initial contribution
 *
 */
public class FirmwareDTO {
    public final String firmwareUID;
    public final String vendor;
    public final String model;
    public final boolean modelRestricted;
    public final String description;
    public final String version;
    public final String changelog;
    public final String prerequisiteVersion;

    public FirmwareDTO(String firmwareUID, String vendor, String model, boolean modelRestricted, String description,
            String version, String prerequisiteVersion, String changelog) {
        this.firmwareUID = firmwareUID;
        this.vendor = vendor;
        this.model = model;
        this.modelRestricted = modelRestricted;
        this.description = description;
        this.version = version;
        this.prerequisiteVersion = prerequisiteVersion;
        this.changelog = changelog;
    }
}
