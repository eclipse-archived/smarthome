package org.eclipse.smarthome.io.sound.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamAudioSource implements AudioSource {

    private static final Logger logger = LoggerFactory.getLogger(StreamAudioSource.class);

    private static final Pattern plsStreamPattern = Pattern.compile("^File[0-9]=(.+)$");

    private String url;

    public StreamAudioSource(String url) {
        this.url = url;

        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(AudioSource.class.getName(), this, new Hashtable<String, Object>());
    }

    @Override
    public String getId() {
        return url;
    }

    @Override
    public String getLabel(Locale locale) {
        return "Streamed Sound";
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        AudioFormat audioFormat = new AudioFormat(AudioFormat.CODEC_MP3, AudioFormat.CODEC_MP3, false, 16, null, null);
        return Collections.singleton(audioFormat);
    }

    @Override
    public AudioStream getInputStream() throws AudioException {
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
                Socket shoutCastSocket = new Socket(streamUrl.getHost(), port);

                OutputStream os = shoutCastSocket.getOutputStream();
                String user_agent = "WinampMPEG/5.09";
                String req = "GET / HTTP/1.0\r\nuser-agent: " + user_agent
                        + "\r\nIcy-MetaData: 1\r\nConnection: keep-alive\r\n\r\n";
                os.write(req.getBytes());
                is = shoutCastSocket.getInputStream();
            } else {
                is = streamUrl.openStream();
            }
            if (is != null) {
                return new StreamAudioStream(is,
                        new AudioFormat(AudioFormat.CODEC_MP3, AudioFormat.CODEC_MP3, false, 16, null, null), url);
            }
        } catch (MalformedURLException e) {
            logger.error("URL '{}' is not a valid url : '{}'", url, e.getMessage());
            throw new AudioException("URL not valid");
        } catch (IOException e) {
            logger.error("Cannot set up stream '{}': {}", url, e);
            throw new AudioException("IO Error");
        }

        return null;
    }

}
