/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterGroup;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionParameterDTO;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionParameterGroupDTO;
import org.eclipse.smarthome.config.core.dto.FilterCriteriaDTO;
import org.eclipse.smarthome.config.core.dto.ParameterOptionDTO;
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

/**
 * This is a java bean that is used with JAXB to serialize things to XML or
 * JSON.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector
 * @author Thomas Höfer - Added thing and thing type properties
 * @author Chris Jackson - Added parameter groups, advanced, multipleLimit, limitToOptions
 */
@Path("thing-types")
public class ThingTypeResource implements RESTResource {

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
    public Response getAll(@HeaderParam("Accept-Language") String language) {
        Locale locale = LocaleUtil.getLocale(language);
        Set<ThingTypeDTO> thingTypeBeans = convertToThingTypeBeans(thingTypeRegistry.getThingTypes(locale), locale);
        return Response.ok(thingTypeBeans).build();
    }

    @GET
    @Path("/{thingTypeUID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByUID(@PathParam("thingTypeUID") String thingTypeUID,
            @HeaderParam("Accept-Language") String language) {
        Locale locale = LocaleUtil.getLocale(language);
        ThingType thingType = thingTypeRegistry.getThingType(new ThingTypeUID(thingTypeUID), locale);
        if (thingType != null) {
            return Response.ok(convertToThingTypeBean(thingType, locale)).build();
        } else {
            return Response.noContent().build();
        }
    }

    public List<ConfigDescriptionParameterDTO> getConfigDescriptionParameterBeans(URI configDescriptionURI,
            Locale locale) {

        ConfigDescription configDescription = configDescriptionRegistry.getConfigDescription(configDescriptionURI,
                locale);
        if (configDescription != null) {
            List<ConfigDescriptionParameterDTO> configDescriptionParameterBeans = new ArrayList<>(
                    configDescription.getParameters().size());
            for (ConfigDescriptionParameter configDescriptionParameter : configDescription.getParameters()) {
                ConfigDescriptionParameterDTO configDescriptionParameterBean = new ConfigDescriptionParameterDTO(
                        configDescriptionParameter.getName(), configDescriptionParameter.getType(),
                        configDescriptionParameter.getMinimum(), configDescriptionParameter.getMaximum(),
                        configDescriptionParameter.getStepSize(), configDescriptionParameter.getPattern(),
                        configDescriptionParameter.isRequired(), configDescriptionParameter.isReadOnly(),
                        configDescriptionParameter.isMultiple(), configDescriptionParameter.getContext(),
                        String.valueOf(configDescriptionParameter.getDefault()), configDescriptionParameter.getLabel(),
                        configDescriptionParameter.getDescription(),
                        createBeansForOptions(configDescriptionParameter.getOptions()),
                        createBeansForCriteria(configDescriptionParameter.getFilterCriteria()),
                        configDescriptionParameter.getGroupName(), configDescriptionParameter.isAdvanced(),
                        configDescriptionParameter.getLimitToOptions(), configDescriptionParameter.getMultipleLimit());
                configDescriptionParameterBeans.add(configDescriptionParameterBean);
            }
            return configDescriptionParameterBeans;
        }

        return null;
    }

    private List<FilterCriteriaDTO> createBeansForCriteria(List<FilterCriteria> filterCriteria) {
        if (filterCriteria == null)
            return null;
        List<FilterCriteriaDTO> result = new LinkedList<FilterCriteriaDTO>();
        for (FilterCriteria criteria : filterCriteria) {
            result.add(new FilterCriteriaDTO(criteria.getName(), criteria.getValue()));
        }
        return result;
    }

    private List<ParameterOptionDTO> createBeansForOptions(List<ParameterOption> options) {
        if (options == null)
            return null;
        List<ParameterOptionDTO> result = new LinkedList<ParameterOptionDTO>();
        for (ParameterOption option : options) {
            result.add(new ParameterOptionDTO(option.getValue(), option.getLabel()));
        }
        return result;
    }

    public Set<ThingTypeDTO> getThingTypeBeans(String bindingId, Locale locale) {

        List<ThingType> thingTypes = thingTypeRegistry.getThingTypes(bindingId);
        Set<ThingTypeDTO> thingTypeBeans = convertToThingTypeBeans(thingTypes, locale);
        return thingTypeBeans;
    }

