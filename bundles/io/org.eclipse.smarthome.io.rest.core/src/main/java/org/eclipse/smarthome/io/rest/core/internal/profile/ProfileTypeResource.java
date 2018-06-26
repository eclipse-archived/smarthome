/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.io.rest.core.internal.profile;

import java.util.Locale;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.core.auth.Role;
import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeRegistry;
import org.eclipse.smarthome.core.thing.profiles.dto.ProfileTypeDTO;
import org.eclipse.smarthome.core.thing.profiles.dto.ProfileTypeDTOMapper;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.Stream2JSONInputStream;
import org.eclipse.smarthome.io.rest.core.internal.thing.ThingTypeResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST resource to obtain profile-types
 *
 * @author Stefan Triller - initial contribution
 *
 */
@Path(ProfileTypeResource.PATH_PROFILE_TYPES)
@RolesAllowed({ Role.ADMIN })
@Api(value = ProfileTypeResource.PATH_PROFILE_TYPES)
@Component
public class ProfileTypeResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_PROFILE_TYPES = "profile-types";

    private final Logger logger = LoggerFactory.getLogger(ThingTypeResource.class);

    private ProfileTypeRegistry profileTypeRegistry;

    @GET
    @RolesAllowed({ Role.USER })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available profile types.", response = ProfileTypeDTO.class, responseContainer = "Set")
    @ApiResponses(value = @ApiResponse(code = 200, message = "OK", response = ProfileTypeDTO.class, responseContainer = "Set"))
    public Response getAll(
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = HttpHeaders.ACCEPT_LANGUAGE) String language) {
        Locale locale = LocaleUtil.getLocale(language);
        Stream<ProfileTypeDTO> typeStream = profileTypeRegistry.getProfileTypes(locale).stream()
                .map(t -> convertToProfileTypeDTO(t, locale));
        return Response.ok(new Stream2JSONInputStream(typeStream)).build();
    }

    private ProfileTypeDTO convertToProfileTypeDTO(ProfileType profileType, Locale locale) {
        final ProfileTypeDTO profileTypeDTO = ProfileTypeDTOMapper.map(profileType);
        if (profileTypeDTO != null) {
            return profileTypeDTO;
        } else {
            logger.warn("Cannot create DTO for profileType '{}'. Skipping it.", profileTypeDTO);
        }

        return null;
    }

    @Override
    public boolean isSatisfied() {
        if (this.profileTypeRegistry == null) {
            return false;
        }
        return true;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setProfileTypeRegistry(ProfileTypeRegistry registry) {
        this.profileTypeRegistry = registry;
    }

    public void unsetProfileTypeRegistry(ProfileTypeRegistry registry) {
        this.profileTypeRegistry = null;
    }
}
