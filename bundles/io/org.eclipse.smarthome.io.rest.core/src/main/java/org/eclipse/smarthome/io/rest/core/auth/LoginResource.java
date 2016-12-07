/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.auth;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.auth.AuthenticationException;
import org.eclipse.smarthome.core.auth.AuthenticationProvider;
import org.eclipse.smarthome.core.auth.Credentials;
import org.eclipse.smarthome.core.auth.dto.AuthenticationDTO;
import org.eclipse.smarthome.core.auth.dto.AuthenticationDTOMapper;
import org.eclipse.smarthome.io.rest.RESTResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Login resource which allows to obtain authentication via regular HTTP call.
 */
@Path(LoginResource.PATH_LOGIN)
@Api(value = LoginResource.PATH_LOGIN)
public class LoginResource implements RESTResource {

    /** The URI path to this resource */
    public final static String PATH_LOGIN = "login";

    private AuthenticationProvider authenticationProvider;

    protected void setAuthenticationService(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    protected void unsetAuthenticationService(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = null;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get authentication for given credentials.", response = AuthenticationDTO.class, responseContainer = "Set")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response login(@ApiParam Credentials credentials) {
        if (authenticationProvider == null) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        try {
            Authentication authentication = authenticationProvider.authenticate(credentials);
            return Response.ok(AuthenticationDTOMapper.map(authentication)).build();
        } catch (AuthenticationException e) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }
    }

}
