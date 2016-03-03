/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.registry;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * The {@link AbstractRegistry} is an abstract implementation of the {@link Registry} interface, that can be used as
 * base class for {@link Registry} implementations.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Stefan Bußweiler - Migration to new event mechanism
 *
 * @param <E>
 *            type of the element
 */
public abstract class AbstractRegistry<E, K> implements ProviderChangeListener<E>, Registry<E, K> {

    private enum EventType {
        ADDED,
        REMOVED,
        UPDATED;
    }

    private final Logger logger = LoggerFactory.getLogger(AbstractRegistry.class);

    protected Map<Provider<E>, Collection<E>> elementMap = new ConcurrentHashMap<Provider<E>, Collection<E>>();

    protected Collection<RegistryChangeListener<E>> listeners = new CopyOnWriteArraySet<RegistryChangeListener<E>>();

    protected ManagedProvider<E, K> managedProvider;

    protected EventPublisher eventPublisher;

    @Override
    public void added(Provider<E> provider, E element) {
        Collection<E> elements = elementMap.get(provider);
        if (elements != null) {
            try {
                onAddElement(element);
                elements.add(element);
                notifyListenersAboutAddedElement(element);
            } catch (Exception ex) {
                logger.warn("Could not add element: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void addRegistryChangeListener(RegistryChangeListener<E> listener) {
        listeners.add(listener);
    }

    @Override
    public Collection<E> getAll() {
        return ImmutableList.copyOf(Iterables.concat(elementMap.values()));
    }

    @Override
    public void removed(Provider<E> provider, E element) {
        Collection<E> elements = elementMap.get(provider);
        if (elements != null) {
            try {
                onRemoveElement(element);
                elements.remove(element);
                notifyListenersAboutRemovedElement(element);
            } catch (Exception ex) {
                logger.warn("Could not remove element: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void removeRegistryChangeListener(RegistryChangeListener<E> listener) {
        listeners.remove(listener);
    }

    @Override
    public void updated(Provider<E> provider, E oldElement, E element) {
        Collection<E> elements = elementMap.get(provider);
        if (elements != null) {
            try {
                onUpdateElement(oldElement, element);
                elements.remove(oldElement);
                elements.add(element);
                notifyListenersAboutUpdatedElement(oldElement, element);
            } catch (Exception ex) {
                logger.warn("Could not update element: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public E add(E element) {
        if (this.managedProvider != null) {
            this.managedProvider.add(element);
            return element;
        } else {
            throw new IllegalStateException("ManagedProvider is not available");
        }
    }

    @Override
    public E update(E element) {
        if (this.managedProvider != null) {
            return this.managedProvider.update(element);
        } else {
            throw new IllegalStateException("ManagedProvider is not available");
        }
    }

    @Override
    public E remove(K key) {
        if (this.managedProvider != null) {
            return this.managedProvider.remove(key);
        } else {
            throw new IllegalStateException("ManagedProvider is not available");
        }
    }

    protected void notifyListeners(E oldElement, E element, EventType eventType) {
        for (RegistryChangeListener<E> listener : this.listeners) {
            try {
                switch (eventType) {
                    case ADDED:
                        listener.added(element);
                        break;
                    case REMOVED:
                        listener.removed(element);
                        break;
                    case UPDATED:
                        listener.updated(oldElement, element);
                        break;
                    default:
                        break;
                }
            } catch (Throwable throwable) {
                logger.error("Could not inform the listener '" + listener + "' about the '" + eventType.name()
                        + "' event!: " + throwable.getMessage(), throwable);
            }
        }
    }

    protected void notifyListeners(E element, EventType eventType) {
        notifyListeners(null, element, eventType);
    }

    protected void notifyListenersAboutAddedElement(E element) {
        notifyListeners(element, EventType.ADDED);
    }

    protected void notifyListenersAboutRemovedElement(E element) {
        notifyListeners(element, EventType.REMOVED);
    }

    protected void notifyListenersAboutUpdatedElement(E oldElement, E element) {
        notifyListeners(oldElement, element, EventType.UPDATED);
    }

    @SuppressWarnings("unchecked")
    protected void addProvider(Provider<E> provider) {
        // only add this provider if it does not already exist
        if (!elementMap.containsKey(provider)) {
            Collection<E> elementsOfProvider = provider.getAll();
            Collection<E> elements = new CopyOnWriteArraySet<E>();
            provider.addProviderChangeListener(this);
            elementMap.put(provider, elements);
            for (E element : elementsOfProvider) {
                try {
                    onAddElement(element);
                    elements.add(element);
                    notifyListenersAboutAddedElement(element);
                } catch (Exception ex) {
                    logger.warn("Could not add element: " + ex.getMessage(), ex);
                }
            }
            logger.debug("Provider '{}' has been added.", provider.getClass().getName());
            if (provider instanceof ManagedProvider) {
                this.managedProvider = (ManagedProvider<E, K>) provider;
            }
        }
    }

    /**
     * This method is called before an element is added. The implementing class
     * can override this method to perform initialization logic or check the
     * validity of the element.
     *
     * <p>
     * If the method throws an {@link IllegalArgumentException} the element will not be added.
     * <p>
     *
     * @param element
     *            element to be added
     * @throws IllegalArgumentException
     *             if the element is invalid and should not be added
     */
    protected void onAddElement(E element) throws IllegalArgumentException {
        // can be overridden by sub classes
    }

    /**
     * This method is called before an element is removed. The implementing
     * class can override this method to perform specific logic.
     *
     * @param element
     *            element to be removed
     */
    protected void onRemoveElement(E element) {
        // can be overridden by sub classes
    }

    /**
     * This method is called before an element is updated. The implementing
     * class can override this method to perform specific logic or check the
     * validity of the updated element.
     *
     * @param oldElement
     *            old element (before update)
     * @param element
     *            updated element (after update)
     *
     *            <p>
     *            If the method throws an {@link IllegalArgumentException} the element will not be updated.
     *            <p>
     *
     * @throws IllegalArgumentException
     *             if the updated element is invalid and should not be updated
     */
    protected void onUpdateElement(E oldElement, E element) throws IllegalArgumentException {
        // can be overridden by sub classes
    }

    protected void removeProvider(Provider<E> provider) {
        if (elementMap.containsKey(provider)) {

            for (E element : elementMap.get(provider)) {
                try {
                    onRemoveElement(element);
                    notifyListenersAboutRemovedElement(element);
                } catch (Exception ex) {
                    logger.warn("Could not remove element: " + ex.getMessage(), ex);
                }
            }

            elementMap.remove(provider);

            provider.removeProviderChangeListener(this);

            logger.debug("Provider '{}' has been removed.", provider.getClass().getSimpleName());

            if (this.managedProvider == provider) {
                this.managedProvider = null;
            }
        }
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    /**
     * This method can be used in a subclass in order to post events through the Eclipse SmartHome events bus. A common
     * use case is to notify event subscribers about an element which has been added/removed/updated to the registry.
     *
     * @param event the event
     */
    protected void postEvent(Event event) {
        if (eventPublisher != null) {
            try {
                eventPublisher.post(event);
            } catch (Exception ex) {
                logger.error("Could not post event of type '" + event.getType() + "'.", ex);
            }
        }
    }

}
