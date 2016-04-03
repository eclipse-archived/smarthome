/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.auth.dto;

import org.eclipse.smarthome.core.auth.Authentication;

/**
 * Simple transformation from {@link Authentication} to transfer object {@link AuthenticationDTO}.
 */
public class AuthenticationDTOMapper {

    /**
     * Maps authentication into DTO object.
     *
     * @param authentication Present authentication.
     * @return Authentication DTO object.
     */
    public static AuthenticationDTO map(Authentication authentication) {
        AuthenticationDTO dto = new AuthenticationDTO();
        dto.username = authentication.getUsername();
        dto.roles = authentication.getRoles();
        return dto;
    }

}
