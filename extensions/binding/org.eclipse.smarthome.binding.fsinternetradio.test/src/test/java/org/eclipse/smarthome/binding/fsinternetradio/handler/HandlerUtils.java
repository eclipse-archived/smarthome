/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.handler;

import org.eclipse.smarthome.binding.fsinternetradio.internal.radio.FrontierSiliconRadio;

/**
 * Utils for the handler.
 *
 * @author Markus Rathgeb - Initial contribution
 */
public class HandlerUtils {

    /**
     * Get the radio of a radio handler.
     *
     * @param handler the handler
     * @return the managed radio object
     */
    public static FrontierSiliconRadio getRadio(final FSInternetRadioHandler handler) {
        return handler.radio;
    }
}
