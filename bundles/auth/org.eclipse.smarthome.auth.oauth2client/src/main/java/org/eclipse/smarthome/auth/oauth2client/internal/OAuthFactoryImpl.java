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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link OAuthManager}
 *
 * @author Michael Bock - Initial contribution
 * @author Gary Tse - ESH adaptation
 */
@NonNullByDefault
@Component
public class OAuthFactoryImpl implements OAuthFactory {

    private final Logger logger = LoggerFactory.getLogger(OAuthFactoryImpl.class);

    @NonNullByDefault({})
    private OAuthStoreHandler oAuthStoreHandler;
    @NonNullByDefault({})
    private HttpClientFactory httpClientFactory;

    private int tokenExpiresInBuffer = OAuthClientServiceImpl.DEFAULT_TOKEN_EXPIRES_IN_BUFFER_SECOND;

    private final Map<String, OAuthClientServiceImpl> oauthClientServiceCache = new ConcurrentHashMap<>();

    @Deactivate
    public void deactivate() {
        // close each service
        for (OAuthClientServiceImpl clientServiceImpl : oauthClientServiceCache.values()) {
            clientServiceImpl.close();
        }
        oauthClientServiceCache.clear();
    }

    @Override
    public String createOAuthClientService(String tokenUrl, @Nullable String authorizationUrl, String clientId,
            @Nullable String clientSecret, @Nullable String scope, @Nullable Boolean supportsBasicAuth) {

        String uuidHandle = UUID.randomUUID().toString();

        OAuthClientServiceImpl clientImpl = OAuthClientServiceImpl.getInstance(uuidHandle, oAuthStoreHandler, tokenUrl,
                authorizationUrl, clientId, clientSecret, scope, supportsBasicAuth, tokenExpiresInBuffer,
                httpClientFactory);

        oauthClientServiceCache.put(uuidHandle, clientImpl);
        return uuidHandle;
    }

    @Override
    public OAuthClientService getOAuthClientService(String handle) throws OAuthException {
        OAuthClientServiceImpl clientImpl = null;

        clientImpl = oauthClientServiceCache.get(handle);
        if (clientImpl == null || clientImpl.isClosed()) {
            // This happens after reboot, or client was closed without factory knowing; create a new client
            // the store has the handle/config data
            clientImpl = OAuthClientServiceImpl.getInstance(handle, oAuthStoreHandler, tokenExpiresInBuffer,
                    httpClientFactory);
            oauthClientServiceCache.put(handle, clientImpl);
        }
        return clientImpl;
    }

    @SuppressWarnings("null")
    @Override
    public void ungetOAuthService(String handle) {
        OAuthClientServiceImpl clientImpl = oauthClientServiceCache.get(handle);
        if (clientImpl == null) {
            logger.debug("{} handle not found.  Cannot unregisterOAuthServie", handle);
            return;
        }
        clientImpl.close();
        oauthClientServiceCache.remove(handle);
    }

    @Override
    public void deleteServiceAndAccessToken(String handle) {
        OAuthClientServiceImpl clientImpl = oauthClientServiceCache.get(handle);
        if (clientImpl != null) {
            try {
                clientImpl.remove();
            } catch (OAuthException e) {
                // client was already closed, does not matter
            }
            oauthClientServiceCache.remove(handle);
        }
        oAuthStoreHandler.remove(handle);
    }

    /**
     * The Store handler is mandatory, but the actual storage service is not.
     * OAuthStoreHandler will handle when storage service is missing.
     *
     * Intended static mandatory 1..1 reference
     *
     * @param oAuthStoreHandler
     */
    @Reference
    protected void setOAuthStoreHandler(OAuthStoreHandler oAuthStoreHandler) {
        this.oAuthStoreHandler = oAuthStoreHandler;
    }

    protected void unsetOAuthStoreHandler(OAuthStoreHandler oAuthStoreHandler) {
        this.oAuthStoreHandler = null;
    }

    /**
     * HttpClientFactory is mandatory, and is used as a client for
     * all http(s) communications to the OAuth providers
     *
     * @param httpClientFactory
     */
    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = null;
    }

    public int getTokenExpiresInBuffer() {
        return tokenExpiresInBuffer;
    }

    public void setTokenExpiresInBuffer(int bufferInSeconds) {
        tokenExpiresInBuffer = bufferInSeconds;
    }

}
