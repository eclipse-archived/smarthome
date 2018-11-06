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
package org.eclipse.smarthome.binding.mqtt.generic.internal.generic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.transform.TransformationService;

/**
 * Provide a transformation service which can be used during MQTT topic transformation.
 *
 * @author Simon Kaufmann - initial contribution and API
 */
@NonNullByDefault
public interface TransformationServiceProvider {

    /**
     * Provide a {@link TransformationService} matching the given type.
     *
     * @param type the type of the requested {@link TransformationService}.
     * @return a {@link TransformationService} matching the given type.
     */
    @Nullable
    TransformationService getTransformationService(String type);

}
