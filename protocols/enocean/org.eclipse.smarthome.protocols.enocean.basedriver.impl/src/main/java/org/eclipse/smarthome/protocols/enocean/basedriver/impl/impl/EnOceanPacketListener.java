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

package org.eclipse.smarthome.protocols.enocean.basedriver.impl.impl;

/**
 * EnOceanPacketListener.
 */
public interface EnOceanPacketListener {

    /**
     * Callback interface method to send complete received packets towards a
     * listener
     * 
     * @param data
     *            the full packet that has been received.
     */
    public void radioPacketReceived(byte[] data);

}
