package org.eclipse.smarthome.config.discovery.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria;
import org.eclipse.smarthome.config.discovery.inbox.InboxListener;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link PersistentInbox} class is a concrete implementation of the {@link Inbox}.
 * <p>
 * This implementation uses the {@link DiscoveryServiceRegistry} to register itself
 * as {@link DiscoveryListener} to receive {@link DiscoveryResult} objects automatically
 * from {@link DiscoveryService}s.
 * <p>
 * This implementation does neither handle memory leaks (orphaned listener instances)
 * nor blocked listeners. No performance optimizations have been done (synchronization).
 *
 * @author Michael Grammling
 */
public final class PersistentInbox implements Inbox, DiscoveryListener {

    private Logger logger = LoggerFactory.getLogger(PersistentInbox.class);

    /**
     * Internal enumeration to identify the correct type of the event to be fired.
     */
    private enum EventType {
        added,
        removed,
        updated
    }

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    private List<DiscoveryResult> entries;
    private List<InboxListener> listeners;

    private boolean invalid;


    public PersistentInbox(DiscoveryServiceRegistry discoveryServiceRegistry)
            throws IllegalArgumentException {

        if (discoveryServiceRegistry == null) {
            throw new IllegalArgumentException("The DiscoveryServiceRegistry must not be null!");
        }

        this.discoveryServiceRegistry = discoveryServiceRegistry;
        this.discoveryServiceRegistry.addDiscoveryListener(this);

        this.entries = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    public synchronized void release() {
        if (!this.invalid) {
            this.invalid = true;

            this.listeners.clear();
        }
    }

    private void assertServiceValid() throws IllegalStateException {
        if (this.invalid) {
            throw new IllegalStateException("The service is no longer available!");
        }
    }

    @Override
    public synchronized boolean add(DiscoveryResult result) throws IllegalStateException {
        assertServiceValid();

        if (result != null) {
            DiscoveryResult inboxResult = get(result.getThingUID());

            if (inboxResult == null) {
                this.entries.add(result);
                notifyListeners(result, EventType.added);
            } else {
                inboxResult.synchronize(result);
                notifyListeners(inboxResult, EventType.updated);
            }

            return true;
        }

        return false;
    }

    @Override
    public synchronized boolean remove(ThingUID thingUID) throws IllegalStateException {
        assertServiceValid();

        if (thingUID != null) {
            DiscoveryResult discoveryResult = get(thingUID);
            if (discoveryResult != null) {
                this.entries.remove(discoveryResult);
                notifyListeners(discoveryResult, EventType.removed);

                return true;
            }
        }

        return false;
    }

    /**
     * Returns the {@link DiscoveryResult} in this {@link Inbox} associated with the specified
     * {@code Thing} ID, or {@code null}, if no {@link DiscoveryResult} could be found.
     *
     * @param thingId the Thing ID to which the discovery result should be returned
     *
     * @return the discovery result associated with the specified Thing ID, or null,
     *     if no discovery result could be found
     */
    private DiscoveryResult get(ThingUID thingUID) {
        if (thingUID != null) {
            for (DiscoveryResult discoveryResult : this.entries) {
                if (discoveryResult.getThingUID().equals(thingUID)) {
                    return discoveryResult;
                }
            }
        }

        return null;
    }

    @Override
    public synchronized List<DiscoveryResult> get(InboxFilterCriteria criteria)
            throws IllegalStateException {

        assertServiceValid();

        List<DiscoveryResult> filteredEntries = new ArrayList<>();

        for (DiscoveryResult discoveryResult : this.entries) {
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

    @Override
    public synchronized void addInboxListener(InboxListener listener)
            throws IllegalStateException {

        assertServiceValid();

        if ((listener != null) && (!this.listeners.contains(listener))) {
            this.listeners.add(listener);
        }
    }

    @Override
    public synchronized void removeInboxListener(InboxListener listener)
            throws IllegalStateException {

        assertServiceValid();

        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    private synchronized void notifyListeners(DiscoveryResult result, EventType type) {
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
                        "Cannot notify the InboxListener '%s' about a Thing %s event!",
                        listener.getClass().getName(),
                        type.name());

                this.logger.error(errorMessage, ex);
            }
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
    public void discoveryFinished(DiscoveryService source) {
        // nothing to do
    }

    @Override
    public void discoveryErrorOccurred(DiscoveryService source, Exception exception) {
        // nothing to do
    }

}
