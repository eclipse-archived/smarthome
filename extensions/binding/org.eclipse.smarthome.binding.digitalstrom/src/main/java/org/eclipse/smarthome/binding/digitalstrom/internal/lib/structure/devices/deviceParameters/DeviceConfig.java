/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

/**
 * The {@link DeviceConfig} saves device configurations.
 *
 * @author Alexander Betker
 * @author Michael Ochel - add missing java-doc
 * @author Matthias Siegele - add missing java-doc
 */
public interface DeviceConfig {

    /**
     * Returns the digitalSTROM-Device parameter class.
     *
     * @return configuration class
     */
    public int getClass_();

    /**
     * Returns the digitalSTROM-Device configuration index.
     *
     * @return configuration index
     */
    public int getIndex();

    /**
     * Returns the digitalSTROM-Device configuration value.
     *
     * @return configuration value
     */
    public int getValue();
}
