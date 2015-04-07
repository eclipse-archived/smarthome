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

import org.osgi.service.enocean.EnOceanChannel;
import org.osgi.service.enocean.descriptions.EnOceanMessageDescription;

/**
 * EnOcean F6-02-01 EEP EnOceanMessageDescription.
 */
public class F60201EnOceanMessageDescription implements EnOceanMessageDescription {

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
        String description = "Light and Blind Control - Application Style 1";
        return description;
    }

}
