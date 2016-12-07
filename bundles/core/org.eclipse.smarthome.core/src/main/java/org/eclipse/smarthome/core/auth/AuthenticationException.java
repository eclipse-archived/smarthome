/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.auth;

/**
 * Base type for exceptions thrown by authentication layer.
 *
 * @author Łukasz Dywicki - Initial contribution and API
 * @author Kai Kreuzer - Added JavaDoc and serial id
 *
 */
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 8063538216812770858L;

    /**
     * Creates a new exception instance.
     *
     * @param message exception message
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Creates a new exception instance.
     *
     * @param cause exception cause
     */
    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception instance.
     *
     * @param message exception message
     * @param cause exception cause
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}
