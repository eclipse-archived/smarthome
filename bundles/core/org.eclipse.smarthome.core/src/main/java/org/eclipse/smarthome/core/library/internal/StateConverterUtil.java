/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.internal;

import org.eclipse.smarthome.core.types.State;

/**
 * Helper methods related to state conversion.
 *
 * @author Simon Kaufmann - Initial contribution and API
 */
public class StateConverterUtil {

    public static State defaultConversion(State state, Class<? extends State> target) {
        if (target != null && target.isInstance(state)) {
            return state;
        } else {
            return null;
        }
    }

}
