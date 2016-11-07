/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria;
import org.eclipse.smarthome.config.discovery.inbox.InboxListener;
import org.eclipse.smarthome.config.discovery.inbox.events.InboxEventFactory;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingRegistryChangeListener;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PersistentInbox} class is a concrete implementation of the {@link Inbox}.
 * <p>
 * This implementation uses the {@link DiscoveryServiceRegistry} to register itself as {@link DiscoveryListener} to
 * receive {@link DiscoveryResult} objects automatically from {@link DiscoveryService}s.
 * <p>
 * This implementation does neither handle memory leaks (orphaned listener instances) nor blocked listeners. No
 * performance optimizations have been done (synchronization).
 *
 * @author Michael Grammling - Initial Contribution
 * @author Dennis Nobel - Added automated removing of entries
 * @author Michael Grammling - Added dynamic configuration updates
 * @author Dennis Nobel - Added persistence support
 * @author Andre Fuechsel - Added removeOlderResults
 * @author Christoph Knauf - Added removeThingsForBridge and getPropsAndConfigParams
 *
 */
public final class PersistentInbox implements Inbox, DiscoveryListener, ThingRegistryChangeListener {

    /**
     * Internal enumeration to identify the correct type of the event to be fired.
     */
    private enum EventType {
        added,
        removed,
        updated
    }

    private class TimeToLiveCheckingThread implements Runnable {

        private PersistentInbox inbox;

        public TimeToLiveCheckingThread(PersistentInbox inbox) {
            this.inbox = inbox;
        }

        @Override
        public void run() {
            long now = new Date().getTime();
            for (DiscoveryResult result : inbox.getAll()) {
                if (isResultExpired(result, now)) {
                    logger.debug("Inbox entry for thing {} is expired and will be removed", result.getThingUID());
                    remove(result.getThingUID());
                }
            }
        }

