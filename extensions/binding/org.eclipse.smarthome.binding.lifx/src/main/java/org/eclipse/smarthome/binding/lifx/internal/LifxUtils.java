/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * Utility class for sharing utility methods between objects.
 *
 * @author Wouter Born - Extracted methods from LifxLightHandler
 */
public final class LifxUtils {

    private LifxUtils() {
        // hidden utility class constructor
    }

    public static PercentType kelvinToPercentType(int kelvin) {
        // range is from 2500-9000K
        return new PercentType((kelvin - 9000) / (-65));
    }

    public static int percentTypeToKelvin(PercentType temperature) {
        // range is from 2500-9000K
        return 9000 - (temperature.intValue() * 65);
    }
}
