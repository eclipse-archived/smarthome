/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.items;

import java.util.Collection;
import java.util.Map;

import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ManagedMetadataProvider;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataProvider;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main implementing class of the {@link MetadataRegistry} interface. It
 * keeps track of all declared metadata of all metadata providers.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(immediate = true, service = { MetadataRegistry.class })
public class MetadataRegistryImpl extends AbstractRegistry<Metadata, MetadataKey, MetadataProvider>
        implements MetadataRegistry, RegistryChangeListener<Item> {

    private final Logger logger = LoggerFactory.getLogger(MetadataRegistryImpl.class);
    private ItemRegistry itemRegistry;

    public MetadataRegistryImpl() {
        super(MetadataProvider.class);
    }

    @Override
    @Activate
    protected void activate(BundleContext context) {
        super.activate(context);
        itemRegistry.addRegistryChangeListener(this);
    }

    @Override
    @Deactivate
    protected void deactivate() {
        super.deactivate();
        itemRegistry.removeRegistryChangeListener(this);
    }

    @Override
    public Metadata get(MetadataKey key) {
        for (final Map.Entry<Provider<Metadata>, Collection<Metadata>> entry : elementMap.entrySet()) {
            for (final Metadata item : entry.getValue()) {
                if (item.getUID().equals(key)) {
                    return item;
                }
            }
        }
        return null;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    @Override
    protected void setEventPublisher(EventPublisher eventPublisher) {
        super.setEventPublisher(eventPublisher);
    }

    @Override
    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        super.unsetEventPublisher(eventPublisher);
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, name = "ManagedThingProvider")
    protected void setManagedProvider(ManagedMetadataProvider provider) {
        super.setManagedProvider(provider);
    }

    protected void unsetManagedProvider(ManagedMetadataProvider managedProvider) {
        super.removeManagedProvider(managedProvider);
    }

    @Override
    public void added(Item element) {
        // do nothing
    }

    @Override
    public void removed(Item element) {
        if (managedProvider != null) {
            // remove our metadata for that item
            ((ManagedMetadataProvider) managedProvider).removeItemMetadata(element.getName());
        }
    }

    @Override
    public void updated(Item oldElement, Item element) {
        // do nothing
    }
}
