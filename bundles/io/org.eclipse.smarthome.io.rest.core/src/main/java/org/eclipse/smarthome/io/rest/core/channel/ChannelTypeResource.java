/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.channel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionDTO;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionDTOMapper;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionParameterDTO;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionParameterGroupDTO;
import org.eclipse.smarthome.core.auth.Role;
import org.eclipse.smarthome.core.thing.dto.ChannelTypeDTO;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.SatisfiableRESTResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Provides access to ChannelType via REST.
 *
 * @author Chris Jackson - Initial contribution
 */
@Path(ChannelTypeResource.PATH_CHANNEL_TYPES)
@RolesAllowed({ Role.ADMIN })
@Api(value = ChannelTypeResource.PATH_CHANNEL_TYPES)
public class ChannelTypeResource implements SatisfiableRESTResource {

    /** The URI path to this resource */
    public static final String PATH_CHANNEL_TYPES = "channel-types";

    private ChannelTypeRegistry channelTypeRegistry;
    private ConfigDescriptionRegistry configDescriptionRegistry;

    protected void setChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = channelTypeRegistry;
    }

    protected void unsetChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = null;
    }

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available channel types.", response = ChannelTypeDTO.class, responseContainer = "Set")
    @ApiResponses(value = @ApiResponse(code = 200, message = "OK"))
    public Response getAll(
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = HttpHeaders.ACCEPT_LANGUAGE) String language) {
        Locale locale = LocaleUtil.getLocale(language);
        Set<ChannelTypeDTO> channelTypeDTOs = convertToChannelTypeDTOs(channelTypeRegistry.getChannelTypes(locale),
                locale);
        return Response.ok(channelTypeDTOs).build();
    }

    @GET
    @Path("/{channelTypeUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets channel type by UID.", response = ChannelTypeDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Channel type with provided channelTypeUID does not exist."),
            @ApiResponse(code = 404, message = "No content") })
    public Response getByUID(@PathParam("channelTypeUID") @ApiParam(value = "channelTypeUID") String channelTypeUID,
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = HttpHeaders.ACCEPT_LANGUAGE) String language) {
        Locale locale = LocaleUtil.getLocale(language);
        ChannelType channelType = channelTypeRegistry.getChannelType(new ChannelTypeUID(channelTypeUID), locale);
        if (channelType != null) {
            return Response.ok(convertToChannelTypeDTO(channelType, locale)).build();
        } else {
            return Response.noContent().build();
        }
    }

    public Set<ChannelTypeDTO> getChannelTypeDTOs(Locale locale) {
        List<ChannelType> channelTypes = channelTypeRegistry.getChannelTypes();
        Set<ChannelTypeDTO> channelTypeDTOs = convertToChannelTypeDTOs(channelTypes, locale);
        return channelTypeDTOs;
    }

    private ChannelTypeDTO convertToChannelTypeDTO(ChannelType channelType, Locale locale) {
        final ConfigDescription configDescription;
        if (channelType.getConfigDescriptionURI() != null) {
            configDescription = this.configDescriptionRegistry
                    .getConfigDescription(channelType.getConfigDescriptionURI(), locale);
        } else {
            configDescription = null;
        }

        List<ConfigDescriptionParameterDTO> parameters;
        List<ConfigDescriptionParameterGroupDTO> parameterGroups;

        if (configDescription != null) {
            ConfigDescriptionDTO configDescriptionDTO = ConfigDescriptionDTOMapper.map(configDescription);
            parameters = configDescriptionDTO.parameters;
            parameterGroups = configDescriptionDTO.parameterGroups;
        } else {
            parameters = new ArrayList<>(0);
            parameterGroups = new ArrayList<>(0);
        }

        return new ChannelTypeDTO(channelType.getUID().toString(), channelType.getLabel(), channelType.getDescription(),
                channelType.getCategory(), channelType.getItemType(), channelType.getKind(), parameters,
                parameterGroups, channelType.getState(), channelType.getTags());
    }

    private Set<ChannelTypeDTO> convertToChannelTypeDTOs(List<ChannelType> channelTypes, Locale locale) {
        Set<ChannelTypeDTO> channelTypeDTOs = new HashSet<>();

        for (ChannelType channelType : channelTypes) {
            channelTypeDTOs.add(convertToChannelTypeDTO(channelType, locale));
        }

        return channelTypeDTOs;
    }

    @Override
    public boolean isSatisfied() {
        return channelTypeRegistry != null && configDescriptionRegistry != null;
    }
}
