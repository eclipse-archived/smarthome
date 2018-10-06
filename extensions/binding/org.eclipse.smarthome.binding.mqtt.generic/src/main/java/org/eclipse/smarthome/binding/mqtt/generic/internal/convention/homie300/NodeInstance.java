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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * TODO Homie specification needs a revised documentation and handling of instances.
 * Therefore Instances are not implemented yet.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class NodeInstance {
    public final Node parentNode;

    NodeInstance(Node parentNode) {
        this.parentNode = parentNode;
    }
}
