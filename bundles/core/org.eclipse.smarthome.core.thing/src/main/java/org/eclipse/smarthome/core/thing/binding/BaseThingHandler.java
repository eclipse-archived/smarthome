/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.validation.ConfigDescriptionValidator;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * {@link BaseThingHandler} provides a base implementation for the {@link ThingHandler} interface.
 * <p>
 * The default behavior for {@link Thing} updates is to {@link #dispose()} this handler first, exchange the
 * {@link Thing} and {@link #initialize()} it again. Override the method {@link #thingUpdated(Thing)} to change the
 * default behavior.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Michael Grammling - Added dynamic configuration update
 * @author Thomas Höfer - Added thing properties and config description validation
 * @author Stefan Bußweiler - Added new thing status handling
 */
public abstract class BaseThingHandler implements ThingHandler {

    private static final String THING_HANDLER_THREADPOOL_NAME = "thingHandler";

    protected final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(THING_HANDLER_THREADPOOL_NAME);

    protected ThingRegistry thingRegistry;
    protected BundleContext bundleContext;

    protected Thing thing;

    @SuppressWarnings("rawtypes")
    private ServiceTracker thingRegistryServiceTracker;
    @SuppressWarnings("rawtypes")
    private ServiceTracker thingHandlerServiceTracker;

    private ThingHandlerCallback callback;

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing
     *            thing
     */
    public BaseThingHandler(Thing thing) {
        this.thing = thing;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setBundleContext(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        thingRegistryServiceTracker = new ServiceTracker(this.bundleContext, ThingRegistry.class.getName(), null) {
            @Override
            public Object addingService(final ServiceReference reference) {
                thingRegistry = (ThingRegistry) bundleContext.getService(reference);
                return thingRegistry;
            }

            @Override
            public void removedService(final ServiceReference reference, final Object service) {
                synchronized (BaseThingHandler.this) {
                    thingRegistry = null;
                }
            }
        };
        thingRegistryServiceTracker.open();
    }

    /**
     * This method is called after {@link BaseThingHandler#initialize()} is called. If this method will be overridden,
     * the super method must be
     * called.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void postInitialize() {
        thingHandlerServiceTracker = new ServiceTracker(this.bundleContext, ThingHandler.class.getName(), null) {
            @Override
            public Object addingService(final ServiceReference reference) {
                Object thingId = reference.getProperty(SERVICE_PROPERTY_THING_ID);
                if (thingId instanceof ThingUID && BaseThingHandler.this.thing != null) {
                    ThingUID thingUID = (ThingUID) thingId;
                    if (thingUID.equals(BaseThingHandler.this.thing.getBridgeUID())) {
                        ThingHandler thingHandler = (ThingHandler) bundleContext.getService(reference);
                        Thing thing = thingHandler.getThing();
                        if (thing instanceof Bridge) {
                            bridgeHandlerInitialized(thingHandler, (Bridge) thing);
                            return thingHandler;
                        }
                    }
                }
                return null;
            }

            @Override
            public void removedService(final ServiceReference reference, final Object service) {
                ThingHandler thingHandler = (ThingHandler) service;
                bridgeHandlerDisposed(thingHandler, (Bridge) thingHandler.getThing());
            }
        };
        thingHandlerServiceTracker.open();
    }

    public void unsetBundleContext(final BundleContext bundleContext) {
        thingRegistryServiceTracker.close();
        this.bundleContext = null;
    }

    /**
     * This method is called before {@link BaseThingHandler#dispose()} is called. If this method will be overridden, the
     * super method must be called.
     */
    public void preDispose() {
        thingHandlerServiceTracker.close();
    }

    @Override
    public void handleRemoval() {
        // can be overridden by subclasses
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        validateConfigurationParameters(configurationParameters);

        // can be overridden by subclasses
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        // reinitialize with new configuration and persist changes
        dispose();
        updateConfiguration(configuration);
        initialize();
    }

    @Override
    public void dispose() {
        // can be overridden by subclasses
    }

    @Override
    public Thing getThing() {
        return this.thing;
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        // can be overridden by subclasses
    }

    @Override
    public void initialize() {
        // can be overridden by subclasses
        // standard behavior is to set the thing to ONLINE,
        // assuming no further initialization is necessary.
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    @Override
    public void setCallback(ThingHandlerCallback thingHandlerCallback) {
        synchronized (this) {
            this.callback = thingHandlerCallback;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // can be overridden by subclasses
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // can be overridden by subclasses
    }

    /**
     * Validates the given configuration parameters against the configuration description.
     *
     * @param configurationParameters the configuration parameters to be validated
     *
     * @throws ConfigValidationException if one or more of the given configuration parameters do not match
     *             their declarations in the configuration description
     */
    protected void validateConfigurationParameters(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        ThingType thingType = TypeResolver.resolve(getThing().getThingTypeUID());
        if (thingType != null) {
            ConfigDescriptionValidator.validate(configurationParameters, thingType.getConfigDescriptionURI());
        }
    }

    /**
     * Returns the configuration of the thing.
     *
     * @return configuration of the thing
     */
    protected Configuration getConfig() {
        return getThing().getConfiguration();
    }

    /**
     * Returns the configuration of the thing and transforms it to the given
     * class.
     *
     * @param configurationClass
     *            configuration class
     * @return configuration of thing in form of the given class
     */
    protected <T> T getConfigAs(Class<T> configurationClass) {
        return getConfig().as(configurationClass);
    }

    /**
     *
     * Updates the state of the thing.
     *
     * @param channelUID
     *            unique id of the channel, which was updated
     * @param state
     *            new state
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void updateState(ChannelUID channelUID, State state) {
        synchronized (this) {
            if (this.callback != null) {
                this.callback.stateUpdated(channelUID, state);
            } else {
                throw new IllegalStateException("Could not update state, because callback is missing");
            }
        }
    }

    /**
     *
     * Updates the state of the thing. Will use the thing UID to infer the
     * unique channel UID.
     *
     * @param channel
     *            ID id of the channel, which was updated
     * @param state
     *            new state
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void updateState(String channelID, State state) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelID);
        updateState(channelUID, state);
    }

    /**
     * Sends a command for a channel of the thing.
     *
     * @param channelID
     *            id of the channel, which sends the command
     * @param command
     *            command
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void postCommand(String channelID, Command command) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelID);
        postCommand(channelUID, command);
    }

    /**
     * Sends a command for a channel of the thing.
     *
     * @param channelUID
     *            unique id of the channel, which sends the command
     * @param command
     *            command
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void postCommand(ChannelUID channelUID, Command command) {
        synchronized (this) {
            if (this.callback != null) {
                this.callback.postCommand(channelUID, command);
            } else {
                throw new IllegalStateException("Could not update state, because callback is missing");
            }
        }
    }

    /**
     * Updates the status of the thing.
     *
     * @param status the status
     * @param statusDetail the detail of the status
     * @param description the description of the status
     *
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        synchronized (this) {
            if (this.callback != null) {
                ThingStatusInfoBuilder statusBuilder = ThingStatusInfoBuilder.create(status, statusDetail);
                ThingStatusInfo statusInfo = statusBuilder.withDescription(description).build();
                this.callback.statusUpdated(this.thing, statusInfo);
            } else {
                throw new IllegalStateException("Could not update status, because callback is missing");
            }
        }
    }

    /**
     * Updates the status of the thing.
     *
     * @param status the status
     * @param statusDetail the detail of the status
     *
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        updateStatus(status, statusDetail, null);
    }

    /**
     * Updates the status of the thing. The detail of the status will be 'NONE'.
     *
     * @param status the status
     *
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void updateStatus(ThingStatus status) {
        updateStatus(status, ThingStatusDetail.NONE, null);
    }

    /**
     * Creates a thing builder, which allows to modify the thing. The method
     * {@link BaseThingHandler#updateThing(Thing)} must be called to persist the changes.
     *
     * @return {@link ThingBuilder} which builds an exact copy of the thing (not null)
     */
    protected ThingBuilder editThing() {
        return ThingBuilder.create(this.thing.getUID()).withBridge(this.thing.getBridgeUID())
                .withChannels(this.thing.getChannels()).withConfiguration(this.thing.getConfiguration());
    }

    /**
     * Informs the framework, that a thing was updated. This method must be called after the configuration or channels
     * was changed.
     *
     * @param thing
     *            thing, that was updated and should be persisted
     *
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void updateThing(Thing thing) {
        synchronized (this) {
            if (this.callback != null) {
                this.thing = thing;
                this.callback.thingUpdated(thing);
            } else {
                throw new IllegalStateException("Could not update thing, because callback is missing");
            }
        }
    }

    /**
     * Returns a copy of the configuration, that can be modified. The method
     * {@link BaseThingHandler#updateConfiguration(Configuration)} must be called to persist the configuration.
     *
     * @return copy of the thing configuration (not null)
     */
    protected Configuration editConfiguration() {
        Map<String, Object> properties = this.thing.getConfiguration().getProperties();
        return new Configuration(new HashMap<>(properties));
    }

    /**
     * Informs the framework, that the given configuration of the thing was updated.
     *
     * @param configuration
     *            configuration, that was updated and should be persisted
     *
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void updateConfiguration(Configuration configuration) {
        this.thing.getConfiguration().setProperties(configuration.getProperties());
        synchronized (this) {
            if (this.callback != null) {
                this.callback.thingUpdated(thing);
            } else {
                throw new IllegalStateException("Could not update configuration, because callback is missing");
            }
        }
    }

    /**
     * Returns a copy of the properties map, that can be modified. The method {@link
     * BaseThingHandler#updateProperties(Map<String, String> properties)} must be called to persist the properties.
     *
     * @return copy of the thing properties (not null)
     */
    protected Map<String, String> editProperties() {
        Map<String, String> properties = this.thing.getProperties();
        return new HashMap<>(properties);
    }

    /**
     * Informs the framework, that the given properties map of the thing was updated. This method performs a check, if
     * the properties were updated. If the properties did not change, the framework is not informed about changes.
     *
     * @param properties
     *            properties map, that was updated and should be persisted
     *
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void updateProperties(Map<String, String> properties) {
        boolean propertiesUpdated = false;
        for (Entry<String, String> property : properties.entrySet()) {
            String propertyName = property.getKey();
            String propertyValue = property.getValue();
            String existingPropertyValue = thing.getProperties().get(propertyName);
            if (existingPropertyValue == null || !existingPropertyValue.equals(propertyValue)) {
                this.thing.setProperty(propertyName, propertyValue);
                propertiesUpdated = true;
            }
        }
        if (propertiesUpdated) {
            synchronized (this) {
                if (this.callback != null) {
                    this.callback.thingUpdated(thing);
                } else {
                    throw new IllegalStateException("Could not update properties, because callback is missing");
                }
            }
        }
    }

    /**
     * <p>
     * Updates the given property value for the thing that is handled by this thing handler instance. The value is only
     * set for the given property name if there has not been set any value yet or if the value has been changed. If the
     * value of the property to be set is null then the property is removed.
     * </p>
     *
     * This method also informs the framework about the updated thing, which in fact will persists the changes. So, if
     * multiple properties should be changed at the same time, the {@link BaseThingHandler#editProperties()} method
     * should be used.
     *
     * @param name the name of the property to be set
     * @param value the value of the property
     */
    protected void updateProperty(String name, String value) {
        String existingPropertyValue = thing.getProperties().get(name);
        if (existingPropertyValue == null || !existingPropertyValue.equals(value)) {
            thing.setProperty(name, value);
            synchronized (this) {
                if (this.callback != null) {
                    this.callback.thingUpdated(thing);
                } else {
                    throw new IllegalStateException("Could not update properties, because callback is missing");
                }
            }
        }
    }

    /**
     * Returns the bridge of the thing.
     *
     * @return returns the bridge of the thing or null if the thing has no
     *         bridge
     */
    protected Bridge getBridge() {
        ThingUID bridgeUID = thing.getBridgeUID();
        synchronized (this) {
            if (bridgeUID != null && thingRegistry != null) {
                return (Bridge) thingRegistry.get(bridgeUID);
            } else {
                return null;
            }
        }
    }

    /**
     * Returns whether at least on item is linked for the given channel ID.
     *
     * @param channelId
     *            channel ID (must not be null)
     * @return true if at least one item is linked, false otherwise
     * @throws IllegalArgumentException
     *             if no channel with the given ID exists
     */
    protected boolean isLinked(String channelId) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            return channel.isLinked();
        } else {
            throw new IllegalArgumentException("Channel with ID '" + channelId + "' does not exists.");
        }
    }

    /**
     * This method is called, when the according {@link ThingHandler} of the
     * bridge was initialized. If the thing of this handler does not have a
     * bridge, this method is never called. This method can be overridden by
     * subclasses.
     *
     * @param thingHandler
     *            thing handler of the bridge
     * @param bridge
     *            bridge
     */
    protected void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        // can be overridden by subclasses
    }

    /**
     * This method is called, when the according {@link ThingHandler} of the
     * bridge was disposed. If the thing of this handler does not have a
     * bridge, this method is never called. This method can be overridden by
     * subclasses.
     *
     * @param thingHandler
     *            thing handler of the bridge
     * @param bridge
     *            bridge
     */
    protected void bridgeHandlerDisposed(ThingHandler thingHandler, Bridge bridge) {
        // can be overridden by subclasses
    }

}