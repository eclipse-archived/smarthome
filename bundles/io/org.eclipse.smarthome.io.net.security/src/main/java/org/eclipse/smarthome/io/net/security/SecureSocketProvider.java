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
package org.eclipse.smarthome.io.net.security;

import org.eclipse.smarthome.core.common.registry.Identifiable;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.io.net.security.internal.providerImpl.SSLContextConfigurableProvider;
import org.eclipse.smarthome.io.net.security.internal.providerImpl.SelfSignedSSLContextProvider;
import org.eclipse.smarthome.io.net.security.internal.utils.ProviderWithCapabilities;

/**
 * This bundle defines some standard secure socket providers like the {@link SSLContextConfigurableProvider} and
 * {@link SelfSignedSSLContextProvider}.
 * The first one reads a corresponding keystore file for each configured service instance with the given keystore
 * password and private key passwords. It offers standard Java SSLContexts.
 * The second one creates a self-signed certificate for a generated public/private key if no keystore is existing yet
 * and offers SSLContexts for TLS (and DTLS on Java9+).
 *
 * You can provide your own secure socket provider (eg SSLContext or Scandium DtlsConnector) by implementing this
 * interface and declaring your service like this:
 *
 * {@code
 * @Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, service = {
 * SecureSocketProvider.class }, configurationPid = "org.eclipse.smarthome.secureSocketProviderInstance")
 * }
 *
 * @author David Graeff - initial contribution
 *
 */
public interface SecureSocketProvider extends Provider<ProviderWithCapabilities>, Identifiable<String> {
}
