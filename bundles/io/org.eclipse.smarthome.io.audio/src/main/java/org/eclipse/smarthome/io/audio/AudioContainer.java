/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio;

/**
 * A collection of constants for container formats
 *
 * @author Harald Kuhn - Initial API
 * @author Kelly Davis - Modified to match discussion in #584
 */
public class AudioContainer {
    /**
     * {@link AudioCodec} encoded data without any container header or footer,
     * e.g. MP3 is a non-container format
     */
    public static final String NONE = "NONE";

    /**
     * Microsofts wave container format
     *
     * @see <a href="http://bit.ly/1TUW93t">WAV Format</a>
     * @see <a href="http://bit.ly/1oRMKOt">Supported codecs</a>
     * @see <a href="http://bit.ly/1TUWSlk">RIFF container format</a>
     */
    public static final String WAVE = "WAVE";
    

    /**
     * OGG container format
     *
     * @see <a href="http://bit.ly/1oRMWNE">OGG</a>
     */ 
    public static final String OGG = "OGG";   
}
