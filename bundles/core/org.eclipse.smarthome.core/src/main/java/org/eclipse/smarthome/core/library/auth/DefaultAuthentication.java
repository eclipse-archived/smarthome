/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.auth;

import org.eclipse.smarthome.core.auth.Authentication;

/**
 * Basic implementation of authentication type.
 */
public class DefaultAuthentication implements Authentication {

    private final String username;
    private final String[] roles;

    public DefaultAuthentication(String username, String... roles) {
        this.username = username;
        this.roles = roles;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String[] getRoles() {
        return roles;
    }

}
