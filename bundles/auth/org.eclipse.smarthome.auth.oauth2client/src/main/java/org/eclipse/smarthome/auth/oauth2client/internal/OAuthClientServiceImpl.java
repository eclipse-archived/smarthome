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
package org.eclipse.smarthome.auth.oauth2client.internal;

import static org.eclipse.smarthome.auth.oauth2client.internal.Keyword.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of OAuthClientService.
 *
 * It requires the following services:
 *
 * org.eclipse.smarthome.core.storage.Storage (mandatory; for storing grant tokens, access tokens and refresh tokens)
 *
 * HttpClientFactory for http connections with Jetty
 *
 * The OAuthTokens, request parameters are stored and persisted using the "Storage" service.
 * This allows the token to be automatically refreshed when needed.
 *
 * @author Michael Bock - Initial contribution
 * @author Gary Tse - Initial contribution
 *
 */
@NonNullByDefault
public class OAuthClientServiceImpl implements OAuthClientService {

    public static final int DEFAULT_TOKEN_EXPIRES_IN_BUFFER_SECOND = 10;

    private static final String EXCEPTION_MESSAGE_CLOSED = "Client service is closed";

    private transient final Logger logger = LoggerFactory.getLogger(OAuthClientServiceImpl.class);

    @NonNullByDefault({})
    private OAuthStoreHandler storeHandler;

    // Constructor params - static
    private final String handle;
    private final int tokenExpiresInBuffer;
    private final HttpClientFactory httpClientFactory;

    private PersistedParams persistedParams = new PersistedParams();

    private volatile boolean closed = false;

