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
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterGroup;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.core.LocaleUtil;
import org.eclipse.smarthome.io.rest.core.thing.beans.ChannelDefinitionBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ChannelGroupDefinitionBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ConfigDescriptionParameterBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.FilterCriteriaBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ParameterGroupBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ParameterOptionBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ThingTypeBean;

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
        Set<ThingTypeBean> thingTypeBeans = convertToThingTypeBeans(thingTypeRegistry.getThingTypes(locale), locale);
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

    public List<ConfigDescriptionParameterBean> getConfigDescriptionParameterBeans(URI configDescriptionURI,
            Locale locale) {

        ConfigDescription configDescription = configDescriptionRegistry.getConfigDescription(configDescriptionURI,
                locale);
        if (configDescription != null) {
            List<ConfigDescriptionParameterBean> configDescriptionParameterBeans = new ArrayList<>(configDescription
                    .getParameters().size());
            for (ConfigDescriptionParameter configDescriptionParameter : configDescription.getParameters()) {
                ConfigDescriptionParameterBean configDescriptionParameterBean = new ConfigDescriptionParameterBean(
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

    private List<FilterCriteriaBean> createBeansForCriteria(List<FilterCriteria> filterCriteria) {
        if (filterCriteria == null)
            return null;
        List<FilterCriteriaBean> result = new LinkedList<FilterCriteriaBean>();
        for (FilterCriteria criteria : filterCriteria) {
            result.add(new FilterCriteriaBean(criteria.getName(), criteria.getValue()));
        }
        return result;
    }

    private List<ParameterOptionBean> createBeansForOptions(List<ParameterOption> options) {
        if (options == null)
            return null;
        List<ParameterOptionBean> result = new LinkedList<ParameterOptionBean>();
        for (ParameterOption option : options) {
            result.add(new ParameterOptionBean(option.getValue(), option.getLabel()));
        }
        return result;
    }

    public Set<ThingTypeBean> getThingTypeBeans(String bindingId, Locale locale) {

        List<ThingType> thingTypes = thingTypeRegistry.getThingTypes(bindingId);
        Set<ThingTypeBean> thingTypeBeans = convertToThingTypeBeans(thingTypes, locale);
        return thingTypeBeans;
    }

    private ThingTypeBean convertToThingTypeBean(ThingType thingType, Locale locale) {
        return new ThingTypeBean(thingType.getUID().toString(), thingType.getLabel(), thingType.getDescription(),
                getConfigDescriptionParameterBeans(thingType.getConfigDescriptionURI(), locale),
                convertToChannelDefinitionBeans(thingType.getChannelDefinitions()),
                convertToChannelGroupDefinitionBeans(thingType.getChannelGroupDefinitions()),
                thingType.getSupportedBridgeTypeUIDs(), thingType.getProperties(), thingType instanceof BridgeType,
                convertToParameterGroupBeans(thingType.getConfigDescriptionURI(), locale));
    }

    private List<ChannelGroupDefinitionBean> convertToChannelGroupDefinitionBeans(
            List<ChannelGroupDefinition> channelGroupDefinitions) {
        List<ChannelGroupDefinitionBean> channelGroupDefinitionBeans = new ArrayList<>();
        for (ChannelGroupDefinition channelGroupDefinition : channelGroupDefinitions) {
            String id = channelGroupDefinition.getId();
            ChannelGroupType channelGroupType = channelGroupDefinition.getType();

            String label = channelGroupType.getLabel();
            String description = channelGroupType.getDescription();
            List<ChannelDefinition> channelDefinitions = channelGroupType.getChannelDefinitions();
            List<ChannelDefinitionBean> channelDefinitionBeans = convertToChannelDefinitionBeans(channelDefinitions);

            channelGroupDefinitionBeans.add(new ChannelGroupDefinitionBean(id, label, description,
                    channelDefinitionBeans));
        }
        return channelGroupDefinitionBeans;
    }

    private List<ChannelDefinitionBean> convertToChannelDefinitionBeans(List<ChannelDefinition> channelDefinitions) {
        List<ChannelDefinitionBean> channelDefinitionBeans = new ArrayList<>();
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            ChannelType channelType = channelDefinition.getType();
            ChannelDefinitionBean channelDefinitionBean = new ChannelDefinitionBean(channelDefinition.getId(),
                    channelType.getLabel(), channelType.getDescription(), channelType.getTags(),
                    channelType.getCategory(), channelType.getState(), channelType.isAdvanced());
            channelDefinitionBeans.add(channelDefinitionBean);
        }
        return channelDefinitionBeans;
    }

    private Set<ThingTypeBean> convertToThingTypeBeans(List<ThingType> thingTypes, Locale locale) {
        Set<ThingTypeBean> thingTypeBeans = new HashSet<>();

        for (ThingType thingType : thingTypes) {
            thingTypeBeans.add(convertToThingTypeBean(thingType, locale));
        }

        return thingTypeBeans;
    }

    private List<ParameterGroupBean> convertToParameterGroupBeans(URI configDescriptionURI, Locale locale) {

        ConfigDescription configDescription = configDescriptionRegistry.getConfigDescription(configDescriptionURI,
                locale);
        List<ParameterGroupBean> parameterGroupBeans = new ArrayList<>();
        if (configDescription != null) {

            List<ConfigDescriptionParameterGroup> parameterGroups = configDescription.getParameterGroups();
            for (ConfigDescriptionParameterGroup parameterGroup : parameterGroups) {
                parameterGroupBeans.add(new ParameterGroupBean(parameterGroup.getName(), parameterGroup.getContext(),
                        parameterGroup.isAdvanced(), parameterGroup.getLabel(), parameterGroup.getDescription()));
            }
        }

        return parameterGroupBeans;
    }

}
