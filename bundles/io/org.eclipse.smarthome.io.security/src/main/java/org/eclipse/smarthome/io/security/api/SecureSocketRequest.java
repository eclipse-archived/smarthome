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
package org.eclipse.smarthome.io.security.api;

import java.util.Set;

import javax.net.ssl.SSLContext;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Implement this to request a secure socket implementation (SSLEngine or others) and get notified of the response.
 * Use {@link SecureSocketServers#requestSecureSocketImplementation(SecureSocketRequest)} to issue a request.
 *
 * {@link SecureSocketServers} will also notify you about changes of the requested secure socket implementation
 * during the life-time of the service.
 *
 * @author David Graeff - Initial contribution
 */
public interface SecureSocketRequest {
    /**
     * @return Return mandatory capabilities. They are required for a requested secure socket implementation.
     *         If you need a SSLEngine or you need DTLS support, you would specify this here. Please make yourself
     *         familiar with the java platform and the ESH backend service limitations. You cannot for example
     *         request a SSLEngine with DTLS support in Java 8, this is only an option on Java 9+.
     */
    Set<SecureSocketCapability> mandatoryCapabilities();

    /**
     * @return Return optional capabilities. They are considered if possible to match the best secure socket
     *         implementation. If you prefer a CA-signed certificate over a self-signed one for example, you would
     *         express this here.
     */
    Set<SecureSocketCapability> optionalCapabilities();

    /**
     * Returns a potentially new secure socket implementation instance that fulfills the requested mandatory
     * capabilities and if possible the optional ones as well.
     *
     * If an existing implementation, for instance a SSLContext, changes in the background this method
     * will get called again. A change might be that a period renewal took place and new
     * certificates got loaded or that the user removed that specific implementation.
     *
     * @param secureSocketObject The object may be a SSLContext or Scandium
     *            DtlsConnector or any other implementation that the backend matched with the requested capabilities.
     *            May be null, if at least one capability could not be matched. May be null in a subsequent call if the
     *            user removed an implementation deliberately.
     */
    void secureSocketImplementationResponse(@Nullable Object secureSocketObject);

    /**
     * Because it is a common case to request the java frameworks own SSLContext, this specific method will get called
     * instead of {@link SecureSocketRequest#secureSocketImplementationResponse(Object)} if the secure socket
     * implementation matches a SSLContext.
     *
     * @param secureSocketContext May be null, if at least one capability could not be matched. May be null in a
     *            subsequent call if the user removed an implementation deliberately.
     */
    void secureSocketImplementationResponse(@Nullable SSLContext secureSocketContext);

    /**
     * If an error happens while the secure socket implementation is initialized, for example because an algorithm
     * could not be loaded or an IO error occurred, you will be called by this method.
     *
     * @param error The exception that occurred.
     */
    void secureSocketImplementationUnavailable(Throwable error);
}
