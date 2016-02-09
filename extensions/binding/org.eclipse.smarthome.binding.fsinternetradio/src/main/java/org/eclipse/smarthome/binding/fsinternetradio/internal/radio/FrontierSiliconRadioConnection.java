/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.internal.radio;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds the http-connection and session information for controlling the radio.
 *
 * @author Rainer Ostendorf
 * @author Patrick Koenemann
 */
public class FrontierSiliconRadioConnection {

    private final Logger logger = LoggerFactory.getLogger(FrontierSiliconRadioConnection.class);

    /** Timeout for HTTP requests. */
    private final static int SOCKET_TIMEOUT = 5000; // ms

    /** Hostname of the radio. */
    private final String hostname;

    /** Port number, usually 80. */
    private final int port;

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

        final String url = "http://" + hostname + ":" + port + "/fsapi/CREATE_SESSION?pin=" + pin;

        logger.trace("opening URL:" + url);

        final HttpMethod method = new GetMethod(url);
        method.getParams().setSoTimeout(SOCKET_TIMEOUT);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {
            final int statusCode = httpClient.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                logger.debug("Communication with radio failed: " + method.getStatusLine());
                if (method.getStatusCode() == 403) {
                    throw new RuntimeException("Radio does not allow connection, maybe wrong pin?");
                }
                throw new IOException("Communication with radio failed, return code: " + statusCode);
            }

            final String responseBody = IOUtils.toString(method.getResponseBodyAsStream());
            if (!responseBody.isEmpty()) {
                logger.trace("login response: " + responseBody);
            }

            final FrontierSiliconRadioApiResult result = new FrontierSiliconRadioApiResult(responseBody);
            if (result.isStatusOk()) {
                logger.trace("login successful");
                sessionId = result.getSessionId();
                isLoggedIn = true;
                return true; // login successful :-)
            }

        } catch (HttpException he) {
            logger.debug("Fatal protocol violation: {}", he.toString());
            throw he;
        } catch (IOException ioe) {
            logger.debug("Fatal transport error: {}", ioe.toString());
            throw ioe;
        } finally {
            method.releaseConnection();
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
        for (int i = 0; i < 2; i++) {
            if (!isLoggedIn && !doLogin()) {
                continue; // not logged in and login was not successful - try again!
            }

            final String url = "http://" + hostname + ":" + port + "/fsapi/" + requestString + "?pin=" + pin + "&sid="
                    + sessionId + (params == null || params.trim().length() == 0 ? "" : "&" + params);

            logger.trace("calling url: '" + url + "'");

            final HttpMethod method = new GetMethod(url);
            method.getParams().setSoTimeout(SOCKET_TIMEOUT);
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                    new DefaultHttpMethodRetryHandler(2, false));

            try {

                final int statusCode = httpClient.executeMethod(method);
                if (statusCode != HttpStatus.SC_OK) {
                    logger.warn("Method failed: " + method.getStatusLine());
                    isLoggedIn = false;
                    method.releaseConnection();
                    continue;
                }

                final String responseBody = IOUtils.toString(method.getResponseBodyAsStream());
                if (!responseBody.isEmpty()) {
                    logger.trace("got result: " + responseBody);
                } else {
                    logger.debug("got empty result");
                    isLoggedIn = false;
                    method.releaseConnection();
                    continue;
                }

                final FrontierSiliconRadioApiResult result = new FrontierSiliconRadioApiResult(responseBody);
                if (result.isStatusOk()) {
                    return result;
                }

                isLoggedIn = false;
                method.releaseConnection();
                continue; // try again
            } catch (HttpException he) {
                logger.error("Fatal protocol violation: {}", he.toString());
                isLoggedIn = false;
                throw he;
            } catch (IOException ioe) {
                logger.error("Fatal transport error: {}", ioe.toString());
                throw ioe;
            } finally {
                method.releaseConnection();
            }
        }
        isLoggedIn = false; // 3 tries failed. log in again next time, maybe our session went invalid (radio restarted?)
        return null;
    }
}
