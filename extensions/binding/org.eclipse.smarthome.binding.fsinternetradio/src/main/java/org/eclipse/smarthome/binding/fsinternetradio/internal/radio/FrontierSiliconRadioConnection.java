/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.internal.radio;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds the http-connection and session information for controlling the radio.
 *
 * @author Rainer Ostendorf
 * @author Patrick Koenemann
 * @author Svilen Valkanov - replaced Apache HttpClient with Jetty
 * @author Mihaela Memova - changed the calling of the stopHttpClient() method, fixed the hardcoded URL path, fixed the for loop condition part
 */
public class FrontierSiliconRadioConnection {

    private final Logger logger = LoggerFactory.getLogger(FrontierSiliconRadioConnection.class);

    /** Timeout for HTTP requests in ms */
    private final static int SOCKET_TIMEOUT = 5000;

    /** Hostname of the radio. */
    private final String hostname;

    /** Port number, usually 80. */
    private final int port;

    /** URL path, must begin with a slash (/) */
    private static final String path = "/fsapi";

    /** Access pin, passed upon login as GET parameter. */
    private final String pin;

    /** The session ID we get from the radio after logging in. */
    private String sessionId;

    /** http clients, store cookies, so it is kept in connection class. */
    private HttpClient httpClient = null;

    /** Flag indicating if we are successfully logged in. */
    private boolean isLoggedIn = false;

    public FrontierSiliconRadioConnection(String hostname, int port, String pin) {
        this.hostname = hostname;
        this.port = port;
        this.pin = pin;
    }

    protected void deactivate() {
        stopHttpClient(httpClient);
    }

    /**
     * Perform login/establish a new session. Uses the PIN number and when successful saves the assigned sessionID for
     * future requests.
     *
     * @return <code>true</code> if login was successful; <code>false</code> otherwise.
     * @throws IOException if communication with the radio failed, e.g. because the device is not reachable.
     */
    public boolean doLogin() throws IOException {
        isLoggedIn = false; // reset login flag

        if (httpClient == null) {
            httpClient = new HttpClient();
        }

        startHttpClient(httpClient);

        final String url = "http://" + hostname + ":" + port + path + "/CREATE_SESSION?pin=" + pin;

        logger.trace("opening URL: {}", url);

        Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);

        try {
            ContentResponse response = request.send();
            int statusCode = response.getStatus();
            if (statusCode != HttpStatus.OK_200) {
                String reason = response.getReason();
                logger.debug("Communication with radio failed: {} {}", statusCode, reason);
                if (statusCode == HttpStatus.FORBIDDEN_403) {
                    throw new RuntimeException("Radio does not allow connection, maybe wrong pin?");
                }
                throw new IOException("Communication with radio failed, return code: " + statusCode);
            }

            final String responseBody = response.getContentAsString();
            if (!responseBody.isEmpty()) {
                logger.trace("login response: {}", responseBody);
            }

            final FrontierSiliconRadioApiResult result = new FrontierSiliconRadioApiResult(responseBody);
            if (result.isStatusOk()) {
                logger.trace("login successful");
                sessionId = result.getSessionId();
                isLoggedIn = true;
                return true; // login successful :-)
            }

        } catch (Exception e) {
            logger.debug("Fatal transport error: {}", e.toString());
            throw new IOException(e);
        }

        return false; // login not successful
    }

    /**
     * Performs a request to the radio with no further parameters.
     *
     * Typically used for polling state info.
     *
     * @param REST
     *            API requestString, e.g. "GET/netRemote.sys.power"
     * @return request result
     * @throws IOException if the request failed.
     */
    public FrontierSiliconRadioApiResult doRequest(String requestString) throws IOException {
        return doRequest(requestString, null);
    }

    /**
     * Performs a request to the radio with addition parameters.
     *
     * Typically used for changing parameters.
     *
     * @param REST
     *            API requestString, e.g. "SET/netRemote.sys.power"
     * @param params
     *            , e.g. "value=1"
     * @return request result
     * @throws IOException if the request failed.
     */
    public FrontierSiliconRadioApiResult doRequest(String requestString, String params) throws IOException {

        // 3 retries upon failure
        for (int i = 0; i < 3; i++) {
            if (!isLoggedIn && !doLogin()) {
                continue; // not logged in and login was not successful - try again!
            }

            final String url = "http://" + hostname + ":" + port + path + "/" + requestString + "?pin=" + pin + "&sid="
                    + sessionId + (params == null || params.trim().length() == 0 ? "" : "&" + params);

            logger.trace("calling url: '{}'", url);

            // HttpClient can not be null, instance is created in doLogin() method
            startHttpClient(httpClient);

            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(SOCKET_TIMEOUT,
                    TimeUnit.MILLISECONDS);

            try {
                ContentResponse response = request.send();
                final int statusCode = response.getStatus();
                if (statusCode != HttpStatus.OK_200) {
                    String reason = response.getReason();
                    logger.warn("Method failed: {}  {}", statusCode, reason);
                    isLoggedIn = false;
                    continue;
                }

                final String responseBody = response.getContentAsString();
                if (!responseBody.isEmpty()) {
                    logger.trace("got result: {}", responseBody);
                } else {
                    logger.debug("got empty result");
                    isLoggedIn = false;
                    continue;
                }

                final FrontierSiliconRadioApiResult result = new FrontierSiliconRadioApiResult(responseBody);
                if (result.isStatusOk()) {
                    return result;
                }

                isLoggedIn = false;
                continue; // try again
            } catch (Exception e) {
                logger.error("Fatal transport error: {}", e.toString());
                throw new IOException(e);
            }
        }
        isLoggedIn = false; // 3 tries failed. log in again next time, maybe our session went invalid (radio restarted?)
        return null;
    }

    private void startHttpClient(HttpClient client) {
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e1) {
                logger.warn("Can not start HttpClient !", e1);
            }
        }
    }

    private void stopHttpClient(HttpClient client) {
        if (client.isStarted()) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.error("Unable to stop HttpClient !", e);
            }
        }
    }
}
