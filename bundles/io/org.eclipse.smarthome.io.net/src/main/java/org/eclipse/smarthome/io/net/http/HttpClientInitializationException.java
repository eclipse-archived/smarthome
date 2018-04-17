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
package org.eclipse.smarthome.io.net.http;

/**
 * This exception is thrown, if an unexpected error occurs during initialization of the jetty client
 * 
 * @author Michael Bock - initial API
 */
public class HttpClientInitializationException extends RuntimeException {

    private static final long serialVersionUID = -3187938868560212413L;

    public HttpClientInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
