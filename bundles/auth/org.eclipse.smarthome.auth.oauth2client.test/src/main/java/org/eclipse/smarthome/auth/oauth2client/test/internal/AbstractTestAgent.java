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
package org.eclipse.smarthome.auth.oauth2client.test.internal;

import java.io.IOException;
import java.util.Map;

import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For testing
 *
 * @author Gary Tse - initial contribution
 *
 */
public abstract class AbstractTestAgent implements TestAgent {

    protected String TOKEN_URL;
    protected String CLIENT_ID;

    protected String AUTH_URL;
    protected String REDIRECT_URI;

    protected String CLIENT_SECRET;
    protected String USERNAME;
    protected String PASSWORD;
    protected String SCOPE;

    private static final Logger logger = LoggerFactory.getLogger(AbstractTestAgent.class);

    protected OAuthFactory oauthFactory;

    protected OAuthClientService oauthClientService;
    protected String handle;

    public void activate(Map<String, Object> properties) {
        TOKEN_URL = getProperty(properties, "TOKEN_URL");
        CLIENT_ID = getProperty(properties, "CLIENT_ID");
        AUTH_URL = getProperty(properties, "AUTH_URL");
        REDIRECT_URI = getProperty(properties, "REDIRECT_URI");
        CLIENT_SECRET = getProperty(properties, "CLIENT_SECRET");
        USERNAME = getProperty(properties, "USERNAME");
        PASSWORD = getProperty(properties, "PASSWORD");
        SCOPE = getProperty(properties, "SCOPE");
    }

    private static String getProperty(Map<String, Object> properties, String propKey) {
        Object obj = properties.get(propKey);
        if (obj == null) {
            return "";
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof String[]) {
            String[] strArr = (String[]) obj;
            if (strArr.length >= 1) {
                return strArr[0];
            } else {
                return "";
            }
        }
        return obj.toString();
    }

    public void deactivate() {
        logger.debug("{} deactivated", this.getClass().getSimpleName());
        if (handle != null) {
            oauthFactory.ungetOAuthService(handle);
        }
    }

    @Override
    public String testCreateClient() {
        logger.debug("test createClient");
        handle = oauthFactory.createOAuthClientService(TOKEN_URL, AUTH_URL, CLIENT_ID, CLIENT_SECRET, SCOPE, false);
        logger.info("new client handle: {}", handle);
        return handle;
    }

    @Override
    public OAuthClientService testGetClient(String handle) throws OAuthException {
        logger.debug("test GetClient handle: {}", handle);
        if (handle == null) {
            if (this.handle == null) {
                logger.error("Cannot get client with handle: {}", handle);
                return null;
            } else {
                logger.info("Using existing handle: {}", this.handle);
                oauthClientService = oauthFactory.getOAuthClientService(this.handle);
            }
        } else {
            this.handle = handle;
            oauthClientService = oauthFactory.getOAuthClientService(this.handle);
        }
        return oauthClientService;
    }

    @Override
    public AccessTokenResponse testGetAccessTokenByResourceOwnerPasswordCredentials()
            throws OAuthException, IOException, OAuthResponseException {
        logger.debug("test getOAuthTokenByResourceOwnerPasswordCredentials");

        if (handle == null) {
            logger.debug("Creating new oauth service");
            handle = oauthFactory.createOAuthClientService(TOKEN_URL, AUTH_URL, CLIENT_ID, CLIENT_SECRET, SCOPE, false);
        }
        logger.debug("getting oauth client by handle: {}", handle);
        oauthClientService = oauthFactory.getOAuthClientService(handle);

        AccessTokenResponse accessTokenResponse = oauthClientService
                .getAccessTokenByResourceOwnerPasswordCredentials(USERNAME, PASSWORD, SCOPE);
        logger.debug("Token: {}", accessTokenResponse);
        return accessTokenResponse;
    }

    @Override
    public AccessTokenResponse testGetAccessTokenByAuthorizationCode(String code)
            throws OAuthException, IOException, OAuthResponseException {
        return oauthClientService.getAccessTokenResponseByAuthorizationCode(code, REDIRECT_URI);
    }

    @Override
    public AccessTokenResponse testGetCachedAccessToken() throws OAuthException, IOException, OAuthResponseException {
        logger.debug("test getCachedAccessToken");
        AccessTokenResponse oldRefreshedToken = oauthClientService.getAccessTokenResponse();
        return oldRefreshedToken;
    }

    @Override
    public AccessTokenResponse testRefreshToken() throws OAuthException, IOException, OAuthResponseException {
        logger.debug("test RefreshToken");
        AccessTokenResponse newRefreshedToken = oauthClientService.refreshToken();
        return newRefreshedToken;
    }

    @Override
    public String testGetAuthorizationUrl(String state) throws OAuthException {
        logger.debug("test getAuthorizationUrl {}", state);
        String authorizationURL = oauthClientService.getAuthorizationUrl(REDIRECT_URI, SCOPE, state);
        return authorizationURL;
    }

    @Override
    public void close() {
        oauthClientService.close();
        // delibrately not set to null to test on oauthClientService
        // oauthClientService = null;
    }

    @Override
    public void delete(String handle) {
        logger.debug("Delete handle: {}", handle);
        oauthFactory.deleteServiceAndAccessToken(handle);
    }
}
