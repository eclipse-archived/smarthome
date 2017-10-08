/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.link;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.internal.profiles.DefaultProfileFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Provider for framework config parameters on {@link ItemChannelLink}s.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@Component
public class ItemChannelLinkConfigDescriptionProvider implements ConfigDescriptionProvider {

    private static final String SCHEME = "link";

    public static final String PARAM_PROFILE = "profile";

    private final Set<ProfileAdvisor> profileAdvisors = new CopyOnWriteArraySet<>();
    private final ProfileAdvisor defaultProfileFactory = new DefaultProfileFactory();

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ItemRegistry itemRegistry;
    private ThingRegistry thingRegistry;

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        return Collections.emptySet();
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        if (SCHEME.equals(uri.getScheme())) {
            ItemChannelLink link = itemChannelLinkRegistry.get(uri.getSchemeSpecificPart());
            if (link == null) {
                return null;
            }
            Item item = itemRegistry.get(link.getItemName());
            if (item == null) {
                return null;
            }
            Thing thing = thingRegistry.get(link.getLinkedUID().getThingUID());
            if (thing == null) {
                return null;
            }
            Channel channel = thing.getChannel(link.getLinkedUID().getId());
            if (channel == null) {
                return null;
            }
            ConfigDescriptionParameter paramProfile = ConfigDescriptionParameterBuilder.create(PARAM_PROFILE, Type.TEXT)
                    .withLabel("Profile").withDescription("the profile to use").withRequired(false)
                    .withLimitToOptions(true).withOptions(getOptions(link, item, channel, locale)).build();
            return new ConfigDescription(uri, Collections.singletonList(paramProfile));
        }
        return null;
    }

    private List<ParameterOption> getOptions(ItemChannelLink link, Item item, Channel channel, Locale locale) {
        return Stream
                .concat(Stream.of(defaultProfileFactory.getApplicableProfileTypeUIDs(link, item, channel)),
                        profileAdvisors.stream().map(f -> f.getApplicableProfileTypeUIDs(link, item, channel)))
                .flatMap(c -> c.stream())
                .map(profileTypeUID -> new ParameterOption(profileTypeUID.toString(), profileTypeUID.getLabel()))
                .collect(Collectors.toList());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addProfileAdvisor(ProfileAdvisor profileAdvisor) {
        profileAdvisors.add(profileAdvisor);
    }

    public void removeProfileAdvisor(ProfileAdvisor profileAdvisor) {
        profileAdvisors.remove(profileAdvisor);
    }

    @Reference
    public void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    public void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    @Reference
    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Reference
    public void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    public void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

}
