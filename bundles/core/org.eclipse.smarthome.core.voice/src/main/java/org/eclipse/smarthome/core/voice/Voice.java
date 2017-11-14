/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice;

import java.util.Locale;

/**
 * This is the interface that a text-to-speech voice has to implement.
 *
 * @author Kelly Davis - Initial contribution and API
 */
public interface Voice {

    /**
     * Globally unique identifier of the voice, must have the format
     * "prefix:voicename", where "prefix" is the id of the related TTS service.
     *
     * @return A String uniquely identifying the voice.
     */
    public String getUID();

    /**
     * The voice label, usually used for GUIs
     *
     * @return The voice label, may not be globally unique
     */
    public String getLabel();

    /**
     * Locale of the voice
     *
     * @return Locale of the voice
     */
    public Locale getLocale();
}
