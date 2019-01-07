/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.mqtt.generic.internal.values;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.types.Command;

/**
 * Implements an image value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ImageValue extends Value {
    public ImageValue() {
        super(CoreItemFactory.IMAGE, Collections.emptyList());
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        throw new IllegalArgumentException("Binary type. Command not allowed");
    }

    @Override
    public boolean isBinary() {
        return true;
    }
}
