/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet that serves audio streams via HTTP.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class AudioServlet extends HttpServlet implements AudioHTTPServer {

    private static final long serialVersionUID = -3364664035854567854L;

    private static final String SERVLET_NAME = "/audio";

    private final Logger logger = LoggerFactory.getLogger(AudioServlet.class);

    private Map<String, AudioStream> streams = new ConcurrentHashMap<>();

    protected HttpService httpService;
    private BundleContext bundleContext;

    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;

        try {
            logger.debug("Starting up the audio servlet at " + SERVLET_NAME);
            Hashtable<String, String> props = new Hashtable<String, String>();
            httpService.registerServlet(SERVLET_NAME, this, props, createHttpContext());
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        } catch (ServletException e) {
            logger.error("Error during servlet startup", e);
        }
    }

    protected void unsetHttpService(HttpService httpService) {
        httpService.unregister(SERVLET_NAME);
        this.httpService = null;
    }

    /**
     * Creates an {@link HttpContext}.
     *
     * @return an {@link HttpContext} that grants anonymous access
     */
    protected HttpContext createHttpContext() {
        // TODO: Once we have a role-based permission system in place, we need to make sure that we create an
        // HttpContext here, which allows accessing the servlet without any authentication.
        HttpContext httpContext = httpService.createDefaultHttpContext();
        return httpContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String streamId = StringUtils.substringAfterLast(req.getRequestURI(), "/");
        if (!streams.containsKey(streamId)) {
            logger.debug("Received request for invalid stream id at  {}", req.getRequestURI());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            logger.debug("Stream to serve is {}", streamId);

            AudioStream stream = streams.remove(streamId);

            // try to set the content-type, if possible
            String mimeType = null;
            if (stream.getFormat().getCodec() == AudioFormat.CODEC_MP3) {
                mimeType = "audio/mpeg";
            } else if (stream.getFormat().getContainer() == AudioFormat.CONTAINER_WAVE) {
                mimeType = "audio/wav";
            } else if (stream.getFormat().getContainer() == AudioFormat.CONTAINER_OGG) {
                mimeType = "audio/ogg";
            }
            if (mimeType != null) {
                resp.setContentType(mimeType);
            }

            // try to set the content-length, if possible
            if (stream instanceof DiscreteAudioStream) {
                Integer size = ((DiscreteAudioStream) stream).size();
                if (size != null) {
                    resp.setContentLength(size);
                }
            }

            ServletOutputStream os = resp.getOutputStream();
            IOUtils.copy(stream, os);
            resp.flushBuffer();
            IOUtils.closeQuietly(stream);
        }
    }

    @Override
    public URL serve(AudioStream stream) {
        String streamId = UUID.randomUUID().toString();
        streams.put(streamId, stream);
        return getURL(streamId);
    }

    @Override
    public URL serveWithSize(AudioStream stream) {
        if (stream.markSupported()) {
            DiscreteAudioStream streamWithSize = new DiscreteAudioStream(stream);
            return serve(streamWithSize);
        } else {
            // TODO: We should also support streams without mark support, but this will mean that we have to read it
            // first to memory or file system and create a new AudioStream from there.
            logger.warn("Stream cannot be reset, so it is served without size through HTTP");
            return serve(stream);
        }
    }

    private URL getURL(String streamId) {
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress(); // we use the primary interface; if a client
                                                                            // knows it any better, he can himself
                                                                            // change the url according to his needs.
            String port = bundleContext.getProperty("org.osgi.service.http.port"); // we do not use SSL as it can cause
                                                                                   // certificate validation issues.
            return new URL("http://" + ipAddress + ":" + port + SERVLET_NAME + "/" + streamId);
        } catch (UnknownHostException | MalformedURLException e) {
            logger.error("Failed to construct audio stream URL: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * This is a wrapper class around {@link AudioStream}, which additionally provides information about its size.
     * Currently, it only support mark-supporting AudioStreams, which are read and reset in order to dertermine the
     * stream size.
     */
    class DiscreteAudioStream extends AudioStream {

        private Integer size;
        private AudioStream stream;

        public DiscreteAudioStream(AudioStream stream) {
            this.stream = stream;
        }

        public Integer size() {
            if (size == null) {
                size = calculateSize();
            }
            return size;
        }

        private Integer calculateSize() {
            if (stream.markSupported()) {
                return readStreamAndReset(stream);
            }
            return null;
        }

        private Integer readStreamAndReset(AudioStream s) {
            int bytes = 0;
            int avail = 0;
            try {
                while ((avail = s.available()) > 0) {
                    bytes += avail;
                    s.skip(avail);
                }
                s.reset();
            } catch (IOException e) {
                logger.warn("Cannot determine size of audio stream!", e);
                return null;
            }
            return bytes;
        }

        @Override
        public AudioFormat getFormat() {
            return stream.getFormat();
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }

    }
}