        private boolean isResultExpired(DiscoveryResult result, long now) {
            if (result.getTimeToLive() == DiscoveryResult.TTL_UNLIMITED) {
                return false;
            }
            return (result.getTimestamp() + result.getTimeToLive() * 1000 < now);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(PersistentInbox.class);

    private Set<InboxListener> listeners = new CopyOnWriteArraySet<>();

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    private ThingRegistry thingRegistry;

    private ManagedThingProvider managedThingProvider;

    private ThingTypeRegistry thingTypeRegistry;

    private ConfigDescriptionRegistry configDescRegistry;

    private Storage<DiscoveryResult> discoveryResultStorage;

    private Map<DiscoveryResult, Class<?>> resultDiscovererMap = new ConcurrentHashMap<>();

    private ScheduledFuture<?> timeToLiveChecker;

    private EventPublisher eventPublisher;

    private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<>();

    @Override
    public Thing approve(ThingUID thingUID, String label) {
        if (thingUID == null) {
            throw new IllegalArgumentException("Thing UID must not be null");
        }
        List<DiscoveryResult> results = get(new InboxFilterCriteria(thingUID, null));
        if (results.isEmpty()) {
            throw new IllegalArgumentException("No Thing with UID " + thingUID.getAsString() + " in inbox");
        }
        DiscoveryResult result = results.get(0);
        final Map<String, String> properties = new HashMap<>();
        final Map<String, Object> configParams = new HashMap<>();
        getPropsAndConfigParams(result, properties, configParams);
        final Configuration config = new Configuration(configParams);
        ThingTypeUID thingTypeUID = result.getThingTypeUID();
        Thing newThing = ThingFactory.createThing(thingUID, config, properties, result.getBridgeUID(), thingTypeUID,
                this.thingHandlerFactories);
        if (newThing == null) {
            logger.warn("Cannot create thing. No binding found that supports creating a thing" + " of type {}.",
                    thingTypeUID);
            return null;
        }
        if (label != null && !label.isEmpty()) {
            newThing.setLabel(label);
        } else {
            newThing.setLabel(result.getLabel());
        }
        addThingSafely(newThing);
        return newThing;
    }

    @Override
    public synchronized boolean add(DiscoveryResult result) throws IllegalStateException {
        if (result != null) {
            ThingUID thingUID = result.getThingUID();
            Thing thing = this.thingRegistry.get(thingUID);

            if (thing == null) {
                DiscoveryResult inboxResult = get(thingUID);

                if (inboxResult == null) {
                    discoveryResultStorage.put(result.getThingUID().toString(), result);
                    notifyListeners(result, EventType.added);
                    logger.info("Added new thing '{}' to inbox.", thingUID);
                    return true;
                } else {
                    if (inboxResult instanceof DiscoveryResultImpl) {
                        DiscoveryResultImpl resultImpl = (DiscoveryResultImpl) inboxResult;
                        resultImpl.synchronize(result);
                        discoveryResultStorage.put(result.getThingUID().toString(), resultImpl);
                        notifyListeners(resultImpl, EventType.updated);
                        logger.debug("Updated discovery result for '{}'.", thingUID);
                        return true;
                    } else {
                        logger.warn("Cannot synchronize result with implementation class '{}'.",
                                inboxResult.getClass().getName());
                    }
                }
            } else {
                logger.debug("Discovery result with thing '{}' not added as inbox entry."
                        + " It is already present as thing in the ThingRegistry.", thingUID);

                boolean updated = synchronizeConfiguration(result.getProperties(), thing.getConfiguration());

                if (updated) {
                    logger.debug("The configuration for thing '{}' is updated...", thingUID);
                    this.managedThingProvider.update(thing);
                }
            }
        }

        return false;
    }

    private boolean synchronizeConfiguration(Map<String, Object> properties, Configuration config) {
        boolean configUpdated = false;

        final Set<Map.Entry<String, Object>> propertySet = properties.entrySet();

        for (Map.Entry<String, Object> propertyEntry : propertySet) {
            final String propertyKey = propertyEntry.getKey();
            final Object propertyValue = propertyEntry.getValue();

            // Check if the key is present in the configuration.
            if (!config.containsKey(propertyKey)) {
                continue;
            }

            // If the value is equal to the one of the configuration, there is nothing to do.
            if (Objects.equals(propertyValue, config.get(propertyKey))) {
                continue;
            }

            // - the given key is part of the configuration
            // - the values differ

            // update value
            config.put(propertyKey, propertyValue);
            configUpdated = true;
        }

        return configUpdated;
    }

    @Override
    public void addInboxListener(InboxListener listener) throws IllegalStateException {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    @Override
    public List<DiscoveryResult> get(InboxFilterCriteria criteria) throws IllegalStateException {
        List<DiscoveryResult> filteredEntries = new ArrayList<>();

        for (DiscoveryResult discoveryResult : this.discoveryResultStorage.getValues()) {
            if (matchFilter(discoveryResult, criteria)) {
                filteredEntries.add(discoveryResult);
            }
        }

        return filteredEntries;
    }

    @Override
    public List<DiscoveryResult> getAll() {
        return get((InboxFilterCriteria) null);
    }

    @Override
    public synchronized boolean remove(ThingUID thingUID) throws IllegalStateException {
        if (thingUID != null) {
            DiscoveryResult discoveryResult = get(thingUID);
            if (discoveryResult != null) {
                if (!isInRegistry(thingUID)) {
                    removeResultsForBridge(thingUID);
                }
                resultDiscovererMap.remove(discoveryResult);
                this.discoveryResultStorage.remove(thingUID.toString());
                notifyListeners(discoveryResult, EventType.removed);
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeInboxListener(InboxListener listener) throws IllegalStateException {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        if (add(result)) {
            resultDiscovererMap.put(result, source.getClass());
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        remove(thingUID);
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        HashSet<ThingUID> removedThings = new HashSet<>();
        for (DiscoveryResult discoveryResult : getAll()) {
            Class<?> discoverer = resultDiscovererMap.get(discoveryResult);
            if (thingTypeUIDs.contains(discoveryResult.getThingTypeUID()) && discoveryResult.getTimestamp() < timestamp
                    && (discoverer == null || source.getClass() == discoverer)) {
                ThingUID thingUID = discoveryResult.getThingUID();
                removedThings.add(thingUID);
                remove(thingUID);
                logger.debug("Removed {} from inbox because it was older than {}", thingUID, new Date(timestamp));
            }
        }
        return removedThings;
    }

    @Override
    public void added(Thing thing) {
        if (remove(thing.getUID())) {
            logger.debug(
                    "Discovery result removed from inbox, because it was added as a Thing" + " to the ThingRegistry.");
        }
    }

    @Override
    public void removed(Thing thing) {
        if (thing instanceof Bridge) {
            removeResultsForBridge(thing.getUID());
        }
    }

    @Override
    public void updated(Thing oldThing, Thing thing) {
        // Attention: Do NOT fire an event back to the ThingRegistry otherwise circular
        // events are fired! This event was triggered by the 'add(DiscoveryResult)'
        // method within this class. -> NOTHING TO DO HERE
    }

    @Override
    public void setFlag(ThingUID thingUID, DiscoveryResultFlag flag) {
        DiscoveryResult result = get(thingUID);
        if (result instanceof DiscoveryResultImpl) {
            DiscoveryResultImpl resultImpl = (DiscoveryResultImpl) result;
            resultImpl.setFlag((flag == null) ? DiscoveryResultFlag.NEW : flag);
            discoveryResultStorage.put(resultImpl.getThingUID().toString(), resultImpl);
            notifyListeners(resultImpl, EventType.updated);
        } else {
            logger.warn("Cannot set flag for result of instance type '{}'", result.getClass().getName());
        }
    }

    /**
     * Returns the {@link DiscoveryResult} in this {@link Inbox} associated with
     * the specified {@code Thing} ID, or {@code null}, if no {@link DiscoveryResult} could be found.
     *
     * @param thingId
     *            the Thing ID to which the discovery result should be returned
     *
     * @return the discovery result associated with the specified Thing ID, or
     *         null, if no discovery result could be found
     */
    private DiscoveryResult get(ThingUID thingUID) {
        if (thingUID != null) {
            return discoveryResultStorage.get(thingUID.toString());
        }

        return null;
    }

    private boolean matchFilter(DiscoveryResult discoveryResult, InboxFilterCriteria criteria) {
        if (criteria != null) {
            String bindingId = criteria.getBindingId();
            if ((bindingId != null) && (!bindingId.isEmpty())) {
                if (!discoveryResult.getBindingId().equals(bindingId)) {
                    return false;
                }
            }

            ThingTypeUID thingTypeUID = criteria.getThingTypeUID();
            if (thingTypeUID != null) {
                if (!discoveryResult.getThingTypeUID().equals(thingTypeUID)) {
                    return false;
                }
            }

            ThingUID thingUID = criteria.getThingUID();
            if (thingUID != null) {
                if (!discoveryResult.getThingUID().equals(thingUID)) {
                    return false;
                }
            }

            DiscoveryResultFlag flag = criteria.getFlag();
            if (flag != null) {
                if (discoveryResult.getFlag() != flag) {
                    return false;
                }
            }
        }
        return true;
    }

    private void notifyListeners(DiscoveryResult result, EventType type) {
        for (InboxListener listener : this.listeners) {
            try {
                switch (type) {
                    case added:
                        listener.thingAdded(this, result);
                        break;
                    case removed:
                        listener.thingRemoved(this, result);
                        break;
                    case updated:
                        listener.thingUpdated(this, result);
                        break;
                }
            } catch (Exception ex) {
                String errorMessage = String.format("Cannot notify the InboxListener '%s' about a Thing %s event!",
                        listener.getClass().getName(), type.name());

                logger.error(errorMessage, ex);
            }
        }
        postEvent(result, type);
    }

    private void postEvent(DiscoveryResult result, EventType eventType) {
        if (eventPublisher != null) {
            try {
                switch (eventType) {
                    case added:
                        eventPublisher.post(InboxEventFactory.createAddedEvent(result));
                        break;
                    case removed:
                        eventPublisher.post(InboxEventFactory.createRemovedEvent(result));
                        break;
                    case updated:
                        eventPublisher.post(InboxEventFactory.createUpdatedEvent(result));
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                logger.error("Could not post event of type '" + eventType.name() + "'.", ex);
            }
        }
    }

    private boolean isInRegistry(ThingUID thingUID) {
        if (thingRegistry.get(thingUID) == null) {
            return false;
        }
        return true;
    }

    private void removeResultsForBridge(ThingUID bridgeUID) {
        for (ThingUID thingUID : getResultsForBridge(bridgeUID)) {
            DiscoveryResult discoveryResult = get(thingUID);
            if (discoveryResult != null) {
                this.discoveryResultStorage.remove(thingUID.toString());
                notifyListeners(discoveryResult, EventType.removed);
            }
        }
    }

    private List<ThingUID> getResultsForBridge(ThingUID bridgeUID) {
        List<ThingUID> thingsForBridge = new ArrayList<>();
        for (DiscoveryResult result : discoveryResultStorage.getValues()) {
            if (bridgeUID.equals(result.getBridgeUID())) {
                thingsForBridge.add(result.getThingUID());
            }
        }
        return thingsForBridge;
    }

    /**
     * Get the properties and configuration parameters for the thing with the given {@link DiscoveryResult}.
     *
     * @param discoveryResult the DiscoveryResult
     * @param props the location the properties should be stored to.
     * @param configParams the location the configuration parameters should be stored to.
     */
    private void getPropsAndConfigParams(final DiscoveryResult discoveryResult, final Map<String, String> props,
            final Map<String, Object> configParams) {
        final Set<String> paramNames = getConfigDescParamNames(discoveryResult);
        final Map<String, Object> resultProps = discoveryResult.getProperties();
        for (String resultKey : resultProps.keySet()) {
            if (paramNames.contains(resultKey)) {
                configParams.put(resultKey, resultProps.get(resultKey));
            } else {
                props.put(resultKey, String.valueOf(resultProps.get(resultKey)));
            }
        }
    }

    private Set<String> getConfigDescParamNames(DiscoveryResult discoveryResult) {
        List<ConfigDescriptionParameter> confDescParams = getConfigDescParams(discoveryResult);
        Set<String> paramNames = new HashSet<>();
        for (ConfigDescriptionParameter param : confDescParams) {
            paramNames.add(param.getName());
        }
        return paramNames;
    }

    private List<ConfigDescriptionParameter> getConfigDescParams(DiscoveryResult discoveryResult) {
        ThingType type = thingTypeRegistry.getThingType(discoveryResult.getThingTypeUID());
        if (type != null && type.getConfigDescriptionURI() != null) {
            URI descURI = type.getConfigDescriptionURI();
            ConfigDescription desc = configDescRegistry.getConfigDescription(descURI);
            if (desc != null) {
                return desc.getParameters();
            }
        }
        return Collections.emptyList();
    }

    private void addThingSafely(Thing thing) {
        ThingUID thingUID = thing.getUID();
        if (thingRegistry.get(thingUID) != null) {
            thingRegistry.remove(thingUID);
        }
        thingRegistry.add(thing);
    }

    protected void activate(ComponentContext componentContext) {
        this.timeToLiveChecker = ThreadPoolManager.getScheduledPool("discovery")
                .scheduleWithFixedDelay(new TimeToLiveCheckingThread(this), 0, 30, TimeUnit.SECONDS);
        this.discoveryServiceRegistry.addDiscoveryListener(this);
    }

    void setTimeToLiveCheckingInterval(int interval) {
        this.timeToLiveChecker.cancel(true);
        this.timeToLiveChecker = ThreadPoolManager.getScheduledPool("discovery")
                .scheduleWithFixedDelay(new TimeToLiveCheckingThread(this), 0, interval, TimeUnit.SECONDS);
    }

    protected void deactivate(ComponentContext componentContext) {
        this.discoveryServiceRegistry.removeDiscoveryListener(this);
        this.listeners.clear();
        this.timeToLiveChecker.cancel(true);
    }

    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
        this.thingRegistry.addRegistryChangeListener(this);
    }

    protected void setManagedThingProvider(ManagedThingProvider thingProvider) {
        this.managedThingProvider = thingProvider;
    }

    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry.removeRegistryChangeListener(this);
        this.thingRegistry = null;
    }

    protected void unsetManagedThingProvider(ManagedThingProvider thingProvider) {
        this.managedThingProvider = null;
    }

    protected void setStorageService(StorageService storageService) {
        this.discoveryResultStorage = storageService.getStorage(DiscoveryResult.class.getName(),
                this.getClass().getClassLoader());
    }

    protected void unsetStorageService(StorageService storageService) {
        this.discoveryResultStorage = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        thingTypeRegistry = null;
    }

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        configDescRegistry = null;
    }

    protected void addThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        this.thingHandlerFactories.add(thingHandlerFactory);
    }

    protected void removeThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        this.thingHandlerFactories.remove(thingHandlerFactory);
    }

}
