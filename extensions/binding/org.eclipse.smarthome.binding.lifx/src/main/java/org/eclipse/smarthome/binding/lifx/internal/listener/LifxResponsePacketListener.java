/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.listener;

import org.eclipse.smarthome.binding.lifx.internal.LifxLightCommunicationHandler;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;

/**
 * The {@link LifxResponsePacketListener} is notified when the {@link LifxLightCommunicationHandler} receives a response
 * packet.
 *
 * @author Wouter Born - Initial contribution
 */
public interface LifxResponsePacketListener {

    /**
     * Called when the {@link LifxLightCommunicationHandler} receives a response packet.
     * 
     * @param packet the received packet
     */
    public void handleResponsePacket(Packet packet);
}
