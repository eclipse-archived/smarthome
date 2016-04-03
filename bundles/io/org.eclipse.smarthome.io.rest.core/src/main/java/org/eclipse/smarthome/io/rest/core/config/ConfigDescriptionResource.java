/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionDTO;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionDTOMapper;
import org.eclipse.smarthome.core.auth.Role;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.SatisfiableRESTResource;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * {@link ConfigDescriptionResource} provides access to {@link ConfigDescription}s via REST.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Chris Jackson - Modify response to use JSONResponse
 */
@Path(ConfigDescriptionResource.PATH_CONFIG_DESCRIPTIONS)
@RolesAllowed({ Role.ADMIN })
@Api(value = ConfigDescriptionResource.PATH_CONFIG_DESCRIPTIONS)
public class ConfigDescriptionResource implements SatisfiableRESTResource {

    /** The URI path to this resource */
    public static final String PATH_CONFIG_DESCRIPTIONS = "config-descriptions";

    private final class ConfigDescriptionConverter implements Function<ConfigDescription, ConfigDescriptionDTO> {
        @Override
        public ConfigDescriptionDTO apply(ConfigDescription configDescription) {
            return ConfigDescriptionDTOMapper.map(configDescription);
        }
    }

    private ConfigDescriptionRegistry configDescriptionRegistry;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available config descriptions.", response = ConfigDescriptionDTO.class, responseContainer = "List")
    @ApiResponses(value = @ApiResponse(code = 200, message = "OK"))
    public Response getAll(@HeaderParam("Accept-Language") @ApiParam(value = "Accept-Language") String language) {
        Locale locale = LocaleUtil.getLocale(language);
        Iterable<ConfigDescriptionDTO> transform = Iterables
                .transform(configDescriptionRegistry.getConfigDescriptions(locale), new ConfigDescriptionConverter());
        return Response.ok(Lists.newArrayList(transform)).build();
    }

    @GET
    @Path("/{uri}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a config description by URI.", response = ConfigDescriptionDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Invalid URI syntax"), @ApiResponse(code = 404, message = "Not found") })
    public Response getByURI(@HeaderParam("Accept-Language") @ApiParam(value = "Accept-Language") String language,
            @PathParam("uri") @ApiParam(value = "uri") String uri) {
        Locale locale = LocaleUtil.getLocale(language);
        try {
            ConfigDescription configDescription = this.configDescriptionRegistry.getConfigDescription(new URI(uri),
                    locale);
            return configDescription != null ? Response.ok(ConfigDescriptionDTOMapper.map(configDescription)).build()
                    : JSONResponse.createErrorResponse(Status.NOT_FOUND, "Configuration not found: " + uri);
        } catch (URISyntaxException e) {
            return JSONResponse.createErrorResponse(Status.BAD_REQUEST, "Exception getting confinguration description");
        }
    }

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = null;
    }

    @Override
    public boolean isSatisfied() {
        return configDescriptionRegistry != null;
    }
}
