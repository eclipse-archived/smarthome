/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.auth.internal;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.auth.Authentication;

/**
 * Principal implementation which is set for all authenticated requests.
 *
 * @author Łukasz Dywicki - Initial contribution and API
 * @author Kai Kreuzer - Added JavaDoc
 *
 */
public class SmartHomePrincipal implements Principal {

    private final Authentication authentication;

    /**
     * Creates a new instance
     *
     * @param authentication authentication details for this principal
     */
    public SmartHomePrincipal(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public String getName() {
        return authentication.getUsername();
    }

    public Set<String> getRoles() {
        return new HashSet<>(authentication.getRoles());
    }

}
