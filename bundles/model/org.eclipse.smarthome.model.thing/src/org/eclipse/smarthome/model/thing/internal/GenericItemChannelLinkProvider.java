/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.common.registry.AbstractProvider;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkProvider;
import org.eclipse.smarthome.model.core.EventType;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.core.ModelRepositoryChangeListener;
import org.eclipse.smarthome.model.item.BindingConfigParseException;
import org.eclipse.smarthome.model.item.BindingConfigReader;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * {@link GenericItemChannelLinkProvider} link items to channel by reading bindings with type "channel".
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Alex Tugarev - Added parsing of multiple Channel UIDs
 *
 */
public class GenericItemChannelLinkProvider extends AbstractProvider<ItemChannelLink> implements BindingConfigReader,
        ItemChannelLinkProvider, ModelRepositoryChangeListener {

    /** caches binding configurations. maps itemNames to {@link BindingConfig}s */
    protected Map<String, Set<ItemChannelLink>> itemChannelLinkMap = new ConcurrentHashMap<>();

    /**
     * stores information about the context of items. The map has this content
     * structure: context -> Set of Item names
     */
    protected Map<String, Set<String>> contextMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unused")
    private ModelRepository modelRepository = null;

    private Set<String> previousItemNames;

    @Override
    public String getBindingType() {
        return "channel";
    }

    @Override
    public void validateItemType(String itemType, String bindingConfig) throws BindingConfigParseException {
        // all item types are allowed
    }

    @Override
    public void processBindingConfiguration(String context, String itemType, String itemName, String bindingConfig)
            throws BindingConfigParseException {
        String[] uids = bindingConfig.split(",");
        if (uids.length == 0) {
            throw new BindingConfigParseException(
                    "At least one Channel UID should be provided: <bindingID>.<thingTypeId>.<thingId>.<channelId>");
        }
        for (String uid : uids) {
            createItemChannelLink(context, itemName, uid.trim());
        }
    }

    private void createItemChannelLink(String context, String itemName, String channelUID)
            throws BindingConfigParseException {
        ChannelUID channelUIDObject = null;
        try {
            channelUIDObject = new ChannelUID(channelUID);
        } catch (IllegalArgumentException e) {
            throw new BindingConfigParseException(e.getMessage());
        }
        ItemChannelLink itemChannelLink = new ItemChannelLink(itemName, channelUIDObject);

        Set<String> itemNames = contextMap.get(context);
        if (itemNames == null) {
            itemNames = new HashSet<>();
            contextMap.put(context, itemNames);
        }
        itemNames.add(itemName);
        if(previousItemNames != null ) {
            previousItemNames.remove(itemName);
        }
        
        Set<ItemChannelLink> links = itemChannelLinkMap.get(itemName);
        if (links == null) {
            itemChannelLinkMap.put(itemName, links = new HashSet<>());
        }
        if (!links.contains(itemChannelLink)) {
            links.add(itemChannelLink);
            notifyListenersAboutAddedElement(itemChannelLink);
        } else {
            notifyListenersAboutUpdatedElement(itemChannelLink, itemChannelLink);
        }
    }

    @Override
    public void startConfigurationUpdate(String context) {
        previousItemNames = contextMap.get(context);
    }

    @Override
    public void stopConfigurationUpdate(String context) {
        if (previousItemNames != null) {
            for (String itemName : previousItemNames) {
                // we remove all binding configurations that were not processed
                Set<ItemChannelLink> links = itemChannelLinkMap.remove(itemName);
                if (links != null) {
                    for (ItemChannelLink removedItemChannelLink : links) {
                        notifyListenersAboutRemovedElement(removedItemChannelLink);
                    }
                }
            }
            contextMap.remove(context);
        }
    }

    @Override
    public Collection<ItemChannelLink> getAll() {
        return Lists.newLinkedList(Iterables.concat(itemChannelLinkMap.values()));
    }

    public void setModelRepository(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
        modelRepository.addModelRepositoryChangeListener(this);
    }

    public void unsetModelRepository(ModelRepository modelRepository) {
        modelRepository.removeModelRepositoryChangeListener(this);
        this.modelRepository = null;
    }

    @Override
    public void modelChanged(String modelName, EventType type) {
        if (modelName.endsWith("items")) {
            switch (type) {
                case ADDED:
                    startConfigurationUpdate(modelName);
                    break;
                case MODIFIED:
                    startConfigurationUpdate(modelName);
                    break;
                case REMOVED:
                    startConfigurationUpdate(modelName);
                    stopConfigurationUpdate(modelName);
                    break;
            }
        }
    }
}
