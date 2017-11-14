/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.ConnectionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.HttpTransport;

/**
 * The {@link ConnectionManager} manages the connection to a digitalSTROM-Server.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface ConnectionManager {

    /**
     * Returns the {@link HttpTransport} to execute queries or special commands on the digitalSTROM-Server.
     *
     * @return the HttpTransport
     */
    public HttpTransport getHttpTransport();

    /**
     * Returns the {@link DsAPI} to execute commands on the digitalSTROM-Server.
     *
     * @return the DsAPI
     */
    public DsAPI getDigitalSTROMAPI();

    /**
     * This method has to be called before each command to check the connection to the digitalSTROM-Server.
     * It examines the connection to the server, sets a new Session-Token, if it is expired and sets a new
     * Application-Token, if none it set at the digitalSTROM-Server. It also outputs the specific connection failure.
     *
     * @return true if the connection is established and false if not
     */
    public boolean checkConnection();

    /**
     * Returns the current Session-Token.
     *
     * @return Session-Token
     */
    public String getSessionToken();

    /**
     * Returns the auto-generated or user defined Application-Token.
     *
     * @return Application-Token
     */
    public String getApplicationToken();

    /**
     * Checks the connection with {@link #checkConnection()} and returns the current Session-Token.
     *
     * @return Session-Token
     */
    public String checkConnectionAndGetSessionToken();

    /**
     * Registers a {@link ConnectionListener} to this {@link ConnectionManager}.
     *
     * @param connectionListener
     */
    public void registerConnectionListener(ConnectionListener connectionListener);

    /**
     * Unregisters the {@link ConnectionListener} from this {@link ConnectionManager}.
     */
    public void unregisterConnectionListener();

    /**
     * Revokes the saved Application-Token from the digitalSTROM-Server and returns true if the Application-Token was
     * revoke successful, otherwise false.
     *
     * @return successful = true, otherwise false
     */
    public boolean removeApplicationToken();

    /**
     * Updates the login configuration.
     *
     * @param hostAddress
     * @param username
     * @param password
     * @param applicationToken
     */
    public void updateConfig(String hostAddress, String username, String password, String applicationToken);

    /**
     * Updates the {@link Config} with the given config.
     *
     * @param config
     */
    public void updateConfig(Config config);

    /**
     * Returns the {@link Config}.
     *
     * @return the config
     */
    public Config getConfig();

    /**
     * Informs this {@link ConnectionManager} that the {@link Config} has been updated.
     */
    public void configHasBeenUpdated();
}
