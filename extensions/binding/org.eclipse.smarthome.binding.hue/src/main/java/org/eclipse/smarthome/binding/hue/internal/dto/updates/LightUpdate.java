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
package org.eclipse.smarthome.binding.hue.internal.dto.updates;

import org.eclipse.smarthome.binding.hue.internal.utils.Util;

/**
 * Contains all fields that can be updated on the /lights/{id} rest endpoint
 *
 * @author David Graeff - Initial contribution
 */
public class LightUpdate {
    public String name;

    public LightUpdate(String name) {
        if (name != null && Util.stringSize(name) > 32) {
            throw new IllegalArgumentException("Name can be at most 32 characters long");
        }
        this.name = name;
    }
}
