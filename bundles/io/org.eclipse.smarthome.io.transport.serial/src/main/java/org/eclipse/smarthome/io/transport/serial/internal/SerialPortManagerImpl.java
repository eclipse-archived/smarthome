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
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.SerialPortProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specific serial port manager implementation.
 *
 * @author Markus Rathgeb - Initial contribution
 */
@NonNullByDefault
@Component
public class SerialPortManagerImpl implements SerialPortManager {

    private final Logger logger = LoggerFactory.getLogger(SerialPortManagerImpl.class);

    private @NonNullByDefault({}) SerialPortRegistry registry;

    @Reference
    protected void setSerialportRegistry(SerialPortRegistry registry) {
        this.registry = registry;
    }

    protected void unsetSerialportRegistry(SerialPortRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Stream<SerialPortIdentifier> getIdentifiers() {
        if (registry == null) {
            return Stream.empty();
        }
        return registry.getPortCreators().stream().flatMap(element -> element.getSerialPortIdentifiers());
    }

    @Override
    public @Nullable SerialPortIdentifier getIdentifier(String name) {
        if (registry == null) {
            return null;
        }
        URI portUri = URI.create(name);
        SerialPortProvider portCreator = registry.getPortProviderForPortName(portUri);
        if (portCreator == null) {
            logger.warn("No SerialPortProvider found for: {}", name);
            return null;
        }
        return portCreator.getPortIdentifier(portUri);
    }
}
