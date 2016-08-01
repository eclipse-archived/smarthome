/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusCallback;
import org.eclipse.smarthome.config.core.status.ConfigStatusProvider;
import org.eclipse.smarthome.core.thing.Thing;

/**
 * The {@link ConfigStatusThingHandler} is an extension of {@link BaseThingHandler} that implements the
 * {@link ConfigStatusProvider} interface. It provides default implementations for
 * <ul>
 * <li>{@link ConfigStatusProvider#supportsEntity(String)}</li>
 * <li>{@link ConfigStatusProvider#setConfigStatusCallback(ConfigStatusCallback)}</li>
 * </ul>
 * Furthermore it overwrites {@link ThingHandler#handleConfigurationUpdate(Map)} and
 * {@link BaseThingHandler#updateConfiguration(Configuration)} to initiate a propagation of a new
 * configuration status. So sub classes need only to provide the current configuration status by implementing
 * {@link ConfigStatusProvider#getConfigStatus()}.
 *
 * @author Thomas Höfer - Initial contribution
 * @author Chris Jackson - Add updateConfiguration override to handle status updates
 */
public abstract class ConfigStatusThingHandler extends BaseThingHandler implements ConfigStatusProvider {

    private ConfigStatusCallback configStatusCallback;

    /**
     * Creates a new instance of this class for the given {@link Thing}.
     *
     * @param thing the thing for this handler
     */
    public ConfigStatusThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public boolean supportsEntity(String entityId) {
        return getThing().getUID().getAsString().equals(entityId);
    }

    @Override
    public void setConfigStatusCallback(ConfigStatusCallback configStatusCallback) {
        this.configStatusCallback = configStatusCallback;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        if (configStatusCallback != null) {
            configStatusCallback.configUpdated(new ThingConfigStatusSource(getThing().getUID().getAsString()));
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        if (configStatusCallback != null) {
            configStatusCallback.configUpdated(new ThingConfigStatusSource(getThing().getUID().getAsString()));
        }
    }

}
