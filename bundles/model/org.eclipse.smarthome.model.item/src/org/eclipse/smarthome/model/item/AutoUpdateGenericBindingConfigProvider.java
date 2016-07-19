/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider;

/**
 * <p>
 * This class can parse information from the generic binding format and provides AutoUpdate binding information from it.
 * If no binding configuration is provided <code>autoupdate</code> is evaluated to true. This means every received
 * <code>Command</code> will update its corresponding <code>State</code> by default.
 * </p>
 * <p>
 * This class registers as a {@link AutoUpdateBindingConfigProvider} service as well.
 * </p>
 *
 * <p>
 * A valid binding configuration strings looks like this:
 * <ul>
 * <li><code>{ autoupdate="false" }</li>
 * </ul>
 *
 * @author Thomas.Eichstaedt-Engelen
 * @author Kai Kreuzer - made it independent from parent abstract classes
 *
 */
public class AutoUpdateGenericBindingConfigProvider implements AutoUpdateBindingConfigProvider, BindingConfigReader {

    /** caches binding configurations. maps itemNames to {@link BindingConfig}s */
    protected Map<String, AutoUpdateBindingConfig> bindingConfigs = new ConcurrentHashMap<>(
            new WeakHashMap<String, AutoUpdateBindingConfig>());

    /**
     * stores information about the context of items. The map has this content
     * structure: context -> Set of Item names
     */
    protected Map<String, Set<String>> contextMap = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBindingType() {
        return "autoupdate";
    }

    /**
     * @{inheritDoc
     */
    @Override
    public void validateItemType(String itemType, String bindingConfig) throws BindingConfigParseException {
        // as AutoUpdate is a default binding, each binding type is valid
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processBindingConfiguration(String context, String itemType, String itemName, String bindingConfig)
            throws BindingConfigParseException {
        Set<String> itemNames = contextMap.get(context);
        if (itemNames == null) {
            itemNames = new HashSet<String>();
            contextMap.put(context, itemNames);
        }
        itemNames.add(itemName);

        AutoUpdateBindingConfig config = new AutoUpdateBindingConfig();
        parseBindingConfig(bindingConfig, config);
        addBindingConfig(itemType, itemName, config);
    }

    protected void parseBindingConfig(String bindingConfig, AutoUpdateBindingConfig config)
            throws BindingConfigParseException {
        if (StringUtils.isNotBlank(bindingConfig)) {
            try {
                config.autoupdate = Boolean.valueOf(bindingConfig.trim());
            } catch (IllegalArgumentException iae) {
                throw new BindingConfigParseException("The given parameter '" + bindingConfig.trim()
                        + "' has to be set to either 'true' or 'false'.");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean autoUpdate(String itemName) {
        AutoUpdateBindingConfig config = bindingConfigs.get(itemName);
        return config != null ? config.autoupdate : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startConfigurationUpdate(String context) {
        Set<String> itemNames = contextMap.get(context);
        if (itemNames != null) {
            for (String itemName : itemNames) {
                // we remove all binding configurations for all items
                bindingConfigs.remove(itemName);
            }
            contextMap.remove(context);
        }
    }

    @Override
    public void stopConfigurationUpdate(String context) {
    }

    protected void addBindingConfig(String itemType, String itemName, AutoUpdateBindingConfig config) {
        bindingConfigs.put(itemName, config);
    }

    /**
     * @{inheritDoc
     */
    public boolean providesBindingFor(String itemName) {
        return bindingConfigs.get(itemName) != null;
    }

    /**
     * @{inheritDoc
     */
    public boolean providesBinding() {
        return !bindingConfigs.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public Collection<String> getItemNames() {
        return new ArrayList<String>(bindingConfigs.keySet());
    }

    /**
     * This is an internal data structure to store information from the binding
     * config strings and use it to answer the requests to the AutoUpdate
     * binding provider.
     */
    static class AutoUpdateBindingConfig {
        boolean autoupdate;
    }

}
