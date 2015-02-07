/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.item;

import org.eclipse.smarthome.model.item.internal.GenericItemProvider;

/**
 * This interface must be implemented by services, which can parse the generic
 * binding configuration string used in the {@link GenericItemProvider}.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface BindingConfigReader {

    /**
     * This defines the type of binding this reader will process, e.g. "knx".
     * 
     * @return the type of the binding
     */
    public String getBindingType();

    /**
     * Validates if the type of <code>item</code> is valid for this binding.
     * 
     * @param itemType the type of the item to validate
     * @param bindingConfig the config string which could be used to refine the
     *            validation
     * 
     * @throws BindingConfigParseException if the item type is
     *             invalid for this binding
     */
    public void validateItemType(String itemType, String bindingConfig) throws BindingConfigParseException;

    /**
     * This method is called by the {@link GenericItemProvider} whenever it comes
     * across a binding configuration string for an item.
     * 
     * @param context a string of the context from where this item comes from. Usually the file name of the config file
     * @param itemType the item type for which the binding config is defined
     * @param itemName the item name for which the binding config is defined
     * @param bindingConfig the configuration string that must be processed
     * 
     * @throws BindingConfigParseException if the configuration string is not valid
     */
    public void processBindingConfiguration(String context, String itemType, String itemName, String bindingConfig)
            throws BindingConfigParseException;

    /**
     * Removes all configuration information for a given context. This is usually called if a config file is reloaded,
     * so that the old values are removed, before the new ones are processed.
     * 
     * @param context the context of the configurations that should be removed
     */
    public void removeConfigurations(String context);
}
