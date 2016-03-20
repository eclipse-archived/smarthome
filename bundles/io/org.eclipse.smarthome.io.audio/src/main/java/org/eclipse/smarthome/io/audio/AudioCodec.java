/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio;

/**
 * A collection of constants for commonly used audio codecs
 *
 * @author Harald Kuhn - Initial API
 * @author Kelly Davis - Modified to match discussion in #584
 */
public class AudioCodec {
    /**
     * PCM Signed
     * 
     * @see <a href="http://wiki.multimedia.cx/?title=PCM#PCM_Types">PCM Types</a>
     */
    public static final String PCM_SIGNED = "PCM_SIGNED";

    /**
     * PCM Unsigned
     * 
     * @see <a href="http://wiki.multimedia.cx/?title=PCM#PCM_Types">PCM Types</a>
     */
    public static final String PCM_UNSIGNED = "PCM_UNSIGNED";

    /**
     * MP3 Codec
     *
     * @see <a href="http://wiki.multimedia.cx/index.php?title=MP3">MP3 Codec</a>
     */
    public static final String MP3 = "MP3";

    /**
     * Vorbis Codec
     *
     * @see <a href="http://xiph.org/vorbis/doc/">Vorbis</a>
     */
    public static final String VORBIS = "VORBIS";
}
