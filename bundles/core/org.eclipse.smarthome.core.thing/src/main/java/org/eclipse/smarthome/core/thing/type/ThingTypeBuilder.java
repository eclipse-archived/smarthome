/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * A {@link ThingType} builder.
 *
 * @author Henning Treu - initial contribution
 *
 */
public class ThingTypeBuilder {

    private List<ChannelGroupDefinition> channelGroupDefinitions;
    private List<ChannelDefinition> channelDefinitions;
    private List<String> extensibleChannelTypeIds;
    private List<String> supportedBridgeTypeUIDs;
    private Map<@NonNull String, String> properties;
    private String representationProperty;
    private URI configDescriptionURI;
    private boolean listed;
    private String category;

    private String bindingId;
    private String thingTypeId;
    private String label;
    private String description;

    /**
     * Create and return a {@link ThingTypeBuilder} without any properties set, except <b>listed</b> defaults to
     * <code>true</code>.
     */
    public ThingTypeBuilder() {
        this.listed = true;
    }

    /**
     * Create this builder with all properties from the given {@link ThingType}.
     *
     * @param thingType take all properties from this {@link ThingType}.
     * @return a new {@link ThingTypeBuilder} configured with all properties from the given {@link ThingType};
     */
    public ThingTypeBuilder(ThingType thingType) {
        bindingId = thingType.getBindingId();
        thingTypeId = thingType.getUID().getId();
        label = thingType.getLabel();
        description = thingType.getDescription();
        channelGroupDefinitions = thingType.getChannelGroupDefinitions();
        channelDefinitions = thingType.getChannelDefinitions();
        extensibleChannelTypeIds = thingType.getExtensibleChannelTypeIds();
        supportedBridgeTypeUIDs = thingType.getSupportedBridgeTypeUIDs();
        properties = thingType.getProperties();
        representationProperty = thingType.getRepresentationProperty();
        configDescriptionURI = thingType.getConfigDescriptionURI();
        listed = thingType.isListed();
        category = thingType.getCategory();
    }

    /**
     * Builds and returns a new {@link ThingType} according to the given values from this builder.
     *
     * @return a new {@link ThingType} according to the given values from this builder.
     *
     * @throws IllegalStateException if <code>bindingId</code> or <code>thingTypeId</code> are not given.
     */
    public ThingType build() {
        if (StringUtils.isBlank(bindingId)) {
            throw new IllegalArgumentException("The bindingId must neither be null nor empty.");
        }
        if (StringUtils.isBlank(thingTypeId)) {
            throw new IllegalArgumentException("The thingTypeId must neither be null nor empty.");
        }

        return new ThingType(new ThingTypeUID(bindingId, thingTypeId), supportedBridgeTypeUIDs, label, description,
                category, listed, representationProperty, channelDefinitions, channelGroupDefinitions, properties,
                configDescriptionURI, extensibleChannelTypeIds);
    }

    /**
     * Builds and returns a new {@link BridgeType} according to the given values from this builder.
     *
     * @return a new {@link BridgeType} according to the given values from this builder.
     *
     * @throws IllegalStateException if <code>bindingId</code> or <code>thingTypeId</code> are not given.
     */
    public BridgeType buildBridge() {
        if (StringUtils.isBlank(bindingId)) {
            throw new IllegalArgumentException("The bindingId must neither be null nor empty.");
        }
        if (StringUtils.isBlank(thingTypeId)) {
            throw new IllegalArgumentException("The thingTypeId must neither be null nor empty.");
        }

        return new BridgeType(new ThingTypeUID(bindingId, thingTypeId), supportedBridgeTypeUIDs, label, description,
                category, listed, representationProperty, channelDefinitions, channelGroupDefinitions, properties,
                configDescriptionURI, extensibleChannelTypeIds);
    }

    public ThingTypeBuilder withBindingId(String bindingId) {
        this.bindingId = bindingId;
        return this;
    }

    public ThingTypeBuilder withThingTypeId(String thingTypeId) {
        this.thingTypeId = thingTypeId;
        return this;
    }

    public ThingTypeBuilder withThingTypeUID(ThingTypeUID uid) {
        withBindingId(uid.getBindingId()).withThingTypeId(uid.getId());
        return this;
    }

    public ThingTypeBuilder withLabel(String label) {
        this.label = label;
        return this;
    }

    public ThingTypeBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ThingTypeBuilder withCategory(String category) {
        this.category = category;
        return this;
    }

    public ThingTypeBuilder isListed(boolean listed) {
        this.listed = listed;
        return this;
    }

    public ThingTypeBuilder withRepresentationProperty(String representationProperty) {
        this.representationProperty = representationProperty;
        return this;
    }

    public ThingTypeBuilder withChannelDefinitions(List<ChannelDefinition> channelDefinitions) {
        this.channelDefinitions = channelDefinitions;
        return this;
    }

    public ThingTypeBuilder withChannelGroupDefinitions(List<@NonNull ChannelGroupDefinition> channelGroupDefinitions) {
        this.channelGroupDefinitions = channelGroupDefinitions;
        return this;
    }

    public ThingTypeBuilder withProperties(Map<@NonNull String, String> properties) {
        this.properties = properties;
        return this;
    }

    public ThingTypeBuilder withConfigDescriptionURI(URI configDescriptionURI) {
        this.configDescriptionURI = configDescriptionURI;
        return this;
    }

    public ThingTypeBuilder withExtensibleChannelTypeIds(List<@NonNull String> extensibleChannelTypeIds) {
        this.extensibleChannelTypeIds = extensibleChannelTypeIds;
        return this;
    }

    public ThingTypeBuilder withSupportedBridgeTypeUIDs(List<String> supportedBridgeTypeUIDs) {
        this.supportedBridgeTypeUIDs = supportedBridgeTypeUIDs;
        return this;
    }

}
