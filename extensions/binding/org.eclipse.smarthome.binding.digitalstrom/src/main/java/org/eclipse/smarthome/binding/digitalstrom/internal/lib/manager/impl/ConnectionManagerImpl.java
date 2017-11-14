/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl;

import java.net.HttpURLConnection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.ConnectionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.HttpTransport;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl.DsAPIImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl.HttpTransportImpl;

/**
 * The {@link ConnectionManagerImpl} is the implementation of the {@link ConnectionManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class ConnectionManagerImpl implements ConnectionManager {

    private Config config;
    private ConnectionListener connListener = null;
    private HttpTransport transport;
    private String sessionToken;
    private Boolean lostConnectionState = false;
    private boolean genAppToken = true;
    private DsAPI digitalSTROMClient;

    public ConnectionManagerImpl(String hostArddress, int connectTimeout, int readTimeout, String username,
            String password, String applicationToken) {
        init(hostArddress, connectTimeout, readTimeout, username, password, applicationToken, false);
    }

    public ConnectionManagerImpl(Config config) {
        init(config, false);
    }

    public ConnectionManagerImpl(Config config, ConnectionListener connectionListener) {
        this.connListener = connectionListener;
        init(config, false);
    }

    public ConnectionManagerImpl(Config config, ConnectionListener connectionListener, boolean genAppToken) {
        this.connListener = connectionListener;
        this.genAppToken = genAppToken;
        init(config, false);
    }

    public ConnectionManagerImpl(String hostAddress, String username, String password, String applicationToken) {
        init(hostAddress, -1, -1, username, password, applicationToken, false);
    }

    public ConnectionManagerImpl(String hostAddress, String applicationToken) {
        init(hostAddress, -1, -1, null, null, applicationToken, false);
    }

    public ConnectionManagerImpl(String hostAddress, String username, String password,
            ConnectionListener connectionListener) {
        this.connListener = connectionListener;
        init(hostAddress, -1, -1, username, password, null, false);
    }

    public ConnectionManagerImpl(String hostAddress, String username, String password, String applicationToken,
            ConnectionListener connectionListener) {
        this.connListener = connectionListener;
        init(hostAddress, -1, -1, username, password, null, false);
    }

    public ConnectionManagerImpl(String hostAddress, String username, String password, boolean genAppToken) {
        this.genAppToken = genAppToken;
        init(hostAddress, -1, -1, username, password, null, false);
    }

    public ConnectionManagerImpl(String hostAddress, String username, String password, String applicationToken,
            boolean genAppToken) {
        this.genAppToken = genAppToken;
        init(hostAddress, -1, -1, username, password, applicationToken, false);
    }

    public ConnectionManagerImpl(String hostAddress, String username, String password, String applicationToken,
            boolean genAppToken, boolean aceptAllCerts) {
        this.genAppToken = genAppToken;
        init(hostAddress, -1, -1, username, password, applicationToken, aceptAllCerts);
    }

    public ConnectionManagerImpl(String hostAddress, String username, String password, String applicationToken,
            boolean genAppToken, ConnectionListener connectionListener) {
        this.connListener = connectionListener;
        this.genAppToken = genAppToken;
        init(hostAddress, -1, -1, username, password, applicationToken, false);
    }

    private void init(String hostAddress, int connectionTimeout, int readTimeout, String username, String password,
            String applicationToken, boolean aceptAllCerts) {
        config = new Config(hostAddress, username, password, applicationToken);
        if (connectionTimeout >= 0) {
            config.setConnectionTimeout(connectionTimeout);
        }
        if (readTimeout >= 0) {
            config.setReadTimeout(readTimeout);
        }
        init(config, aceptAllCerts);
    }

    private void init(Config config, boolean aceptAllCerts) {
        this.transport = new HttpTransportImpl(config, aceptAllCerts);
        this.digitalSTROMClient = new DsAPIImpl(transport);
        this.config = config;
        if (this.genAppToken) {
            this.onNotAuthenticated();
        }
    }

    @Override
    public HttpTransport getHttpTransport() {
        return transport;
    }

    @Override
    public DsAPI getDigitalSTROMAPI() {
        return this.digitalSTROMClient;
    }

    @Override
    public String getSessionToken() {
        return this.sessionToken;
    }

    @Override
    public String checkConnectionAndGetSessionToken() {
        if (checkConnection()) {
            return this.sessionToken;
        }
        return null;
    }

    @Override
    public synchronized boolean checkConnection() {
        int code = this.digitalSTROMClient.checkConnection(sessionToken);
        switch (code) {
            case HttpURLConnection.HTTP_OK:
                if (!lostConnectionState) {
                    lostConnectionState = true;
                    onConnectionResumed();
                }
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                lostConnectionState = false;
                break;
            case HttpURLConnection.HTTP_FORBIDDEN:
                if (this.genAppToken) {
                    if (StringUtils.isNotBlank(config.getAppToken())) {
                        sessionToken = this.digitalSTROMClient.loginApplication(config.getAppToken());
                    } else {
                        this.onNotAuthenticated();
                    }
                } else {
                    sessionToken = this.digitalSTROMClient.login(this.config.getUserName(), this.config.getPassword());
                }
                if (sessionToken != null) {
                    if (!lostConnectionState) {
                        onConnectionResumed();
                        lostConnectionState = true;
                    }
                } else {
                    if (this.genAppToken) {
                        onNotAuthenticated();
                    }
                    lostConnectionState = false;
                }
                break;
            case -2:
                onConnectionLost(ConnectionListener.INVALID_URL);
                lostConnectionState = false;
                break;
            case -3:
            case -4:
                onConnectionLost(ConnectionListener.CONNECTON_TIMEOUT);
                lostConnectionState = false;
                break;
            case -1:
                if (connListener != null) {
                    connListener.onConnectionStateChange(ConnectionListener.CONNECTION_LOST);
                }
                break;
            case -5:
                if (connListener != null) {
                    onConnectionLost(ConnectionListener.UNKNOWN_HOST);
                }
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
                onConnectionLost(ConnectionListener.HOST_NOT_FOUND);
                lostConnectionState = false;
                break;
        }
        return lostConnectionState;
    }

    /**
     * This method is called whenever the connection to the digitalSTROM-Server is available,
     * but requests are not allowed due to a missing or invalid authentication.
     */
    private void onNotAuthenticated() {
        String applicationToken;
        boolean isAuthenticated = false;
        if (StringUtils.isNotBlank(config.getAppToken())) {
            sessionToken = digitalSTROMClient.loginApplication(config.getAppToken());
            if (sessionToken != null) {
                isAuthenticated = true;
            } else {
                if (connListener != null) {
                    connListener.onConnectionStateChange(ConnectionListener.NOT_AUTHENTICATED,
                            ConnectionListener.WRONG_APP_TOKEN);
                    if (!checkUserPassword()) {
                        return;
                    }
                }
            }
        }
        if (checkUserPassword()) {
            if (!isAuthenticated) {
                // generate applicationToken and test host is reachable
                applicationToken = this.digitalSTROMClient.requestAppplicationToken(config.getApplicationName());
                if (StringUtils.isNotBlank(applicationToken)) {
                    // enable applicationToken
                    if (this.digitalSTROMClient.enableApplicationToken(applicationToken,
                            this.digitalSTROMClient.login(config.getUserName(), config.getPassword()))) {
                        config.setAppToken(applicationToken);
                        // this.applicationToken = applicationToken;
                        isAuthenticated = true;
                    } else {
                        if (connListener != null) {
                            connListener.onConnectionStateChange(ConnectionListener.NOT_AUTHENTICATED,
                                    ConnectionListener.WRONG_USER_OR_PASSWORD);
                        }
                    }
                }
            }
            // remove password and username, to don't store them persistently
            if (isAuthenticated) {
                config.removeUsernameAndPassword();
                if (connListener != null) {
                    connListener.onConnectionStateChange(ConnectionListener.APPLICATION_TOKEN_GENERATED);
                }
            }
        } else if (!isAuthenticated) {
            if (connListener != null) {
                connListener.onConnectionStateChange(ConnectionListener.NOT_AUTHENTICATED,
                        ConnectionListener.NO_USER_PASSWORD);
            }
        }
    }

    private boolean checkUserPassword() {
        if (StringUtils.isNotBlank(config.getUserName()) && StringUtils.isNotBlank(config.getPassword())) {
            return true;
        }
        return false;
    }

    /**
     * This method is called whenever the connection to the digitalSTROM-Server is lost.
     *
     * @param reason
     */
    private void onConnectionLost(String reason) {
        if (connListener != null) {
            connListener.onConnectionStateChange(ConnectionListener.CONNECTION_LOST, reason);
        }
    }

    /**
     * This method is called whenever the connection to the digitalSTROM-Server is resumed.
     */
    private void onConnectionResumed() {
        if (connListener != null) {
            connListener.onConnectionStateChange(ConnectionListener.CONNECTION_RESUMED);
        }
    }

    @Override
    public void registerConnectionListener(ConnectionListener listener) {
        this.connListener = listener;
    }

    @Override
    public void unregisterConnectionListener() {
        this.connListener = null;
    }

    @Override
    public String getApplicationToken() {
        return config.getAppToken();
    }

    @Override
    public boolean removeApplicationToken() {
        if (StringUtils.isNotBlank(config.getAppToken())) {
            if (checkConnection()) {
                return digitalSTROMClient.revokeToken(config.getAppToken(), getSessionToken());
            }
            return false;
        }
        return true;
    }

    @Override
    public void updateConfig(String host, String username, String password, String applicationToken) {
        init(host, -1, -1, username, password, applicationToken, false);
    }

    @Override
    public void updateConfig(Config config) {
        if (this.config != null) {
            this.config.updateConfig(config);
        } else {
            this.config = config;
        }
        init(this.config, false);
    }

    @Override
    public void configHasBeenUpdated() {
        init(this.config, false);
    }

    @Override
    public Config getConfig() {
        return this.config;
    }
}
