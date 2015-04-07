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

package org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils;

/**
 * EnOceanDriverException.
 */
public class EnOceanDriverException extends Exception {

    /** generated */
    private static final long serialVersionUID = -959105375824289441L;

    /**
     * @param string
     */
    public EnOceanDriverException(String string) {
        super("[EnOceanDriverException] " + string);
    }

}
