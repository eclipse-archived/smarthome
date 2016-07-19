/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio;

import java.io.InputStream;

/**
 * Wrapper for a source of audio data.
 *
 * In contrast to {@link AudioSource}, this is often a "one time use" instance for passing some audio data,
 * but it is not meant to be registered as a service.
 *
 * The stream needs to be closed by the client that uses it.
 *
 * @author Harald Kuhn - Initial API
 * @author Kelly Davis - Modified to match discussion in #584
 * @author Kai Kreuzer - Refactored to be only a temporary instance for the stream
 */
abstract public class AudioStream extends InputStream {

    /**
     * Gets the supported audio format
     *
     * @return The supported audio format
     */
    abstract public AudioFormat getFormat();

}
