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
package org.eclipse.smarthome.binding.mqtt.generic.internal.mapping;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implement this interface to be notified of an updated field, registered by {@link MqttTopicClassMapper}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface FieldChanged {
    void fieldChanged(CompletableFuture<Boolean> future, String fieldname, Object value);
}
