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
package org.eclipse.smarthome.binding.meteoblue.internal;

/**
 * Model for the meteoblue bridge configuration.
 *
 * @author Chris Carman - Initial contribution
 */
public class MeteoBlueBridgeConfig {
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}
