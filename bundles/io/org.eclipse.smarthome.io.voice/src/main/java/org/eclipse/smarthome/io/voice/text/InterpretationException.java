/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.text;

/**
 * An exception used by {@link HumanLanguageInterpreter}s, if an error occurs.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public class InterpretationException extends Exception {

    private static final long serialVersionUID = 76120119745036525L;

    public InterpretationException(String msg) {
        super(msg);
    }

}
