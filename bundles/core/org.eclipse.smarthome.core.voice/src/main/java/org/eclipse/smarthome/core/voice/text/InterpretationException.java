/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.text;

/**
 * An exception used by {@link HumanLanguageInterpreter}s, if an error occurs.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public class InterpretationException extends Exception {

    private static final long serialVersionUID = 76120119745036525L;

    /**
     * Constructs a new interpretation exception.
     *
     * @param msg the textual response. Should be short, localized and understandable by non-technical users.
     */
    public InterpretationException(String msg) {
        super(msg);
    }

}
