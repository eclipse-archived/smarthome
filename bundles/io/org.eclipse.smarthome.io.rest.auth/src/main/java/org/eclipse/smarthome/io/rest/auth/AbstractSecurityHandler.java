/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.auth;

import java.security.Principal;

import javax.ws.rs.container.ContainerRequestContext;

import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.auth.AuthenticationProvider;
import org.eclipse.smarthome.core.auth.Credentials;
import org.eclipse.smarthome.io.rest.auth.internal.SmartHomePrincipal;

import com.eclipsesource.jaxrs.provider.security.AuthenticationHandler;
import com.eclipsesource.jaxrs.provider.security.AuthorizationHandler;

/**
 * Base type for handling authentication and authorization delegated from rest/jaxrs service layer.
 *
 * @author Łukasz Dywicki - Initial contribution and API
 * @author Kai Kreuzer - Added JavaDoc
 *
 */
public abstract class AbstractSecurityHandler implements AuthenticationHandler, AuthorizationHandler {

    private AuthenticationProvider authenticationProvider;

    @Override
    public final boolean isUserInRole(Principal user, String role) {
        if (user instanceof SmartHomePrincipal) {
            return ((SmartHomePrincipal) user).getRoles().contains(role);
        }

        return false;
    }

    @Override
    public Principal authenticate(ContainerRequestContext requestContext) {
        if (authenticationProvider == null) {
            return null;
        }

        Credentials credentials = createCredentials(requestContext);
        if (credentials == null) {
            return null;
        }

        Authentication authentication = authenticationProvider.authenticate(credentials);
        if (authentication != null) {
            return new SmartHomePrincipal(authentication);
        }

        return null;
    }

    /**
     * provides the credentials from a given request context. To be implemented by child classes.
     *
     * @param requestContext the request context
     * @return the inferred credentials
     */
    protected abstract Credentials createCredentials(ContainerRequestContext requestContext);

    protected void setAuthenticationService(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    protected void unsetAuthenticationService(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = null;
    }

}
