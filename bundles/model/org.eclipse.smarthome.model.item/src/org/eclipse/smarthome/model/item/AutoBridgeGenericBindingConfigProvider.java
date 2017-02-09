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
import org.eclipse.smarthome.core.autobridge.AutoBridgeBindingConfigProvider;
import org.eclipse.smarthome.core.autobridge.AutoBridgeType;

/**
 * <p>
 * This class can parse information from the generic binding format and provides AutoBridge binding information from it.
 * If no binding configuration is provided <code>autobridge</code> is evaluated to "all". This means every received
 * <code>Command</code> and <code>States</code> will be bridges to all <code>Channels</code> by default.
 * </p>
 * <p>
 * This class registers as a {@link AutoBridgeBindingConfigProvider} service as well.
 * </p>
 *
 * <p>
 * A valid binding configuration strings looks like this:
 * <ul>
 * <li><code>{ autobridge="all" }</code></li> : bridge Command and States to all other Channels
 * <li><code>{ autobridge="none" }</code></li> : no bridging
 * <li><code>{ autobridge="inter" }</code></li> : bridge Command and States to all other Channels that have a different
 * Binding
 * ID
 * <li><code>{ autobridge="intra" }</code></li> : bridge Command and States to all other Channels that have a the same
 * Binding
 * ID
 * </ul>
 *
 * @author Karel Goderis - Initial Contribution
 *
 */
public class AutoBridgeGenericBindingConfigProvider implements AutoBridgeBindingConfigProvider, BindingConfigReader {

    /** caches binding configurations. maps itemNames to {@link BindingConfig}s */
    protected Map<String, AutoBridgeBindingConfig> bindingConfigs = new ConcurrentHashMap<>(
            new WeakHashMap<String, AutoBridgeBindingConfig>());

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
        return "autobridge";
    }

    /**
     * @{inheritDoc
     */
    @Override
    public void validateItemType(String itemType, String bindingConfig) throws BindingConfigParseException {
        // as AutoBridge is a default binding, each binding type is valid
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

        AutoBridgeBindingConfig config = new AutoBridgeBindingConfig();
        parseBindingConfig(bindingConfig, config);
        addBindingConfig(itemType, itemName, config);
    }

    protected void parseBindingConfig(String bindingConfig, AutoBridgeBindingConfig config)
            throws BindingConfigParseException {
        if (StringUtils.isNotBlank(bindingConfig)) {
            try {
                config.type = AutoBridgeType.valueOf(StringUtils.upperCase(bindingConfig.trim()));
            } catch (IllegalArgumentException iae) {
                throw new BindingConfigParseException("The given parameter '" + bindingConfig.trim()
                        + "' has to be set to either 'all', 'none', 'inter' or 'intra'.");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AutoBridgeType bridgeType(String itemName) {
        AutoBridgeBindingConfig config = bindingConfigs.get(itemName);
        return config != null ? config.type : null;
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

    protected void addBindingConfig(String itemType, String itemName, AutoBridgeBindingConfig config) {
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
     * config strings and use it to answer the requests to the AutoBridge
     * binding provider.
     */
    static class AutoBridgeBindingConfig {
        AutoBridgeType type;
    }

}
