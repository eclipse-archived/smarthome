/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.io.rest.auth.basic.internal;

import java.util.StringTokenizer;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.smarthome.core.auth.Credentials;
import org.eclipse.smarthome.core.auth.UsernamePasswordCredentials;
import org.eclipse.smarthome.io.rest.auth.AbstractSecurityHandler;

/**
 * Handler responsible for parsing basic authentication sent over standard http header.
 *
 * @author Łukasz Dywicki - Initial contribution and API
 *
 */
public class BasicSecurityHandler extends AbstractSecurityHandler {

    @Override
    protected Credentials createCredentials(ContainerRequestContext requestContext) {
        String authenticationHeader = requestContext.getHeaderString("Authorization");

        if (authenticationHeader == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(authenticationHeader, " ");
        String authType = tokenizer.nextToken();
        if (SecurityContext.BASIC_AUTH.equalsIgnoreCase(authType)) {
            String usernameAndPassword = new String(Base64.decodeBase64(tokenizer.nextToken()));

            tokenizer = new StringTokenizer(usernameAndPassword, ":");
            String username = tokenizer.nextToken();
            String password = tokenizer.nextToken();

            return new UsernamePasswordCredentials(username, password);
        }

        return null;
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }

}
