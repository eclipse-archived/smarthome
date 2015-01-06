/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria;
import org.eclipse.smarthome.config.discovery.inbox.InboxListener;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingRegistryChangeListener;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PersistentInbox} class is a concrete implementation of the
 * {@link Inbox}.
 * <p>
 * This implementation uses the {@link DiscoveryServiceRegistry} to register
 * itself as {@link DiscoveryListener} to receive {@link DiscoveryResult}
 * objects automatically from {@link DiscoveryService}s.
 * <p>
 * This implementation does neither handle memory leaks (orphaned listener
 * instances) nor blocked listeners. No performance optimizations have been
 * done (synchronization).
 * 
 * @author Michael Grammling - Initial Contribution
 * @author Dennis Nobel - Added automated removing of entries
 * @author Michael Grammling - Added dynamic configuration updates
 * @author Dennis Nobel - Added persistence support
 * 
 */
public final class PersistentInbox implements Inbox, DiscoveryListener, ThingRegistryChangeListener {

    /**
     * Internal enumeration to identify the correct type of the event to be fired.
     */
    private enum EventType {
        added, removed, updated
    }

    private final Logger logger = LoggerFactory.getLogger(PersistentInbox.class);

    private Set<InboxListener> listeners = new CopyOnWriteArraySet<>();

    private DiscoveryServiceRegistry discoveryServiceRegistry;
    private ThingRegistry thingRegistry;
    private ManagedThingProvider managedThingProvider;
    
    private Storage<DiscoveryResult> discoveryResultStorage;

    @Override
    public synchronized boolean add(DiscoveryResult result) throws IllegalStateException {
        if (result != null) {
            ThingUID thingUID = result.getThingUID();
            Thing thing = this.thingRegistry.getByUID(thingUID);

            if (thing == null) {
            	DiscoveryResult inboxResult = get(thingUID);

                if (inboxResult == null) {
                	discoveryResultStorage.put(result.getThingUID().toString(), result);
                    notifyListeners(result, EventType.added);
                    logger.info("Added new thing '{}' to inbox.", thingUID);
                    return true;
                } else {
                    if(inboxResult instanceof DiscoveryResultImpl) {
                    	DiscoveryResultImpl resultImpl = (DiscoveryResultImpl) inboxResult;
                    	resultImpl.synchronize(result);
                    	discoveryResultStorage.put(result.getThingUID().toString(), resultImpl);
                        notifyListeners(resultImpl, EventType.updated);
                        logger.debug("Updated discovery result for '{}'.", thingUID);
                        return true;
                    } else {
                        logger.warn("Cannot synchronize result with implementation class '{}'.", inboxResult.getClass().getName());
                    }
                }
            } else {
                logger.debug("Discovery result with thing '{}' not added as inbox entry."
                        + " It is already present as thing in the ThingRegistry.", thingUID);

                boolean updated = synchronizeConfiguration(
                        result.getProperties(), thing.getConfiguration());

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

        Set<Map.Entry<String, Object>> propertySet = properties.entrySet();

        for (Map.Entry<String, Object> propertyEntry : propertySet) {
            String propertyKey = propertyEntry.getKey();
            Object propertyValue = propertyEntry.getValue();

            Object configValue = config.get(propertyKey);

            if (((propertyValue == null) && (configValue != null))
                    || (propertyValue != null && !propertyValue.equals(configValue))) {

                // update value
                config.put(propertyKey, propertyValue);
                configUpdated = true;
            }
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
        add(result);
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        remove(thingUID);
    }

    @Override
    public void added(Thing thing) {
        if (remove(thing.getUID())) {
            logger.debug("Discovery result removed from inbox, because it was added as a Thing"
                    + " to the ThingRegistry.");
        }
    }

    @Override
    public void removed(Thing thing) {
        // nothing to do
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
    	if(result instanceof DiscoveryResultImpl) {
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
     * the specified {@code Thing} ID, or {@code null}, if no
     * {@link DiscoveryResult} could be found.
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
                String errorMessage = String.format(
                        "Cannot notify the InboxListener '%s' about a Thing %s event!", listener
                                .getClass().getName(), type.name());

                logger.error(errorMessage, ex);
            }
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        this.listeners.clear();
    }

    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
        this.discoveryServiceRegistry.addDiscoveryListener(this);
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
        this.thingRegistry.addRegistryChangeListener(this);
    }

    protected void setManagedThingProvider(ManagedThingProvider thingProvider) {
        this.managedThingProvider = thingProvider;
    }

    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry.removeDiscoveryListener(this);
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
        this.discoveryResultStorage = storageService.getStorage(DiscoveryResult.class.getName(), this.getClass().getClassLoader());
    }
    
    protected void unsetStorageService(StorageService storageService) {
        this.discoveryResultStorage = null;
    }

}
