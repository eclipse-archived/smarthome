/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lirc.internal;

/**
 * Exceptions thrown from the serial interface.
 *
 * @author Andrew Nagle - Initial contributor
 */
public class LIRCResponseException extends Exception {

    private static final long serialVersionUID = 6214176461907613559L;

    /**
     * Constructor. Creates new instance of LIRCResponseException
     */
    public LIRCResponseException() {
        super();
    }

    /**
     * Constructor. Creates new instance of LIRCResponseException
     *
     * @param message the detail message.
     */
    public LIRCResponseException(String message) {
        super(message);
    }

    /**
     * Constructor. Creates new instance of LIRCResponseException
     *
     * @param cause the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public LIRCResponseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor. Creates new instance of LIRCResponseException
     *
     * @param message the detail message.
     * @param cause the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public LIRCResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}