    private ThingTypeDTO convertToThingTypeBean(ThingType thingType, Locale locale) {
        return new ThingTypeDTO(thingType.getUID().toString(), thingType.getLabel(), thingType.getDescription(),
                getConfigDescriptionParameterBeans(thingType.getConfigDescriptionURI(), locale),
                convertToChannelDefinitionBeans(thingType.getChannelDefinitions(), locale),
                convertToChannelGroupDefinitionBeans(thingType.getChannelGroupDefinitions(), locale),
                thingType.getSupportedBridgeTypeUIDs(), thingType.getProperties(), thingType instanceof BridgeType,
                convertToParameterGroupBeans(thingType.getConfigDescriptionURI(), locale));
    }

    private List<ChannelGroupDefinitionDTO> convertToChannelGroupDefinitionBeans(
            List<ChannelGroupDefinition> channelGroupDefinitions, Locale locale) {
        List<ChannelGroupDefinitionDTO> channelGroupDefinitionBeans = new ArrayList<>();
        for (ChannelGroupDefinition channelGroupDefinition : channelGroupDefinitions) {
            String id = channelGroupDefinition.getId();
            ChannelGroupType channelGroupType = TypeResolver.resolve(channelGroupDefinition.getTypeUID(), locale);

            String label = channelGroupType.getLabel();
            String description = channelGroupType.getDescription();
            List<ChannelDefinition> channelDefinitions = channelGroupType.getChannelDefinitions();
            List<ChannelDefinitionDTO> channelDefinitionBeans = convertToChannelDefinitionBeans(channelDefinitions,
                    locale);

            channelGroupDefinitionBeans
                    .add(new ChannelGroupDefinitionDTO(id, label, description, channelDefinitionBeans));
        }
        return channelGroupDefinitionBeans;
    }

    private List<ChannelDefinitionDTO> convertToChannelDefinitionBeans(List<ChannelDefinition> channelDefinitions,
            Locale locale) {
        List<ChannelDefinitionDTO> channelDefinitionBeans = new ArrayList<>();
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            ChannelType channelType = TypeResolver.resolve(channelDefinition.getChannelTypeUID(), locale);

            // Default to the channelDefinition label to override the channelType
            String label = channelDefinition.getLabel();
            if (label == null) {
                label = channelType.getLabel();
            }

            // Default to the channelDefinition description to override the channelType
            String description = channelDefinition.getDescription();
            if (description == null) {
                description = channelType.getDescription();
            }

            ChannelDefinitionDTO channelDefinitionBean = new ChannelDefinitionDTO(channelDefinition.getId(),
                    channelDefinition.getChannelTypeUID().toString(), label, description, channelType.getTags(),
                    channelType.getCategory(), channelType.getState(), channelType.isAdvanced(),
                    channelDefinition.getProperties());
            channelDefinitionBeans.add(channelDefinitionBean);
        }
        return channelDefinitionBeans;
    }

    private Set<ThingTypeDTO> convertToThingTypeBeans(List<ThingType> thingTypes, Locale locale) {
        Set<ThingTypeDTO> thingTypeBeans = new HashSet<>();

        for (ThingType thingType : thingTypes) {
            thingTypeBeans.add(convertToThingTypeBean(thingType, locale));
        }

        return thingTypeBeans;
    }

    private List<ConfigDescriptionParameterGroupDTO> convertToParameterGroupBeans(URI configDescriptionURI,
            Locale locale) {

        ConfigDescription configDescription = configDescriptionRegistry.getConfigDescription(configDescriptionURI,
                locale);
        List<ConfigDescriptionParameterGroupDTO> parameterGroupBeans = new ArrayList<>();
        if (configDescription != null) {

            List<ConfigDescriptionParameterGroup> parameterGroups = configDescription.getParameterGroups();
            for (ConfigDescriptionParameterGroup parameterGroup : parameterGroups) {
                parameterGroupBeans.add(new ConfigDescriptionParameterGroupDTO(parameterGroup.getName(),
                        parameterGroup.getContext(), parameterGroup.isAdvanced(), parameterGroup.getLabel(),
                        parameterGroup.getDescription()));
            }
        }

        return parameterGroupBeans;
    }

}
