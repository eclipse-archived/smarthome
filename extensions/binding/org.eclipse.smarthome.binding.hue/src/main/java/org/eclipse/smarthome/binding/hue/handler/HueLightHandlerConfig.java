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
package org.eclipse.smarthome.binding.hue.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The config holder for {@link HueLightHandler}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueLightHandlerConfig {
    public static final String LIGHT_ID = "lightId";
    public static final String LIGHT_UNIQUE_ID = "uniqueId";
    public static final String MODEL_ID = "modelId";

    public String lightId = "";
    public String uniqueId = "";
    public String modelId = "";
}