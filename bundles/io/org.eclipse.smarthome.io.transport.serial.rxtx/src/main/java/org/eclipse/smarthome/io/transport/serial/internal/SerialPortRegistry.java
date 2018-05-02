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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.smarthome.io.transport.serial.rxtx.SerialPortCreator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 *
 * @author MatthiasS
 *
 */
@Component(immediate = true, service = SerialPortRegistry.class)
public class SerialPortRegistry {

    private Collection<SerialPortCreator<?>> portCreators;

    public SerialPortRegistry() {
        // register the LOCAL PortCreator as last argument, so that is always taken into account when no other creator
        // is applicable.
        this.portCreators = new TreeSet<SerialPortCreator<?>>(new Comparator<SerialPortCreator<?>>() {

            @Override
            public int compare(SerialPortCreator<?> o1, SerialPortCreator<?> o2) {
                if (o1.getProtocol().equals(SerialPortCreator.LOCAL)) {
                    return 1;
                }
                if (o2.getProtocol().equals(SerialPortCreator.LOCAL)) {
                    return -1;
                }
                return o1.getProtocol().compareTo(o2.getProtocol());
            }
        });

    }

    /**
     * Registers a {@link SerialPortCreator}.
     *
     * @param creator
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void registerSerialPortCreator(SerialPortCreator<?> creator) {
        this.portCreators.add(creator);
    }

    protected void unregisterSerialPortCreator(SerialPortCreator<?> creator) {
        this.portCreators.remove(creator);
    }

    /**
     * Gets the best applicable {@link SerialPortCreator} for the given <code>portName</code>
     *
     * @param portName The port's name.
     * @return A found {@link SerialPortCreator} or null if none could be found.
     */
    @SuppressWarnings("unchecked")
    public <T> SerialPortCreator<T> getPortCreatorForPortName(String portName, Class<T> expectedClass) {
        for (@SuppressWarnings("rawtypes")
        SerialPortCreator creator : this.portCreators) {
            try {
                if (creator.isApplicable(portName, expectedClass)) {
                    return creator;
                }
            } catch (Exception e) {
                System.err.println("Error for SerialPortCreator#isApplicable: " + creator.getClass() + "; "
                        + creator.getProtocol() + " -> " + e.getMessage());
            }
        }
        return null;
    }

    public Collection<SerialPortCreator<?>> getPortCreators() {
        return Collections.unmodifiableCollection(portCreators);
    }
}
