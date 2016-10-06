/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio;

import java.net.URL;

import org.eclipse.smarthome.core.audio.internal.AudioServlet;

/**
 * This is an interface that is implemented by {@link AudioServlet} and which allows exposing audio streams through
 * HTTP.
 * Streams are only served a single time and then discarded.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface AudioHTTPServer {

    /**
     * Creates a url for a given {@link AudioStream} where it can be requested a single time.
     *
     * @param stream the stream to serve on HTTP
     * @return the absolute URL to access the stream (using the primary network interface)
     */
    URL serve(AudioStream stream);

    /**
     * Creates a url for a given {@link AudioStream} where it can be requested a single time.
     * This method makes sure that the HTTP response contains the "Content-Length" header as some clients require this.
     * Note that this should only be used if really needed, since it might mean that the whole stream has to be read
     * locally first in order to determine its length.
     *
     * @param stream the stream to serve on HTTP
     * @return the absolute URL to access the stream (using the primary network interface)
     */
    URL serveWithSize(AudioStream stream);

}
