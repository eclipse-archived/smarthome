/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.items;

import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
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
        implements MetadataRegistry {

    private final Logger logger = LoggerFactory.getLogger(MetadataRegistryImpl.class);

    private final ProviderChangeListener<Item> itemProviderChangeListener = new ProviderChangeListener<Item>() {
        @Override
        public void added(Provider<Item> provider, Item element) {
        }

        @Override
        public void removed(Provider<Item> provider, Item element) {
            if (managedProvider != null) {
                // remove our metadata for that item
                logger.debug("Item {} was removed, trying to clean up corresponding metadata", element.getUID());
                ((ManagedMetadataProvider) managedProvider).removeItemMetadata(element.getName());
            }
        }

        @Override
        public void updated(Provider<Item> provider, Item oldelement, Item element) {
        }
    };

    public MetadataRegistryImpl() {
        super(MetadataProvider.class);
    }

    @Override
    @Activate
    protected void activate(BundleContext context) {
        super.activate(context);
    }

    @Override
    @Deactivate
    protected void deactivate() {
        super.deactivate();
    }

    @Reference
    protected void setManagedItemProvider(ManagedItemProvider managedItemProvider) {
        managedItemProvider.addProviderChangeListener(itemProviderChangeListener);
    }

    protected void unsetManagedItemProvider(ManagedItemProvider managedItemProvider) {
        managedItemProvider.removeProviderChangeListener(itemProviderChangeListener);
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

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setManagedProvider(ManagedMetadataProvider provider) {
        super.setManagedProvider(provider);
    }

    protected void unsetManagedProvider(ManagedMetadataProvider managedProvider) {
        super.removeManagedProvider(managedProvider);
    }

}
