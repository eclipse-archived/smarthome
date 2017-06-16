/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.ProxyConfiguration.Proxy;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.library.types.RawType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some common methods to be used in both HTTP-In-Binding and HTTP-Out-Binding
 *
 * @author Thomas Eichstaedt-Engelen
 * @author Kai Kreuzer - Initial contribution and API
 * @author Svilen Valkanov - replaced Apache HttpClient with Jetty
 */
public class HttpUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final HttpClient CLIENT = new HttpClient(new SslContextFactory());

    private static class ProxyParams {
        public String proxyHost = null;
        public int proxyPort = 80;
        public String proxyUser = null;
        public String proxyPassword = null;
        public String nonProxyHosts = null;
    }

    /**
     * Executes the given <code>url</code> with the given <code>httpMethod</code>.
     * Furthermore the <code>http.proxyXXX</code> System variables are read and
     * set into the {@link HttpClient}.
     *
     * @param httpMethod the HTTP method to use
     * @param url the url to execute (in milliseconds)
     * @param timeout the socket timeout to wait for data
     *
     * @return the response body or <code>NULL</code> when the request went wrong
     * @throws IOException when the request execution failed, timed out or it was interrupted
     */
    public static String executeUrl(String httpMethod, String url, int timeout) throws IOException {
        return executeUrl(httpMethod, url, null, null, timeout);
    }

    /**
     * Executes the given <code>url</code> with the given <code>httpMethod</code>.
     * Furthermore the <code>http.proxyXXX</code> System variables are read and
     * set into the {@link HttpClient}.
     *
     * @param httpMethod the HTTP method to use
     * @param url the url to execute (in milliseconds)
     * @param content the content to be send to the given <code>url</code> or <code>null</code> if no content should be
     *            send.
     * @param contentType the content type of the given <code>content</code>
     * @param timeout the socket timeout to wait for data
     *
     * @return the response body or <code>NULL</code> when the request went wrong
     * @throws IOException when the request execution failed, timed out or it was interrupted
     */
    public static String executeUrl(String httpMethod, String url, InputStream content, String contentType, int timeout)
            throws IOException {

        return executeUrl(httpMethod, url, null, content, contentType, timeout);
    }

    /**
     * Executes the given <code>url</code> with the given <code>httpMethod</code>.
     * Furthermore the <code>http.proxyXXX</code> System variables are read and
     * set into the {@link HttpClient}.
     *
     * @param httpMethod the HTTP method to use
     * @param url the url to execute (in milliseconds)
     * @param httpHeaders optional http request headers which has to be sent within request
     * @param content the content to be send to the given <code>url</code> or <code>null</code> if no content should be
     *            send.
     * @param contentType the content type of the given <code>content</code>
     * @param timeout the socket timeout to wait for data
     *
     * @return the response body or <code>NULL</code> when the request went wrong
     * @throws IOException when the request execution failed, timed out or it was interrupted
     */
    public static String executeUrl(String httpMethod, String url, Properties httpHeaders, InputStream content,
            String contentType, int timeout) throws IOException {

        final ProxyParams proxyParams = prepareProxyParams();

        return executeUrl(httpMethod, url, httpHeaders, content, contentType, timeout, proxyParams.proxyHost,
                proxyParams.proxyPort, proxyParams.proxyUser, proxyParams.proxyPassword, proxyParams.nonProxyHosts);
    }

    /**
     * Executes the given <code>url</code> with the given <code>httpMethod</code>
     *
     * @param httpMethod the HTTP method to use
     * @param url the url to execute (in milliseconds)
     * @param httpHeaders optional HTTP headers which has to be set on request
     * @param content the content to be send to the given <code>url</code> or <code>null</code> if no content should be
     *            send.
     * @param contentType the content type of the given <code>content</code>
     * @param timeout the socket timeout to wait for data
     * @param proxyHost the hostname of the proxy
     * @param proxyPort the port of the proxy
     * @param proxyUser the username to authenticate with the proxy
     * @param proxyPassword the password to authenticate with the proxy
     * @param nonProxyHosts the hosts that won't be routed through the proxy
     *
     * @return the response body or <code>NULL</code> when the request went wrong
     * @throws IOException when the request execution failed, timed out or it was interrupted
     */
    public static String executeUrl(String httpMethod, String url, Properties httpHeaders, InputStream content,
            String contentType, int timeout, String proxyHost, Integer proxyPort, String proxyUser,
            String proxyPassword, String nonProxyHosts) throws IOException {

        ContentResponse response = executeUrlAndGetReponse(httpMethod, url, httpHeaders, content, contentType, timeout,
                proxyHost, proxyPort, proxyUser, proxyPassword, nonProxyHosts);
        String encoding = response.getEncoding() != null ? response.getEncoding().replaceAll("\"", "").trim() : "UTF-8";
        String responseBody;
        try {
            responseBody = new String(response.getContent(), encoding);
        } catch (UnsupportedEncodingException e) {
            responseBody = null;
        }
        return responseBody;
    }

    /**
     * Executes the given <code>url</code> with the given <code>httpMethod</code>
     *
     * @param httpMethod the HTTP method to use
     * @param url the url to execute (in milliseconds)
     * @param httpHeaders optional HTTP headers which has to be set on request
     * @param content the content to be send to the given <code>url</code> or <code>null</code> if no content should be
     *            send.
     * @param contentType the content type of the given <code>content</code>
     * @param timeout the socket timeout to wait for data
     * @param proxyHost the hostname of the proxy
     * @param proxyPort the port of the proxy
     * @param proxyUser the username to authenticate with the proxy
     * @param proxyPassword the password to authenticate with the proxy
     * @param nonProxyHosts the hosts that won't be routed through the proxy
     *
     * @return the response as a ContentResponse object or <code>NULL</code> when the request went wrong
     * @throws IOException when the request execution failed, timed out or it was interrupted
     */
    private static ContentResponse executeUrlAndGetReponse(String httpMethod, String url, Properties httpHeaders,
            InputStream content, String contentType, int timeout, String proxyHost, Integer proxyPort, String proxyUser,
            String proxyPassword, String nonProxyHosts) throws IOException {

        startHttpClient(CLIENT);

        HttpProxy proxy = null;
        // only configure a proxy if a host is provided
        if (StringUtils.isNotBlank(proxyHost) && proxyPort != null && shouldUseProxy(url, nonProxyHosts)) {
            AuthenticationStore authStore = CLIENT.getAuthenticationStore();
            ProxyConfiguration proxyConfig = CLIENT.getProxyConfiguration();
            List<Proxy> proxies = proxyConfig.getProxies();

            proxy = new HttpProxy(proxyHost, proxyPort);
            proxies.add(proxy);

            // This value is a replacement for any realm
            final String anyRealm = "*";
            authStore.addAuthentication(new BasicAuthentication(proxy.getURI(), anyRealm, proxyUser, proxyPassword) {

                // In version 9.2.12 Jetty HttpClient does not support adding an authentication for any realm. This is a
                // workaround until this issue is solved
                @Override
                public boolean matches(String type, URI uri, String realm) {
                    realm = anyRealm;
                    return super.matches(type, uri, realm);
                }
            });
        }

        HttpMethod method = HttpUtil.createHttpMethod(httpMethod);

        Request request = CLIENT.newRequest(url).method(method).timeout(timeout, TimeUnit.MILLISECONDS);

        if (httpHeaders != null) {
            for (String httpHeaderKey : httpHeaders.stringPropertyNames()) {
                request.header(httpHeaderKey, httpHeaders.getProperty(httpHeaderKey));
            }
        }

        // add basic auth header, if url contains user info
        try {
            URI uri = new URI(url);
            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":");

                String user = userInfo[0];
                String password = userInfo[1];

                String basicAuthentication = "Basic " + B64Code.encode(user + ":" + password, StringUtil.__ISO_8859_1);
                request.header(HttpHeader.AUTHORIZATION, basicAuthentication);
            }
        } catch (URISyntaxException e) {
            logger.debug("String {} can not be parsed as URI reference", url);
        }

        // add content if a valid method is given ...
        if (content != null && (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT))) {
            request.content(new InputStreamContentProvider(content), contentType);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("About to execute {}", request.getURI());
        }

        try {
            ContentResponse response = request.send();
            int statusCode = response.getStatus();
            if (statusCode >= HttpStatus.BAD_REQUEST_400) {
                String statusLine = statusCode + " " + response.getReason();
                logger.debug("Method failed: {}", statusLine);
            }

            return response;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (proxy != null) {
                // Remove the proxy, that has been added for this request
                CLIENT.getProxyConfiguration().getProxies().remove(proxy);
            }
        }
    }

    /**
     * Load proxy parameters in global variables proxyHost, proxyPort, proxyUser, proxyPassword and nonProxyHosts
     */
    private static ProxyParams prepareProxyParams() {
        final ProxyParams proxyParams = new ProxyParams();
        String proxySet = System.getProperty("http.proxySet");
        if ("true".equalsIgnoreCase(proxySet)) {
            proxyParams.proxyHost = System.getProperty("http.proxyHost");
            String proxyPortString = System.getProperty("http.proxyPort");
            if (StringUtils.isNotBlank(proxyPortString)) {
                try {
                    proxyParams.proxyPort = Integer.valueOf(proxyPortString);
                } catch (NumberFormatException e) {
                    logger.warn("'{}' is not a valid proxy port - using default port ({}) instead", proxyPortString,
                            proxyParams.proxyPort);
                }
            }
            proxyParams.proxyUser = System.getProperty("http.proxyUser");
            proxyParams.proxyPassword = System.getProperty("http.proxyPassword");
            proxyParams.nonProxyHosts = System.getProperty("http.nonProxyHosts");
        }
        return proxyParams;
    }

    /**
     * Determines whether the list of <code>nonProxyHosts</code> contains the
     * host (which is part of the given <code>urlString</code> or not.
     *
     * @param urlString
     * @param nonProxyHosts
     *
     * @return <code>false</code> if the host of the given <code>urlString</code> is contained in
     *         <code>nonProxyHosts</code>-list and <code>true</code> otherwise
     */
    private static boolean shouldUseProxy(String urlString, String nonProxyHosts) {

        if (StringUtils.isNotBlank(nonProxyHosts)) {
            String givenHost = urlString;

            try {
                URL url = new URL(urlString);
                givenHost = url.getHost();
            } catch (MalformedURLException e) {
                logger.error("the given url {} is malformed", urlString);
            }

            String[] hosts = nonProxyHosts.split("\\|");
            for (String host : hosts) {
                if (host.contains("*")) {
                    // the nonProxyHots-pattern allows wildcards '*' which must
                    // be masked to be used with regular expressions
                    String hostRegexp = host.replaceAll("\\.", "\\\\.");
                    hostRegexp = hostRegexp.replaceAll("\\*", ".*");
                    if (givenHost.matches(hostRegexp)) {
                        return false;
                    }
                } else {
                    if (givenHost.equals(host)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Factory method to create a {@link HttpMethod}-object according to the
     * given String <code>httpMethodString</code>
     *
     * @param httpMethodString the name of the {@link HttpMethod} to create
     *
     * @throws IllegalArgumentException if <code>httpMethod</code> is none of <code>GET</code>, <code>PUT</code>,
     *             <code>POST</POST> or <code>DELETE</code>
     */
    public static HttpMethod createHttpMethod(String httpMethodString) {

        if ("GET".equals(httpMethodString)) {
            return HttpMethod.GET;
        } else if ("PUT".equals(httpMethodString)) {
            return HttpMethod.PUT;
        } else if ("POST".equals(httpMethodString)) {
            return HttpMethod.POST;
        } else if ("DELETE".equals(httpMethodString)) {
            return HttpMethod.DELETE;
        } else {
            throw new IllegalArgumentException("given httpMethod '" + httpMethodString + "' is unknown");
        }
    }

    private static void startHttpClient(HttpClient client) {
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                logger.warn("Cannot start HttpClient!", e);
            }
        }
    }

    /**
     * Download the image data from an URL.
     *
     * If content type is not found in the headers, the data is scanned to determine the content type.
     *
     * @param url the URL of the image to be downloaded
     * @return a RawType object containing the image, null if the content type could not be found or the content type is
     *         not an image
     */
    public static RawType downloadImage(String url) {
        return downloadImage(url, true, -1);
    }

    /**
     * Download the image data from an URL.
     *
     * @param url the URL of the image to be downloaded
     * @param scanTypeInContent true to allow the scan of data to determine the content type if not found in the headers
     * @param maxContentLength the maximum data size in bytes to trigger the download; any negative value to ignore the
     *            data size
     * @return a RawType object containing the image, null if the content type could not be found or the content type is
     *         not an image or the data size is too big
     */
    public static RawType downloadImage(String url, boolean scanTypeInContent, long maxContentLength) {
        return downloadData(url, "image/.*", scanTypeInContent, maxContentLength);
    }

    /**
     * Download the data from an URL.
     *
     * @param url the URL of the data to be downloaded
     * @param contentTypeRegex the REGEX the content type must match; null to ignore the content type
     * @param scanTypeInContent true to allow the scan of data to determine the content type if not found in the headers
     * @param maxContentLength the maximum data size in bytes to trigger the download; any negative value to ignore the
     *            data size
     * @return a RawType object containing the downloaded data, null if the content type does not match the expected
     *         type or the data size is too big
     */
    public static RawType downloadData(String url, String contentTypeRegex, boolean scanTypeInContent,
            long maxContentLength) {

        final ProxyParams proxyParams = prepareProxyParams();

        RawType rawData = null;
        try {
            ContentResponse response = executeUrlAndGetReponse("GET", url, null, null, null, 5000,
                    proxyParams.proxyHost, proxyParams.proxyPort, proxyParams.proxyUser, proxyParams.proxyPassword,
                    proxyParams.nonProxyHosts);
            byte[] data = response.getContent();
            long length = (data == null) ? 0 : data.length;
            String mediaType = response.getMediaType();
            logger.debug("Media download response: status {} content length {} media type {} (URL {})",
                    response.getStatus(), length, mediaType, url);

            if (response.getStatus() != HttpStatus.OK_200 || length == 0) {
                logger.debug("Media download failed: unexpected return code {} (URL {})", response.getStatus(), url);
                return null;
            }

            if (maxContentLength >= 0 && length > maxContentLength) {
                logger.debug("Media download aborted: content length {} too big (URL {})", length, url);
                return null;
            }

            String contentType = mediaType;
            if (contentTypeRegex != null) {
                if ((contentType == null || contentType.isEmpty()) && scanTypeInContent) {
                    // We try to get the type from the data
                    contentType = guessContentTypeFromData(data);
                    logger.debug("Media download: content type from data: {} (URL {})", contentType, url);
                }
                if (contentType != null && contentType.isEmpty()) {
                    contentType = null;
                }
                if (contentType == null) {
                    logger.debug("Media download aborted: unknown content type (URL {})", url);
                    return null;
                } else if (!contentType.matches(contentTypeRegex)) {
                    logger.debug("Media download aborted: unexpected content type \"{}\" (URL {})", contentType, url);
                    return null;
                }
            } else if (contentType == null || contentType.isEmpty()) {
                contentType = RawType.DEFAULT_MIME_TYPE;
            }

            rawData = new RawType(data, contentType);

        } catch (IOException e) {
            rawData = null;
        }
        if (rawData == null) {
            logger.debug("Media download failed (URL {})", url);
        } else {
            logger.debug("Media downloaded: size {} type {} (URL {})", rawData.getBytes().length, rawData.getMimeType(),
                    url);
        }
        return rawData;
    }

    /**
     * Determine the content type from the data.
     *
     * @param data the data as a buffer of bytes
     *
     * @return the MIME type of the content, null if the content type could not be found
     */
    public static String guessContentTypeFromData(byte[] data) {
        String contentType = null;
        try (final ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            try {
                contentType = URLConnection.guessContentTypeFromStream(input);
                if (contentType != null && contentType.isEmpty()) {
                    contentType = null;
                }
            } catch (final IOException e) {
                logger.debug("Failed to determine content type: {}", e.getMessage());
            }
        } catch (final IOException ex) {
            // Error on closing input stream -- nothing we can do here.
        }
        return contentType;
    }

}