    private OAuthClientServiceImpl(String handle, int tokenExpiresInBuffer, HttpClientFactory httpClientFactory) {
        this.handle = handle;
        this.tokenExpiresInBuffer = tokenExpiresInBuffer;
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * It should only be used internally, thus the access is package level
     *
     * @param bundleContext Bundle Context
     * @param handle The handle produced previously from
     *            {@link org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory#createOAuthClientService}
     * @param storeHandler Storage handler
     * @param tokenExpiresInBuffer Positive integer; a small time buffer in seconds. It is used to calculate the expiry
     *            of the access tokens. This allows the access token to expire earlier than the
     *            official stated expiry time; thus prevents the caller obtaining a valid token at the time of invoke,
     *            only to find the token immediately expired.
     * @param httpClientFactory Http client factory
     * @return new instance of OAuthClientServiceImpl
     * @throws OAuthException if handle is not found in store.
     * @throws IllegalStateException if store is not available.
     */
    static OAuthClientServiceImpl getInstance(String handle, OAuthStoreHandler storeHandler, int tokenExpiresInBuffer,
            HttpClientFactory httpClientFactory) throws OAuthException {

        OAuthClientServiceImpl clientService = new OAuthClientServiceImpl(handle, tokenExpiresInBuffer,
                httpClientFactory);
        clientService.storeHandler = storeHandler;

        // Load parameters from Store
        PersistedParams persistedParamsFromStore = storeHandler.loadPersistedParams(handle);
        if (persistedParamsFromStore == null) {
            throw new OAuthException("Cannot get oauth provider parameters from handle:" + handle);
        }
        clientService.persistedParams = persistedParamsFromStore;

        return clientService;
    }

    /**
     * It should only be used internally, thus the access is package level
     *
     * @param bundleContext Bundle Context*
     * @param handle The handle produced previously from
     *            {@link org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory#createOAuthClientService}*
     * @param storeHandler Storage handler
     * @param tokenUrl Authorization provider; access token url
     * @param authorizationUrl Authorization provider; authorization url
     * @param clientId the client id
     * @param clientSecret the client secret (optional)
     * @param scope of the access, a space delimited separated list
     * @param supportsBasicAuth whether the OAuth provider supports basic authorization or the client id and client
     *            secret should be passed as form params. true - use http basic authentication, false - do not use http
     *            basic authentication, null - unknown (default to do not use).
     * @param tokenExpiresInBuffer Positive integer; a small time buffer in seconds. It is used to calculate the expiry
     *            of the access tokens. This makes the access token to expire earlier than the official stated expiry
     *            time. The mechanism prevents the caller from getting a valid token at the time of invocation, and then
     *            to find the token immediately expired.
     * @param httpClientFactory Http client factory
     * @return OAuthClientServiceImpl an instance
     */
    static OAuthClientServiceImpl getInstance(String handle, OAuthStoreHandler storeHandler, String tokenUrl,
            @Nullable String authorizationUrl, String clientId, @Nullable String clientSecret, @Nullable String scope,
            @Nullable Boolean supportsBasicAuth, int tokenExpiresInBuffer, HttpClientFactory httpClientFactory) {

        OAuthClientServiceImpl clientService = new OAuthClientServiceImpl(handle, tokenExpiresInBuffer,
                httpClientFactory);
        clientService.storeHandler = storeHandler;
        clientService.persistedParams.tokenUrl = tokenUrl;
        clientService.persistedParams.authorizationUrl = authorizationUrl;
        clientService.persistedParams.clientId = clientId;
        clientService.persistedParams.clientSecret = clientSecret;
        clientService.persistedParams.scope = scope;
        clientService.persistedParams.supportsBasicAuth = supportsBasicAuth;
        storeHandler.savePersistedParams(handle, clientService.persistedParams);

        return clientService;
    }

    @Override
    public String getAuthorizationUrl(@Nullable String redirectURI, @Nullable String scope, @Nullable String state)
            throws OAuthException {
        if (state == null) {
            persistedParams.state = createNewState();
        } else {
            persistedParams.state = state;
        }
        persistedParams.scope = scope;

        // keep it to check against redirectUri in #getAccessTokenResponseByAuthorizationCode
        persistedParams.redirectUri = redirectURI;

        String authorizationUrl = persistedParams.authorizationUrl;
        if (authorizationUrl == null) {
            throw new OAuthException("Missing authorization url");
        }
        String clientId = persistedParams.clientId;
        if (clientId == null) {
            throw new OAuthException("Missing client ID");
        }

        OAuthConnector connector = new OAuthConnector(httpClientFactory);

        return connector.getAuthorizationUrl(authorizationUrl, clientId, redirectURI, persistedParams.state, scope);
    }

    @Override
    public String extractAuthCodeFromAuthResponse(@NonNull String redirectURLwithParams) throws OAuthException {
        // parse the redirectURL
        try {
            URL redirectURLObject = new URL(redirectURLwithParams);
            UrlEncoded urlEncoded = new UrlEncoded(redirectURLObject.getQuery());

            String stateFromRedirectURL = urlEncoded.getValue(STATE, 0); // may contain multiple...
            if (stateFromRedirectURL == null) {
                if (persistedParams.state == null) {
                    // This should not happen as the state is usually set
                    return urlEncoded.getValue(CODE, 0);
                } // else
                throw new OAuthException(String.format("state from redirectURL is incorrect.  Expected: %s Found: %s",
                        persistedParams.state, stateFromRedirectURL));
            } else {
                if (stateFromRedirectURL.equals(persistedParams.state)) {
                    return urlEncoded.getValue(CODE, 0);
                } // else
                throw new OAuthException(String.format("state from redirectURL is incorrect.  Expected: %s Found: %s",
                        persistedParams.state, stateFromRedirectURL));
            }
        } catch (MalformedURLException e) {
            throw new OAuthException("Redirect URL is malformed", e);
        }
    }

    @Override
    public AccessTokenResponse getAccessTokenResponseByAuthorizationCode(String authorizationCode, String redirectURI)
            throws OAuthException, IOException, OAuthResponseException {

        if (isClosed()) {
            throw new OAuthException(EXCEPTION_MESSAGE_CLOSED);
        }

        OAuthConnector connector = new OAuthConnector(httpClientFactory);

        if (persistedParams.redirectUri != null && !persistedParams.redirectUri.equals(redirectURI)) {
            // check parameter redirectURI in #getAuthorizationUrl are the same as given
            throw new OAuthException(String.format(
                    "redirectURI should be the same from previous call #getAuthorizationUrl.  Expected: %s Found: %s",
                    persistedParams.redirectUri, redirectURI));
        }

        String tokenUrl = persistedParams.tokenUrl;
        if (tokenUrl == null) {
            throw new OAuthException("Missing token url");
        }
        String clientId = persistedParams.clientId;
        if (clientId == null) {
            throw new OAuthException("Missing client ID");
        }

        AccessTokenResponse accessTokenResponse = connector.grantTypeAuthorizationCode(tokenUrl, authorizationCode,
                clientId, persistedParams.clientSecret, redirectURI,
                Boolean.TRUE.equals(persistedParams.supportsBasicAuth));

        // store it
        storeHandler.saveAccessTokenResponse(handle, accessTokenResponse);
        return accessTokenResponse;
    }

    /**
     * Implicit Grant (RFC 6749 section 4.2) is not implemented. It is directly interacting with user-agent
     * The implicit grant is not implemented. It usually involves browser/javascript redirection flows
     * and is out of Eclipse Smarthome scope.
     */
    @Override
    public AccessTokenResponse getAccessTokenByImplicit(@Nullable String redirectURI, @Nullable String scope,
            @Nullable String state) throws OAuthException, IOException, OAuthResponseException {
        throw new UnsupportedOperationException("Implicit Grant is not implemented");
    }

    @Override
    public AccessTokenResponse getAccessTokenByResourceOwnerPasswordCredentials(String username, String password,
            @Nullable String scope) throws OAuthException, IOException, OAuthResponseException {

        if (isClosed()) {
            throw new OAuthException(EXCEPTION_MESSAGE_CLOSED);
        }

        OAuthConnector connector = new OAuthConnector(httpClientFactory);

        String tokenUrl = persistedParams.tokenUrl;
        if (tokenUrl == null) {
            throw new OAuthException("Missing token url");
        }

        AccessTokenResponse accessTokenResponse = connector.grantTypePassword(tokenUrl, username, password,
                persistedParams.clientId, persistedParams.clientSecret, scope,
                Boolean.TRUE.equals(persistedParams.supportsBasicAuth));

        // store it
        storeHandler.saveAccessTokenResponse(handle, accessTokenResponse);
        return accessTokenResponse;
    }

    @Override
    public AccessTokenResponse getAccessTokenByClientCredentials(@Nullable String scope)
            throws OAuthException, IOException, OAuthResponseException {

        if (isClosed()) {
            throw new OAuthException(EXCEPTION_MESSAGE_CLOSED);
        }

        if (persistedParams.tokenUrl == null) {
            throw new IllegalStateException("bull shit");
        }
        OAuthConnector connector = new OAuthConnector(httpClientFactory);

        String tokenUrl = persistedParams.tokenUrl;
        if (tokenUrl == null) {
            throw new OAuthException("Missing token url");
        }

        String clientId = persistedParams.clientId;
        if (clientId == null) {
            throw new OAuthException("Missing client ID");
        }

        // depending on usage, cannot guarantee every parameter is not null at the beginning
        AccessTokenResponse accessTokenResponse = connector.grantTypeClientCredentials(tokenUrl, clientId,
                persistedParams.clientSecret, scope, Boolean.TRUE.equals(persistedParams.supportsBasicAuth));

        // store it
        storeHandler.saveAccessTokenResponse(handle, accessTokenResponse);
        return accessTokenResponse;
    }

    @Override
    public AccessTokenResponse refreshToken() throws OAuthException, IOException, OAuthResponseException {

        if (isClosed()) {
            throw new OAuthException(EXCEPTION_MESSAGE_CLOSED);
        }

        AccessTokenResponse lastAccessToken;
        try {
            lastAccessToken = storeHandler.loadAccessTokenResponse(handle);
        } catch (GeneralSecurityException e) {
            throw new OAuthException("Cannot decrypt access token from store", e);
        }
        if (lastAccessToken == null) {
            throw new OAuthException(
                    "Cannot refresh token because last access token is not available from handle: " + handle);
        }
        if (lastAccessToken.getRefreshToken() == null) {
            throw new OAuthException("Cannot refresh token because last access token did not have a refresh token");
        }
        String tokenUrl = persistedParams.tokenUrl;
        if (tokenUrl == null) {
            throw new OAuthException("tokenUrl is required but null");
        }

        OAuthConnector connector = new OAuthConnector(httpClientFactory);
        AccessTokenResponse accessTokenResponse = connector.grantTypeRefreshToken(tokenUrl,
                lastAccessToken.getRefreshToken(), persistedParams.clientId, persistedParams.clientSecret,
                persistedParams.scope, Boolean.TRUE.equals(persistedParams.supportsBasicAuth));

        // store it
        storeHandler.saveAccessTokenResponse(handle, accessTokenResponse);
        return accessTokenResponse;
    }

    @Override
    public @Nullable AccessTokenResponse getAccessTokenResponse()
            throws OAuthException, IOException, OAuthResponseException {

        if (isClosed()) {
            throw new OAuthException(EXCEPTION_MESSAGE_CLOSED);
        }

        AccessTokenResponse lastAccessToken;
        try {
            lastAccessToken = storeHandler.loadAccessTokenResponse(handle);
        } catch (GeneralSecurityException e) {
            throw new OAuthException("Cannot decrypt access token from store", e);
        }
        if (lastAccessToken == null) {
            return null;
        }

        if (lastAccessToken.isExpired(LocalDateTime.now(), tokenExpiresInBuffer)
                && lastAccessToken.getRefreshToken() != null) {
            return refreshToken();
        }
        return lastAccessToken;
    }

    @Override
    public void importAccessTokenResponse(AccessTokenResponse accessTokenResponse) throws OAuthException {
        if (isClosed()) {
            throw new OAuthException(EXCEPTION_MESSAGE_CLOSED);
        }
        storeHandler.saveAccessTokenResponse(handle, accessTokenResponse);
    }

    /**
     * Access tokens have expiry times. They are given by authorization server.
     * This parameter introduces a buffer in seconds that deduct from the expires-in.
     * For example, if the expires-in = 3600 seconds ( 1hour ), then setExpiresInBuffer(60)
     * will remove 60 seconds from the expiry time. In other words, the expires-in
     * becomes 3540 ( 59 mins ) effectively.
     *
     * Calls to protected resources can reasonably assume that the token is not expired.
     *
     * @param tokenExpiresInBuffer The number of seconds to remove the expires-in. Default 0 seconds.
     */
    public void setTokenExpiresInBuffer(int tokenExpiresInBuffer) {
        this.persistedParams.tokenExpiresInBuffer = tokenExpiresInBuffer;
    }

    @Override
    public void remove() throws OAuthException {

        if (isClosed()) {
            throw new OAuthException(EXCEPTION_MESSAGE_CLOSED);
        }

        logger.debug("removing handle: {}", handle);
        storeHandler.remove(handle);
        close();
    }

    @Override
    public void close() {
        closed = true;
        storeHandler = null;

        logger.debug("closing oauth client, handle: {}", handle);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    private String createNewState() {
        return UUID.randomUUID().toString();
    }

    // Params that need to be persisted
    public static class PersistedParams {
        @Nullable
        String handle;
        @Nullable
        String tokenUrl;
        @Nullable
        String authorizationUrl;
        @Nullable
        String clientId;
        @Nullable
        String clientSecret;
        @Nullable
        String scope;
        @Nullable
        Boolean supportsBasicAuth;
        @Nullable
        String state;
        @Nullable
        String redirectUri;
        int tokenExpiresInBuffer = 60;
    }

}
