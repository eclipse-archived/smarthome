/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.auth;

/**
 * Credentials which represent user name and password.
 *
 * @author Łukasz Dywicki - Initial contribution and API
 * @author Kai Kreuzer - Added JavaDoc
 *
 */
public class UsernamePasswordCredentials implements Credentials {

    private final String username;
    private final String password;

    /**
     * Creates a new instance
     *
     * @param username name of the user
     * @param password password of the user
     */
    public UsernamePasswordCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Retrieves the user name
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retrieves the password
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

}
