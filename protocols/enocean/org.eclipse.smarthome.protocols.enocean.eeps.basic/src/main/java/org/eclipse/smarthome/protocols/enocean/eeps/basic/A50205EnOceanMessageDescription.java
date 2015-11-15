/*******************************************************************************
 * Copyright (c) 2013, 2015 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Victor PERRON, Antonin CHAZALET, Andre BOTTARO.
 *******************************************************************************/

package org.eclipse.smarthome.protocols.enocean.eeps.basic;

/**
 * EnOcean A5-02-05 EEP EnOceanMessageDescription.
 */
public class A50205EnOceanMessageDescription implements EnOceanMessageDescription {

    public byte[] serialize(EnOceanChannel[] channels) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    public EnOceanChannel[] deserialize(byte[] bytes) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMessageDescription() {
        // default description
        String description = "Temperature Sensor Range 0°C to +40°C";
        return description;
    }

}
