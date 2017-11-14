/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.auth;

/**
 * Realizations of this type are responsible for checking validity of various credentials and giving back authentication
 * which defines access scope for authenticated user or system.
 *
 * @author Łukasz Dywicki - Initial contribution and API
 *
 */
public interface AuthenticationProvider {

    /**
     * Verify given credentials and give back authentication if they are valid.
     *
     * @param credentials User credentials.
     * @return null if credentials were not valid for this provider, otherwise in case of failed authentication an
     *         AuthenticationException should be thrown
     */
    Authentication authenticate(Credentials credentials);

}
