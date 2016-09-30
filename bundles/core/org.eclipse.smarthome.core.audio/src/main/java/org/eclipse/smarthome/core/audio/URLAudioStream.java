/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an AudioStream from an URL. Note that some sinks, like Sonos, can directly handle URL
 * based streams, and therefore can/should call getURL() to get an direct reference to the URL.
 *
 * @author Karel Goderis - Initial contribution and API
 * @author Kai Kreuzer - Refactored to not require a source
 *
 */
public class URLAudioStream extends org.eclipse.smarthome.core.audio.AudioStream {

    private static final Pattern plsStreamPattern = Pattern.compile("^File[0-9]=(.+)$");

    private final Logger logger = LoggerFactory.getLogger(URLAudioStream.class);

    private AudioFormat audioFormat;
    private final InputStream inputStream;
    private String url;

    private Socket shoutCastSocket;

    public URLAudioStream(String url) throws AudioException {
        if (url == null) {
            throw new IllegalArgumentException("url must not be null!");
        }
        this.url = url;
        this.audioFormat = new AudioFormat(AudioFormat.CODEC_MP3, AudioFormat.CODEC_MP3, false, 16, null, null);
        this.inputStream = createInputStream();
    }

    private InputStream createInputStream() throws AudioException {
        try {
            if (url.toLowerCase().endsWith(".m3u")) {
                InputStream is = new URL(url).openStream();
                String urls = IOUtils.toString(is);
                for (String line : urls.split("\n")) {
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        url = line;
                        break;
                    }
                }
            } else if (url.toLowerCase().endsWith(".pls")) {
                InputStream is = new URL(url).openStream();
                String urls = IOUtils.toString(is);
                for (String line : urls.split("\n")) {
                    if (!line.isEmpty() && line.startsWith("File")) {
                        Matcher matcher = plsStreamPattern.matcher(line);
                        if (matcher.find()) {
                            url = matcher.group(1);
                            break;
                        }
                    }
                }
            }
            URL streamUrl = new URL(url);
            URLConnection connection = streamUrl.openConnection();
            InputStream is = null;
            if (connection.getContentType().equals("unknown/unknown")) {
                // Java does not parse non-standard headers used by SHOUTCast
                int port = streamUrl.getPort() > 0 ? streamUrl.getPort() : 80;
                // Manipulate User-Agent to receive a stream
                shoutCastSocket = new Socket(streamUrl.getHost(), port);

                OutputStream os = shoutCastSocket.getOutputStream();
                String user_agent = "WinampMPEG/5.09";
                String req = "GET / HTTP/1.0\r\nuser-agent: " + user_agent
                        + "\r\nIcy-MetaData: 1\r\nConnection: keep-alive\r\n\r\n";
                os.write(req.getBytes());
                is = shoutCastSocket.getInputStream();
            } else {
                is = streamUrl.openStream();
            }
            return is;
        } catch (MalformedURLException e) {
            logger.error("URL '{}' is not a valid url : '{}'", url, e.getMessage());
            throw new AudioException("URL not valid");
        } catch (IOException e) {
            logger.error("Cannot set up stream '{}': {}", url, e);
            throw new AudioException("IO Error");
        }
    }

    @Override
    public AudioFormat getFormat() {
        return audioFormat;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    public String getURL() {
        return url;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (shoutCastSocket != null) {
            shoutCastSocket.close();
        }
    }

    @Override
    public String toString() {
        return url;
    }
}
