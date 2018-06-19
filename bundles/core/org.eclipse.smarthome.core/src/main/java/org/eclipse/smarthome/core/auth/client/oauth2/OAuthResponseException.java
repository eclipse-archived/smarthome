/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.auth.client.oauth2;

/**
 * This is an exception class for OAUTH specific errors. i.e. The error responses described in the
 * RFC6749. Do NOT confuse this with Java errors.
 *
 * To keep it simple, this exception class serves for both Authorization Request and Authorization Grant
 * error response
 *
 * The field names are kept exactly the same as the specification.
 * This allows the error responses to be directly deserialized from JSON.
 *
 * @author Gary Tse - initial contribution
 *
 * @see https://tools.ietf.org/html/rfc6749#section-4.1.2.1
 * @see https://tools.ietf.org/html/rfc6749#section-5.2
 */
public class OAuthResponseException extends Exception {

    private static final long serialVersionUID = -3268280125111194474L;

    /**
     * error is compulsary in OAUth error response.
     * Must be one of { invalid_request, invalid_client, invalid_grant, unauthorized_client, unsupported_grant_type,
     * invalid_scope, access_denied, unsupported_response_type, server_error, temporarily_unavailable }
     */
    private String error;
    private String errorDescription;
    private String errorUri;
    private String state;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorUri() {
        return errorUri;
    }

    public void setErrorUri(String errorUri) {
        this.errorUri = errorUri;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "OAuthResponseException [error=" + error + ", errorDescription=" + errorDescription + ", errorUri="
                + errorUri + ", state=" + state + "]";
    }

}
