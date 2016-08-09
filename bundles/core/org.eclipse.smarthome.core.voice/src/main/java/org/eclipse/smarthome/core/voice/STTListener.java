/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice;

/**
 * The listener interface for receiving {@link STTEvent} events.
 *
 * A class interested in processing {@link STTEvent} events implements this interface,
 * and its instances are passed to the {@code STTService}'s {@code recognize()} method.
 * Such instances are then targeted for various {@link STTEvent} events corresponding
 * to the speech recognition process.
 *
 * @author Kelly Davis - Initial contribution and API
 */
public interface STTListener {

    /**
     * Invoked when a {@link STTEvent} event occurs during speech recognition.
     *
     * @param sttEvent The {@link STTEvent} fired by the {@link STTService}
     */
    public void sttEventReceived(STTEvent sttEvent);
}
