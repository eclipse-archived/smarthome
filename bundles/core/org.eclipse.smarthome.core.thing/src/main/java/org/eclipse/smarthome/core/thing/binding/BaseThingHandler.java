/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.thing.util.ThingHandlerHelper;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * {@link BaseThingHandler} provides a base implementation for the {@link ThingHandler} interface.
 * <p>
 * The default behavior for {@link Thing} updates is to {@link #dispose()} this handler first, exchange the
 * {@link Thing} and {@link #initialize()} it again. Override the method {@link #thingUpdated(Thing)} to change the
 * default behavior.
 * <p>
 * It is recommended to extend this abstract base class, because it covers a lot of common logic.
 * <p>
 *
 * @author Dennis Nobel - Initial contribution
 * @author Michael Grammling - Added dynamic configuration update
 * @author Thomas Höfer - Added thing properties and config description validation
 * @author Stefan Bußweiler - Added new thing status handling, refactorings thing/bridge life cycle
 * @author Kai Kreuzer - Refactored isLinked method to not use deprecated functions anymore
 */
public abstract class BaseThingHandler implements ThingHandler {

    private static final String THING_HANDLER_THREADPOOL_NAME = "thingHandler";
    private final Logger logger = LoggerFactory.getLogger(BaseThingHandler.class);

    protected final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(THING_HANDLER_THREADPOOL_NAME);

    protected ThingRegistry thingRegistry;
    protected ItemChannelLinkRegistry linkRegistry;
    protected BundleContext bundleContext;

    protected Thing thing;

    @SuppressWarnings("rawtypes")
    private ServiceTracker thingRegistryServiceTracker;
    @SuppressWarnings("rawtypes")
    private ServiceTracker linkRegistryServiceTracker;

    private ThingHandlerCallback callback;

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     *
     * @throws IllegalArgumentException if thing argument is null
     */
    public BaseThingHandler(Thing thing) {
        Preconditions.checkArgument(thing != null, "The argument 'thing' must not be null.");
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
        linkRegistryServiceTracker = new ServiceTracker(this.bundleContext, ItemChannelLinkRegistry.class.getName(),
                null) {
            @Override
            public Object addingService(final ServiceReference reference) {
                linkRegistry = (ItemChannelLinkRegistry) bundleContext.getService(reference);
                return linkRegistry;
            }

            @Override
            public void removedService(final ServiceReference reference, final Object service) {
                synchronized (BaseThingHandler.this) {
                    linkRegistry = null;
                }
            }
        };
        linkRegistryServiceTracker.open();
    }

    public void unsetBundleContext(final BundleContext bundleContext) {
        linkRegistryServiceTracker.close();
        thingRegistryServiceTracker.close();
        this.bundleContext = null;
    }

    @Override
    public void handleRemoval() {
        // can be overridden by subclasses
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        validateConfigurationParameters(configurationParameters);

        // can be overridden by subclasses
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        if (isInitialized()) {
            // persist new configuration and reinitialize handler
            dispose();
            updateConfiguration(configuration);
            initialize();
        } else {
            // persist new configuration and notify Thing Manager
            updateConfiguration(configuration);
            callback.configurationUpdated(getThing());
        }
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
        // standard behavior is to refresh the linked channel,
        // so the newly linked items will receive a state update.
        handleCommand(channelUID, RefreshType.REFRESH);
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
    protected void validateConfigurationParameters(Map<String, Object> configurationParameters) {
        ThingType thingType = TypeResolver.resolve(getThing().getThingTypeUID());
        if (thingType != null && thingType.getConfigDescriptionURI() != null) {
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
     * Emits an event for the given channel.
     *
     * @param channelUID UID of the channel over which the event will be emitted
     * @param event Event to emit
     */
    protected void triggerChannel(ChannelUID channelUID, String event) {
        synchronized (this) {
            if (this.callback != null) {
                this.callback.channelTriggered(this.getThing(), channelUID, event);
            } else {
                throw new IllegalStateException("Could not update state, because callback is missing");
            }
        }
    }

    /**
     * Emits an event for the given channel. Will use the thing UID to infer the
     * unique channel UID.
     *
     * @param channelUID UID of the channel over which the event will be emitted
     * @param event Event to emit
     */
    protected void triggerChannel(String channelUID, String event) {
        triggerChannel(new ChannelUID(this.getThing().getUID(), channelUID), event);
    }

    /**
     * Emits an event for the given channel. Will use the thing UID to infer the
     * unique channel UID.
     *
     * @param channelUID UID of the channel over which the event will be emitted
     */
    protected void triggerChannel(String channelUID) {
        triggerChannel(new ChannelUID(this.getThing().getUID(), channelUID), "");
    }

    /**
     * Emits an event for the given channel. Will use the thing UID to infer the
     * unique channel UID.
     *
     * @param channelUID UID of the channel over which the event will be emitted
     */
    protected void triggerChannel(ChannelUID channelUID) {
        triggerChannel(channelUID, "");
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
        return ThingBuilder.create(this.thing.getThingTypeUID(), this.thing.getUID())
                .withBridge(this.thing.getBridgeUID()).withChannels(this.thing.getChannels())
                .withConfiguration(this.thing.getConfiguration()).withLabel(this.thing.getLabel())
                .withLocation(this.thing.getLocation()).withProperties(this.thing.getProperties());
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
     * Updates the configuration of the thing and informs the framework about it.
     *
     * @param configuration
     *            configuration, that was updated and should be persisted
     *
     * @throws IllegalStateException
     *             if handler is not initialized correctly, because no callback is present
     */
    protected void updateConfiguration(Configuration configuration) {
        Map<String, Object> old = this.thing.getConfiguration().getProperties();
        try {
            this.thing.getConfiguration().setProperties(configuration.getProperties());
            synchronized (this) {
                if (this.callback != null) {
                    this.callback.thingUpdated(thing);
                } else {
                    throw new IllegalStateException("Could not update configuration, because callback is missing");
                }
            }
        } catch (RuntimeException e) {
            logger.warn(
                    "Error while applying configuration changes: '{}: {}' - reverting configuration changes on thing '{}'.",
                    e.getClass().getSimpleName(), e.getMessage(), this.thing.getUID().getAsString());
            this.thing.getConfiguration().setProperties(old);
            throw e;
        }
    }

    /**
     * Returns a copy of the properties map, that can be modified. The method {@link
     * BaseThingHandler#updateProperties(Map<String, String> properties)} must then be called to change the
     * properties values for the thing that is handled by this thing handler instance.
     *
     * @return copy of the thing properties (not null)
     */
    protected Map<String, String> editProperties() {
        Map<String, String> properties = this.thing.getProperties();
        return new HashMap<>(properties);
    }

    /**
     * Updates multiple properties for the thing that is handled by this thing handler instance. Each value is only
     * set for the given property name if there has not been set any value yet or if the value has been changed. If the
     * value of the property to be set is null then the property is removed.
     *
     * @param properties
     *            properties map, that was updated
     */
    protected void updateProperties(Map<String, String> properties) {
        for (Entry<String, String> property : properties.entrySet()) {
            String propertyName = property.getKey();
            String propertyValue = property.getValue();
            String existingPropertyValue = thing.getProperties().get(propertyName);
            if (existingPropertyValue == null || !existingPropertyValue.equals(propertyValue)) {
                this.thing.setProperty(propertyName, propertyValue);
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
     * If multiple properties should be changed at the same time, the {@link BaseThingHandler#editProperties()} method
     * should be used.
     *
     * @param name the name of the property to be set
     * @param value the value of the property
     */
    protected void updateProperty(String name, String value) {
        String existingPropertyValue = thing.getProperties().get(name);
        if (existingPropertyValue == null || !existingPropertyValue.equals(value)) {
            thing.setProperty(name, value);
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
            return linkRegistry != null ? !linkRegistry.getLinks(channel.getUID()).isEmpty() : false;
        } else {
            throw new IllegalArgumentException("Channel with ID '" + channelId + "' does not exists.");
        }
    }

    /**
     * Returns whether the handler has already been initialized.
     *
     * @return true if handler is initialized, false otherwise
     */
    protected boolean isInitialized() {
        return ThingHandlerHelper.isHandlerInitialized(this);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    protected void changeThingType(ThingTypeUID thingTypeUID, Configuration configuration) {
        if (this.callback != null) {
            this.callback.migrateThingType(getThing(), thingTypeUID, configuration);
        } else {
            throw new IllegalStateException("Could not change thing type because callback is missing");
        }
    }

}