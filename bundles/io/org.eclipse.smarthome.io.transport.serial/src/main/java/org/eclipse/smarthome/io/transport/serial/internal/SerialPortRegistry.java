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
package org.eclipse.smarthome.io.transport.serial.internal;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.transport.serial.ProtocolType.PathType;
import org.eclipse.smarthome.io.transport.serial.SerialPortProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Registers all {@link SerialPortProvider}s which can be accessed here.
 *
 * @author Matthias Steigenberger - Initial Contribution
 *
 */
@Component(service = SerialPortRegistry.class)
@NonNullByDefault
public class SerialPortRegistry {

    private @NonNullByDefault({}) Collection<SerialPortProvider> portCreators;

    public SerialPortRegistry() {
        this.portCreators = new HashSet<>();
    }

    /**
     * Registers a {@link SerialPortProvider}.
     *
     * @param creator
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    protected void registerSerialPortCreator(SerialPortProvider creator) {
        synchronized (this.portCreators) {
            this.portCreators.add(creator);
        }
    }

    protected void unregisterSerialPortCreator(SerialPortProvider creator) {
        synchronized (this.portCreators) {
            this.portCreators.remove(creator);
        }
    }

    /**
     * Gets the best applicable {@link SerialPortProvider} for the given <code>portName</code>
     *
     * @param portName The port's name.
     * @return A found {@link SerialPortProvider} or null if none could be found.
     */
    public @NonNullByDefault({}) SerialPortProvider getPortProviderForPortName(URI portName) {
        PathType pathType = PathType.fromURI(portName);

        synchronized (this.portCreators) {
            String scheme = portName.getScheme();
            Optional<SerialPortProvider> first = scheme != null
                    ? portCreators.stream()
                            .filter(provider -> provider.getAcceptedProtocols()
                                    .filter(prot -> prot.getScheme().equals(scheme)).count() > 0)
                            .findFirst()
                    : Optional.empty();
            // get a PortProvider which accepts exactly the port with its scheme. If there is none, just try a port with
            // same type (local, net)
            if (!first.isPresent() && scheme != null) {
                return null;
            }

            return first
                    .orElse(portCreators.stream()
                            .filter(provider -> provider.getAcceptedProtocols()
                                    .filter(prot -> prot.getPathType().equals(pathType)).count() > 0)
                            .findFirst().orElse(null));
        }
    }

    public Collection<SerialPortProvider> getPortCreators() {
        synchronized (this.portCreators) {
            return Collections.unmodifiableCollection(new HashSet<>(portCreators));
        }
    }
}
