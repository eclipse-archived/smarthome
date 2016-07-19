/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener;

/**
 * The {@link ConnectionListener} is notified if the connection state of digitalSTROM-Server has changed.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface ConnectionListener {

    /* Connection-States */
    /**
     * State, if you're not authenticated on the digitalSTROM-Server.
     */
    public final String NOT_AUTHENTICATED = "notAuth";
    /**
     * State, if the connection to the digitalSTROM-Server is lost.
     */
    public final String CONNECTION_LOST = "connLost";
    /**
     * State, if the connection to the digitalSTROM-Server is resumed.
     */
    public final String CONNECTION_RESUMED = "connResumed";
    /**
     * State, if the Application-Token is generated.
     */
    public final String APPLICATION_TOKEN_GENERATED = "appGen";

    /* Not authentication reasons */
    /**
     * State, if the given Application-Token cannot be used.
     */
    public final String WRONG_APP_TOKEN = "wrongAppT";
    /**
     * State, if the given username or password cannot be used.
     */
    public final String WRONG_USER_OR_PASSWORD = "wrongUserOrPasswd";
    /**
     * State, if no username or password is set and the given application-token cannot be used or is null.
     */
    public final String NO_USER_PASSWORD = "noUserPasswd";

    /**
     * State, if the connection timed out.
     */
    public final String CONNECTON_TIMEOUT = "connTimeout";

    /**
     * State, if the host address cannot be found.
     */
    public final String HOST_NOT_FOUND = "hostNotFound";

    /**
     * State, if the host address is unknown.
     */
    public final String UNKNOWN_HOST = "unknownHost";

    /**
     * State, if the the URL is invalid.
     */
    public final String INVALID_URL = "invalideURL";

    /**
     * This method is called whenever the connection state has changed from {@link #CONNECTION_LOST}
     * to {@link #CONNECTION_RESUMED} and vice versa. It also will be called if the application-token is generated over
     * {@link APPLICATION_TOKEN_GENERATED}.
     *
     * @param newConnectionState
     */
    public void onConnectionStateChange(String newConnectionState);

    /**
     * This method is called whenever the connection state has changed to {@link #NOT_AUTHENTICATED} or
     * {@link CONNECTION_LOST}
     * and also passes the reason why. Reason can be:
     * <ul>
     * <li>{@link #WRONG_APP_TOKEN} if the given application-token can't be used.</li>
     * <li>{@link #WRONG_USER_OR_PASSWORD} if the given user name or password can't be used.</li>
     * <li>{@link #NO_USER_PASSWORD} if no user name or password is set and the given application-token can't be used.
     * <li>{@link #HOST_NOT_FOUND} if the host can't be found.
     * <li>{@link #INVALID_URL} if the the URL is invalid.
     * </li>
     * </ul>
     *
     * @param newConnectionState
     * @param reason
     */
    public void onConnectionStateChange(String newConnectionState, String reason);
}
