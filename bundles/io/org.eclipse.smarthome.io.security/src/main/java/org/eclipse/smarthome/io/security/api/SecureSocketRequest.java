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
 * Use {@link RequestSecureSocketProvider#addProviderRequest(SecureSocketRequest)} to issue a request.
 *
 * {@link RequestSecureSocketProvider} will also notify you about changes of the requested secure socket implementation
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
     * Provides a potentially new secure socket implementation instance to your SecureSocketRequest implementation.
     * It is guaranteed that all requested mandatory capabilities are present and if possible the optional ones as well.
     *
     * If an existing implementation, for instance a SSLContext, changes in the background this method
     * will get called again another two times. First with a null object and in a subsequent call with the new secure
     * socket provider.
     * A change might be that a period renewal took place and new certificates got loaded or that the user removed a
     * specific provider.
     *
     * @param secureSocketProvider The object may be a SSLContext or Scandium
     *            DtlsConnector or any other implementation that the backend matched with the requested capabilities.
     *            May be null, if one or more mandatory capabilities could not be matched. May be null in a subsequent
     *            call if the user removed a provider deliberately.
     */
    void providerResponse(@Nullable Object secureSocketProvider);

    /**
     * Because it is a common case to request the java frameworks own SSLContext, this specific method will get called
     * additionally to {@link SecureSocketRequest#providerResponse(Object)} if the secure socket provider is a
     * SSLContext.
     *
     * You need to react to the former mentioned method as well, because here you will not be notified if the provider
     * vanished.
     *
     * @param secureSocketProvider A SSLContext that matched all {@link #mandatoryCapabilities()}.
     */
    void providerResponse(SSLContext secureSocketProvider);
}
