/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.actions;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides static methods that can be used in automation rules
 * for sending HTTP requests
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class HTTP {

    /** Constant which represents the content type <code>application/json</code> */
    public final static String CONTENT_TYPE_JSON = "application/json";

    private static Logger logger = LoggerFactory.getLogger(HTTP.class);

    /**
     * Send out a GET-HTTP request. Errors will be logged, returned values just ignored.
     *
     * @param url the URL to be used for the GET request.
     * @return the response body or <code>NULL</code> when the request went wrong
     */
    static public String sendHttpGetRequest(String url) {
        String response = null;
        try {
            return HttpUtil.executeUrl("GET", url, 5000);
        } catch (IOException e) {
            logger.error("Fatal transport error: {}", e);
        }
        return response;
    }

    /**
     * Send out a PUT-HTTP request. Errors will be logged, returned values just ignored.
     *
     * @param url the URL to be used for the PUT request.
     * @return the response body or <code>NULL</code> when the request went wrong
     */
    static public String sendHttpPutRequest(String url) {
        String response = null;
        try {
            response = HttpUtil.executeUrl("PUT", url, 1000);
        } catch (IOException e) {
            logger.error("Fatal transport error: {}", e);
        }
        return response;
    }

    /**
     * Send out a PUT-HTTP request. Errors will be logged, returned values just ignored.
     *
     * @param url the URL to be used for the PUT request.
     * @param contentType the content type of the given <code>content</code>
     * @param content the content to be send to the given <code>url</code> or <code>null</code> if no content should be
     *            send.
     * @return the response body or <code>NULL</code> when the request went wrong
     */
    static public String sendHttpPutRequest(String url, String contentType, String content) {
        String response = null;
        try {
            response = HttpUtil.executeUrl("PUT", url, IOUtils.toInputStream(content), contentType, 1000);
        } catch (IOException e) {
            logger.error("Fatal transport error: {}", e);
        }
        return response;
    }

    /**
     * Send out a POST-HTTP request. Errors will be logged, returned values just ignored.
     *
     * @param url the URL to be used for the POST request.
     * @return the response body or <code>NULL</code> when the request went wrong
     */
    static public String sendHttpPostRequest(String url) {
        String response = null;
        try {
            response = HttpUtil.executeUrl("POST", url, 1000);
        } catch (IOException e) {
            logger.error("Fatal transport error: {}", e);
        }
        return response;
    }

    /**
     * Send out a POST-HTTP request. Errors will be logged, returned values just ignored.
     *
     * @param url the URL to be used for the POST request.
     * @param contentType the content type of the given <code>content</code>
     * @param content the content to be send to the given <code>url</code> or <code>null</code> if no content should be
     *            send.
     * @return the response body or <code>NULL</code> when the request went wrong
     */
    static public String sendHttpPostRequest(String url, String contentType, String content) {
        String response = null;
        try {
            response = HttpUtil.executeUrl("POST", url, IOUtils.toInputStream(content), contentType, 1000);
        } catch (IOException e) {
            logger.error("Fatal transport error: {}", e);
        }
        return response;
    }

    /**
     * Send out a DELETE-HTTP request. Errors will be logged, returned values just ignored.
     *
     * @param url the URL to be used for the DELETE request.
     * @return the response body or <code>NULL</code> when the request went wrong
     */
    static public String sendHttpDeleteRequest(String url) {
        String response = null;
        try {
            response = HttpUtil.executeUrl("DELETE", url, 1000);
        } catch (IOException e) {
            logger.error("Fatal transport error: {}", e);
        }
        return response;
    }

}
