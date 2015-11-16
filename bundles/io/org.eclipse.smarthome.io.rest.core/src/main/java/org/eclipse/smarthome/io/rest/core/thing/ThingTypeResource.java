/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionDTO;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionDTOMapper;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionParameterDTO;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionParameterGroupDTO;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.dto.ChannelDefinitionDTO;
import org.eclipse.smarthome.core.thing.dto.ChannelGroupDefinitionDTO;
import org.eclipse.smarthome.core.thing.dto.ThingTypeDTO;
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.RESTResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * ThingTypeResource provides access to ThingType via REST.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector
 * @author Thomas Höfer - Added thing and thing type properties
 * @author Chris Jackson - Added parameter groups, advanced, multipleLimit,
 *         limitToOptions
 * @author Yordan Zhelev - Added Swagger annotations
 */
@Path(ThingTypeResource.PATH_THINGS_TYPES)
@Api
public class ThingTypeResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_THINGS_TYPES = "thing-types";

    private ThingTypeRegistry thingTypeRegistry;
    private ConfigDescriptionRegistry configDescriptionRegistry;

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available things types.", response = ThingTypeDTO.class, responseContainer = "Set")
    @ApiResponses(value = @ApiResponse(code = 200, message = "OK") )
    public Response getAll(@HeaderParam("Accept-Language") @ApiParam(value = "Accept-Language") String language) {
        Locale locale = LocaleUtil.getLocale(language);
        Set<ThingTypeDTO> thingTypeDTOs = convertToThingTypeDTOs(thingTypeRegistry.getThingTypes(locale), locale);
        return Response.ok(thingTypeDTOs).build();
    }

    @GET
    @Path("/{thingTypeUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets thing type by UID.", response = ThingTypeDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Thing type with provided thingTypeUID does not exist."),
            @ApiResponse(code = 204, message = "No content") })
    public Response getByUID(@PathParam("thingTypeUID") @ApiParam(value = "thingTypeUID") String thingTypeUID,
            @HeaderParam("Accept-Language") @ApiParam(value = "Accept-Language") String language) {
        Locale locale = LocaleUtil.getLocale(language);
        ThingType thingType = thingTypeRegistry.getThingType(new ThingTypeUID(thingTypeUID), locale);
        if (thingType != null) {
            return Response.ok(convertToThingTypeDTO(thingType, locale)).build();
        } else {
            return Response.noContent().build();
        }
    }

    public Set<ThingTypeDTO> getThingTypeDTOs(String bindingId, Locale locale) {

        List<ThingType> thingTypes = thingTypeRegistry.getThingTypes(bindingId);
        Set<ThingTypeDTO> thingTypeDTOs = convertToThingTypeDTOs(thingTypes, locale);
        return thingTypeDTOs;
    }

    private ThingTypeDTO convertToThingTypeDTO(ThingType thingType, Locale locale) {

        ConfigDescription configDescription = this.configDescriptionRegistry
                .getConfigDescription(thingType.getConfigDescriptionURI(), locale);

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

        return new ThingTypeDTO(thingType.getUID().toString(), thingType.getLabel(), thingType.getDescription(),
                parameters, convertToChannelDefinitionDTOs(thingType.getChannelDefinitions(), locale),
                convertToChannelGroupDefinitionDTOs(thingType.getChannelGroupDefinitions(), locale),
                thingType.getSupportedBridgeTypeUIDs(), thingType.getProperties(), thingType instanceof BridgeType,
                parameterGroups);
    }

    private List<ChannelGroupDefinitionDTO> convertToChannelGroupDefinitionDTOs(
            List<ChannelGroupDefinition> channelGroupDefinitions, Locale locale) {
        List<ChannelGroupDefinitionDTO> channelGroupDefinitionDTOs = new ArrayList<>();
        for (ChannelGroupDefinition channelGroupDefinition : channelGroupDefinitions) {
            String id = channelGroupDefinition.getId();
            ChannelGroupType channelGroupType = TypeResolver.resolve(channelGroupDefinition.getTypeUID(), locale);

            String label = channelGroupType.getLabel();
            String description = channelGroupType.getDescription();
            List<ChannelDefinition> channelDefinitions = channelGroupType.getChannelDefinitions();
            List<ChannelDefinitionDTO> channelDefinitionDTOs = convertToChannelDefinitionDTOs(channelDefinitions,
                    locale);

            channelGroupDefinitionDTOs
                    .add(new ChannelGroupDefinitionDTO(id, label, description, channelDefinitionDTOs));
        }
        return channelGroupDefinitionDTOs;
    }

    private List<ChannelDefinitionDTO> convertToChannelDefinitionDTOs(List<ChannelDefinition> channelDefinitions,
            Locale locale) {
        List<ChannelDefinitionDTO> channelDefinitionDTOs = new ArrayList<>();
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            ChannelType channelType = TypeResolver.resolve(channelDefinition.getChannelTypeUID(), locale);

            // Default to the channelDefinition label to override the
            // channelType
            String label = channelDefinition.getLabel();
            if (label == null) {
                label = channelType.getLabel();
            }

            // Default to the channelDefinition description to override the
            // channelType
            String description = channelDefinition.getDescription();
            if (description == null) {
                description = channelType.getDescription();
            }

            ChannelDefinitionDTO channelDefinitionDTO = new ChannelDefinitionDTO(channelDefinition.getId(),
                    channelDefinition.getChannelTypeUID().toString(), label, description, channelType.getTags(),
                    channelType.getCategory(), channelType.getState(), channelType.isAdvanced(),
                    channelDefinition.getProperties());
            channelDefinitionDTOs.add(channelDefinitionDTO);
        }
        return channelDefinitionDTOs;
    }

    private Set<ThingTypeDTO> convertToThingTypeDTOs(List<ThingType> thingTypes, Locale locale) {
        Set<ThingTypeDTO> thingTypeDTOs = new HashSet<>();

        for (ThingType thingType : thingTypes) {
            thingTypeDTOs.add(convertToThingTypeDTO(thingType, locale));
        }

        return thingTypeDTOs;
    }

}
