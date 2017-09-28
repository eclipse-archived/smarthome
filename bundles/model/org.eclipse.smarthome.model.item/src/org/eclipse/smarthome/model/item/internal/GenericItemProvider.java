/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.item.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.AbstractProvider;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupFunction;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.items.dto.GroupFunctionDTO;
import org.eclipse.smarthome.core.items.dto.ItemDTOMapper;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionProvider;
import org.eclipse.smarthome.model.core.EventType;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.core.ModelRepositoryChangeListener;
import org.eclipse.smarthome.model.item.BindingConfigParseException;
import org.eclipse.smarthome.model.item.BindingConfigReader;
import org.eclipse.smarthome.model.items.ItemModel;
import org.eclipse.smarthome.model.items.ModelBinding;
import org.eclipse.smarthome.model.items.ModelGroupFunction;
import org.eclipse.smarthome.model.items.ModelGroupItem;
import org.eclipse.smarthome.model.items.ModelItem;
import org.eclipse.smarthome.model.items.ModelNormalItem;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemProvider implementation which computes *.items file based item configurations.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Thomas.Eichstaedt-Engelen
 */
public class GenericItemProvider extends AbstractProvider<Item>
        implements ModelRepositoryChangeListener, ItemProvider, StateDescriptionProvider {

    private final Logger logger = LoggerFactory.getLogger(GenericItemProvider.class);

    /** to keep track of all binding config readers */
    private final Map<String, BindingConfigReader> bindingConfigReaders = new HashMap<String, BindingConfigReader>();

    private ModelRepository modelRepository = null;

    private final Map<String, Collection<Item>> itemsMap = new ConcurrentHashMap<>();

    private final Collection<ItemFactory> itemFactorys = new ArrayList<ItemFactory>();

    private final Map<String, StateDescription> stateDescriptions = new ConcurrentHashMap<>();

    private Integer rank;

    protected void activate(Map<String, Object> properties) {
        Object serviceRanking = properties.get(Constants.SERVICE_RANKING);
        if (serviceRanking instanceof Integer) {
            rank = (Integer) serviceRanking;
        } else {
            rank = 0;
        }
    }

    @Override
    public Integer getRank() {
        return rank;
    }

    public void setModelRepository(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;

        // process models which are already parsed by modelRepository:
        for (String modelName : modelRepository.getAllModelNamesOfType("items")) {
            modelChanged(modelName, EventType.ADDED);
        }

        modelRepository.addModelRepositoryChangeListener(this);
    }

    public void unsetModelRepository(ModelRepository modelRepository) {
        modelRepository.removeModelRepositoryChangeListener(this);
        this.modelRepository = null;
    }

    /**
     * Add another instance of an {@link ItemFactory}. Used by Declarative Services.
     *
     * @param factory The {@link ItemFactory} to add.
     */
    public void addItemFactory(ItemFactory factory) {
        itemFactorys.add(factory);
        dispatchBindingsPerItemType(null, factory.getSupportedItemTypes());
    }

    /**
     * Removes the given {@link ItemFactory}. Used by Declarative Services.
     *
     * @param factory The {@link ItemFactory} to remove.
     */
    public void removeItemFactory(ItemFactory factory) {
        itemFactorys.remove(factory);
    }

    public void addBindingConfigReader(BindingConfigReader reader) {
        if (!bindingConfigReaders.containsKey(reader.getBindingType())) {
            bindingConfigReaders.put(reader.getBindingType(), reader);
            dispatchBindingsPerType(reader, new String[] { reader.getBindingType() });
        } else {
            logger.warn("Attempted to register a second BindingConfigReader of type '{}'."
                    + " The primaraly reader will remain active!", reader.getBindingType());
        }
    }

    public void removeBindingConfigReader(BindingConfigReader reader) {
        if (bindingConfigReaders.get(reader.getBindingType()).equals(reader)) {
            bindingConfigReaders.remove(reader.getBindingType());
        }
    }

    @Override
    public Collection<Item> getAll() {
        List<Item> items = new ArrayList<Item>();
        stateDescriptions.clear();
        for (String name : modelRepository.getAllModelNamesOfType("items")) {
            items.addAll(getItemsFromModel(name));
        }
        return items;
    }

    private Collection<Item> getItemsFromModel(String modelName) {
        logger.debug("Read items from model '{}'", modelName);

        List<Item> items = new ArrayList<Item>();
        if (modelRepository != null) {
            ItemModel model = (ItemModel) modelRepository.getModel(modelName);
            if (model != null) {
                for (ModelItem modelItem : model.getItems()) {
                    Item item = createItemFromModelItem(modelItem);
                    if (item != null) {
                        for (String groupName : modelItem.getGroups()) {
                            ((GenericItem) item).addGroupName(groupName);
                        }
                        items.add(item);
                    }
                }
            }
        }

        return items;
    }

    private void processBindingConfigsFromModel(String modelName, EventType type) {
        logger.debug("Processing binding configs for items from model '{}'", modelName);

        if (modelRepository != null) {
            ItemModel model = (ItemModel) modelRepository.getModel(modelName);
            if (model == null) {
                return;
            }

            // start binding configuration processing
            for (BindingConfigReader reader : bindingConfigReaders.values()) {
                reader.startConfigurationUpdate(modelName);
            }

            // create items and read new binding configuration
            if (!EventType.REMOVED.equals(type)) {
                for (ModelItem modelItem : model.getItems()) {
                    Item item = createItemFromModelItem(modelItem);
                    if (item != null) {
                        internalDispatchBindings(modelName, item, modelItem.getBindings());
                    }
                }
            }

            // end binding configuration processing
            for (BindingConfigReader reader : bindingConfigReaders.values()) {
                reader.stopConfigurationUpdate(modelName);
            }
        }
    }

    private Item createItemFromModelItem(ModelItem modelItem) {
        GenericItem item = null;
        if (modelItem instanceof ModelGroupItem) {
            ModelGroupItem modelGroupItem = (ModelGroupItem) modelItem;
            String baseItemType = modelGroupItem.getType();
            GenericItem baseItem = createItemOfType(baseItemType, modelGroupItem.getName());
            if (baseItem != null) {
                // if the user did not specify a function the first value of the enum in xtext (EQUAL) will be used
                ModelGroupFunction function = modelGroupItem.getFunction();
                item = applyGroupFunction(baseItem, modelGroupItem, function);
            } else {
                item = new GroupItem(modelGroupItem.getName());
            }
        } else {
            ModelNormalItem normalItem = (ModelNormalItem) modelItem;
            String itemName = normalItem.getName();
            item = createItemOfType(normalItem.getType(), itemName);
        }
        if (item != null) {
            String label = modelItem.getLabel();
            String format = StringUtils.substringBetween(label, "[", "]");
            if (format != null) {
                label = StringUtils.substringBefore(label, "[").trim();
                stateDescriptions.put(modelItem.getName(), new StateDescription(null, null, null, format, false, null));
            }
            item.setLabel(label);
            item.setCategory(modelItem.getIcon());
            assignTags(modelItem, item);
            return item;
        } else {
            return null;
        }
    }

    private void assignTags(ModelItem modelItem, GenericItem item) {
        List<String> tags = modelItem.getTags();
        for (String tag : tags) {
            item.addTag(tag);
        }
    }

    private GroupItem applyGroupFunction(GenericItem baseItem, ModelGroupItem modelGroupItem,
            ModelGroupFunction function) {
        GroupFunctionDTO dto = new GroupFunctionDTO();
        dto.name = function.getName();
        dto.params = modelGroupItem.getArgs().toArray(new String[modelGroupItem.getArgs().size()]);

        GroupFunction groupFunction = ItemDTOMapper.mapFunction(baseItem, dto);

        return new GroupItem(modelGroupItem.getName(), baseItem, groupFunction);
    }

    private void dispatchBindingsPerItemType(BindingConfigReader reader, String[] itemTypes) {
        if (modelRepository != null) {
            for (String modelName : modelRepository.getAllModelNamesOfType("items")) {
                ItemModel model = (ItemModel) modelRepository.getModel(modelName);
                if (model != null) {
                    for (ModelItem modelItem : model.getItems()) {
                        for (String itemType : itemTypes) {
                            if (itemType.equals(modelItem.getType())) {
                                Item item = createItemFromModelItem(modelItem);
                                if (item != null) {
                                    internalDispatchBindings(reader, modelName, item, modelItem.getBindings());
                                }
                            }
                        }
                    }
                } else {
                    logger.debug("Model repository returned NULL for model named '{}'", modelName);
                }
            }
        } else {
            logger.warn("ModelRepository is NULL > dispatch bindings aborted!");
        }
    }

    private void dispatchBindingsPerType(BindingConfigReader reader, String[] bindingTypes) {
        if (modelRepository != null) {
            for (String modelName : modelRepository.getAllModelNamesOfType("items")) {
                ItemModel model = (ItemModel) modelRepository.getModel(modelName);
                if (model != null) {
                    for (ModelItem modelItem : model.getItems()) {
                        for (ModelBinding modelBinding : modelItem.getBindings()) {
                            for (String bindingType : bindingTypes) {
                                if (bindingType.equals(modelBinding.getType())) {
                                    Item item = createItemFromModelItem(modelItem);
                                    if (item != null) {
                                        internalDispatchBindings(reader, modelName, item, modelItem.getBindings());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    logger.debug("Model repository returned NULL for model named '{}'", modelName);
                }
            }
        } else {
            logger.warn("ModelRepository is NULL > dispatch bindings aborted!");
        }
    }

    private void internalDispatchBindings(String modelName, Item item, EList<ModelBinding> bindings) {
        internalDispatchBindings(null, modelName, item, bindings);
    }

    private void internalDispatchBindings(BindingConfigReader reader, String modelName, Item item,
            EList<ModelBinding> bindings) {
        for (ModelBinding binding : bindings) {
            String bindingType = binding.getType();
            String config = binding.getConfiguration();

            Configuration configuration = new Configuration();
            binding.getProperties().forEach(p -> configuration.put(p.getKey(), p.getValue()));

            BindingConfigReader localReader = reader;
            if (reader == null) {
                logger.trace("Given binding config reader is null > query cache to find appropriate reader!");
                localReader = bindingConfigReaders.get(bindingType);
            } else {
                if (!localReader.getBindingType().equals(binding.getType())) {
                    logger.trace(
                            "The Readers' binding type '{}' and the Bindings' type '{}' doesn't match > continue processing next binding.",
                            localReader.getBindingType(), binding.getType());
                    continue;
                } else {
                    logger.debug("Start processing binding configuration of Item '{}' with '{}' reader.", item,
                            localReader.getClass().getSimpleName());
                }
            }

            if (localReader != null) {
                try {
                    localReader.validateItemType(item.getType(), config);
                    localReader.processBindingConfiguration(modelName, item.getType(), item.getName(), config,
                            configuration);
                } catch (BindingConfigParseException e) {
                    logger.error("Binding configuration of type '{}' of item '{}' could not be parsed correctly.",
                            bindingType, item.getName(), e);
                } catch (Exception e) {
                    // Catch badly behaving binding exceptions and continue processing
                    logger.error("Binding configuration of type '{}' of item '{}' could not be parsed correctly.",
                            bindingType, item.getName(), e);
                }
            } else {
                logger.trace("Couldn't find config reader for binding type '{}' > "
                        + "parsing binding configuration of Item '{}' aborted!", bindingType, item);
            }
        }
    }

    @Override
    public void modelChanged(String modelName, EventType type) {
        if (modelName.endsWith("items")) {
            switch (type) {
                case ADDED:
                case MODIFIED:
                    Map<String, Item> oldItems = toItemMap(itemsMap.get(modelName));
                    Map<String, Item> newItems = toItemMap(getItemsFromModel(modelName));
                    itemsMap.put(modelName, newItems.values());
                    for (Item newItem : newItems.values()) {
                        if (oldItems.containsKey(newItem.getName())) {
                            Item oldItem = oldItems.get(newItem.getName());
                            if (!oldItem.equals(newItem)) {
                                notifyListenersAboutUpdatedElement(oldItem, newItem);
                            }
                        } else {
                            notifyListenersAboutAddedElement(newItem);
                        }
                    }
                    processBindingConfigsFromModel(modelName, type);
                    for (Item oldItem : oldItems.values()) {
                        if (!newItems.containsKey(oldItem.getName())) {
                            notifyListenersAboutRemovedElement(oldItem);
                        }
                    }
                    break;
                case REMOVED:
                    processBindingConfigsFromModel(modelName, type);
                    Collection<Item> itemsFromModel = getItemsFromModel(modelName);
                    itemsMap.remove(modelName);
                    for (Item item : itemsFromModel) {
                        notifyListenersAboutRemovedElement(item);
                    }
                    break;
            }
        }
    }

    private Map<String, Item> toItemMap(Collection<Item> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Item> ret = new LinkedHashMap<>();
        for (Item item : items) {
            ret.put(item.getName(), item);
        }
        return ret;
    }

    /**
     * Creates a new item of type {@code itemType} by utilizing an appropriate {@link ItemFactory}.
     *
     * @param itemType The type to find the appropriate {@link ItemFactory} for.
     * @param itemName The name of the {@link Item} to create.
     *
     * @return An Item instance of type {@code itemType} null if no item factory for it was found.
     */
    private GenericItem createItemOfType(String itemType, String itemName) {
        if (itemType == null) {
            return null;
        }

        for (ItemFactory factory : itemFactorys) {
            GenericItem item = factory.createItem(itemType, itemName);
            if (item != null) {
                logger.trace("Created item '{}' of type '{}'", itemName, itemType);
                return item;
            }
        }

        logger.debug("Couldn't find ItemFactory for item '{}' of type '{}'", itemName, itemType);
        return null;
    }

    @Override
    public StateDescription getStateDescription(String itemName, Locale locale) {
        return stateDescriptions.get(itemName);
    }

}
