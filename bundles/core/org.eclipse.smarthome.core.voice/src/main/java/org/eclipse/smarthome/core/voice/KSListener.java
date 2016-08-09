/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice;

/**
 * The listener interface for receiving {@link KSEvent} events.
 *
 * A class interested in processing {@link KSEvent} events implements this interface,
 * and its instances are passed to the {@code KSService}'s {@code spot()} method.
 * Such instances are then targeted for various {@link KSEvent} events corresponding
 * to the keyword spotting process.
 *
 * @author Kelly Davis - Initial contribution and API
 */
public interface KSListener {
    /**
     * Invoked when a {@link KSEvent} event occurs during keyword spotting.
     *
     * @param ksEvent The {@link KSEvent} fired by the {@link KSService}
     */
    public void ksEventReceived(KSEvent ksEvent);
}
