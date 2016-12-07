/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.auth.jaas.internal;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.auth.AuthenticationException;
import org.eclipse.smarthome.core.auth.AuthenticationProvider;
import org.eclipse.smarthome.core.auth.Credentials;
import org.eclipse.smarthome.core.auth.UsernamePasswordCredentials;

/**
 * Implementation of authentication provider which is backed by JAAS realm.
 *
 * Real authentication logic is embedded in login modules implemented by 3rd party, this code is just for bridging it to
 * smarthome platform.
 *
 * @author Łukasz Dywicki - Initial contribution and API
 * @author Kai Kreuzer - Removed ManagedService and used DS configuration instead
 */
public class JaasAuthenticationProvider implements AuthenticationProvider {

    private String realmName;

    @Override
    public Authentication authenticate(final Credentials credentials) {
        if (realmName == null) { // configuration is not yet ready or set
            return null;
        }

        final String name = getName(credentials);
        final char[] password = getPassword(credentials);

        try {
            LoginContext loginContext = new LoginContext(realmName, new CallbackHandler() {
                @Override
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for (Callback callback : callbacks) {
                        if (callback instanceof PasswordCallback) {
                            ((PasswordCallback) callback).setPassword(password);
                        } else if (callback instanceof NameCallback) {
                            ((NameCallback) callback).setName(name);
                        } else {
                            throw new UnsupportedCallbackException(callback);
                        }
                    }
                }
            });
            loginContext.login();

            // TODO shall we call logout method on login context?
            return getAuthentication(name, loginContext.getSubject());
        } catch (LoginException e) {
            throw new AuthenticationException("Could not obtain authentication over login context", e);
        }
    }

    private Authentication getAuthentication(String name, Subject subject) {
        return new Authentication(name, getRoles(subject.getPrincipals()));
    }

    private String[] getRoles(Set<Principal> principals) {
        String[] roles = new String[principals.size()];
        int i = 0;
        for (Principal principal : principals) {
            roles[i++] = principal.getName();
        }
        return roles;
    }

    private String getName(Credentials credentials) {
        if (credentials instanceof UsernamePasswordCredentials) {
            return ((UsernamePasswordCredentials) credentials).getUsername();
        }
        return null;
    }

    private char[] getPassword(Credentials credentials) {
        if (credentials instanceof UsernamePasswordCredentials) {
            return ((UsernamePasswordCredentials) credentials).getPassword().toCharArray();
        }
        return null;
    }

    protected void activate(Map<String, Object> properties) {
        modified(properties);
    }

    protected void deactivate(Map<String, Object> properties) {
    }

    protected void modified(Map<String, Object> properties) {
        if (properties == null) {
            realmName = null;
            return;
        }

        Object propertyValue = properties.get("realmName");
        if (propertyValue != null) {
            if (propertyValue instanceof String) {
                realmName = (String) propertyValue;
            } else {
                realmName = propertyValue.toString();
            }
        } else {
            // value could be unset, we should reset it value
            realmName = null;
        }
    }
}
