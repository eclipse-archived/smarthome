/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio;

/**
 * This is an {@link AudioStream}, which can provide information about its absolute length.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
abstract public class FixedLengthAudioStream extends AudioStream {

    /**
     * Provides the length of the stream in bytes.
     * 
     * @return absolute length in bytes
     */
    public abstract long length();

}
