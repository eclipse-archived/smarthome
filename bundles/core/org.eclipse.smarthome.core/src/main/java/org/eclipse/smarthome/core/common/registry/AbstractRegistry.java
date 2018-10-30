/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.common.registry;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractRegistry} is an abstract implementation of the {@link Registry} interface, that can be used as
 * base class for {@link Registry} implementations.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Stefan Bußweiler - Migration to new event mechanism
 * @author Victor Toni - provide elements as {@link Stream}
 * @author Kai Kreuzer - switched to parameterized logging
 * @author Hilbrand Bouwkamp - Made protected fields private and added new methods to give access.
 *
 * @param <E>
 *            type of the element
 */
public abstract class AbstractRegistry<E extends Identifiable<K>, K, P extends Provider<E>>
        implements ProviderChangeListener<E>, Registry<E, K> {

    private enum EventType {
        ADDED,
        REMOVED,
        UPDATED;
    }

    private final Logger logger = LoggerFactory.getLogger(AbstractRegistry.class);

    private final Class<P> providerClazz;
    private ServiceTracker<P, P> providerTracker;

    private final Map<Provider<E>, Collection<E>> elementMap = new ConcurrentHashMap<Provider<E>, Collection<E>>();
    private final Collection<RegistryChangeListener<E>> listeners = new CopyOnWriteArraySet<RegistryChangeListener<E>>();

    private Optional<ManagedProvider<E, K>> managedProvider = Optional.empty();

    private EventPublisher eventPublisher;

    /**
     * Constructor.
     *
     * @param providerClazz the class of the providers (see e.g. {@link AbstractRegistry#addProvider(Provider)}), null
     *            if no providers should be tracked automatically after activation
     */
    protected AbstractRegistry(final Class<P> providerClazz) {
        this.providerClazz = providerClazz;
    }

    protected void activate(final BundleContext context) {
        if (providerClazz != null) {
            /*
             * The handlers for 'add' and 'remove' the services implementing the provider class (cardinality is
             * multiple) rely on an active component.
             * To grant that the add and remove functions are called only for an active component, we use a provider
             * tracker.
             */
            providerTracker = new ProviderTracker(context, providerClazz);
            providerTracker.open();
        }
    }

    protected void deactivate() {
        if (providerTracker != null) {
            providerTracker.close();
            providerTracker = null;
        }
    }

    private final class ProviderTracker extends ServiceTracker<P, P> {

        private final BundleContext context;

        /**
         * Constructor.
         *
         * @param context the bundle context to lookup services
         * @param providerClazz the class that implementing services should be tracked
         */
        public ProviderTracker(final BundleContext context, final Class<P> providerClazz) {
            super(context, providerClazz.getName(), null);
            this.context = context;
        }

        @Override
        public P addingService(ServiceReference<P> reference) {
            final P service = context.getService(reference);
            addProvider(service);
            return service;
        }

        @Override
        public void removedService(ServiceReference<P> reference, P service) {
            removeProvider(service);
        }
    }

    @Override
    public void added(Provider<E> provider, E element) {
        Collection<E> elements = elementMap.get(provider);
        if (elements != null) {
            try {
                K uid = element.getUID();
                E existingElement = get(uid);
                if (uid != null && existingElement != null) {
                    logger.warn(
                            "{} with key '{}' already exists from provider {}! Failed to add a second with the same UID from provider {}!",
                            element.getClass().getSimpleName(), uid,
                            getProvider(existingElement).getClass().getSimpleName(),
                            provider.getClass().getSimpleName());
                    return;
                }
                onAddElement(element);
                elements.add(element);
                notifyListenersAboutAddedElement(element);
            } catch (Exception ex) {
                logger.warn("Could not add element: {}", ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void addRegistryChangeListener(RegistryChangeListener<E> listener) {
        listeners.add(listener);
    }

    @Override
    public Collection<@NonNull E> getAll() {
        return stream().collect(Collectors.toList());
    }

    @Override
    public Stream<E> stream() {
        return elementMap.values() // gets a Collection<Collection<E>>
                .stream() // creates a Stream<Collection<E>>
                .flatMap(collection -> collection.stream()); // flattens the stream to Stream<E>
    }

    @Override
    public void removed(Provider<E> provider, E element) {
        Collection<E> elements = elementMap.get(provider);
        if (elements != null) {
            try {
                // the given "element" might not be the live instance but
                // loaded from storage. operate on the real element:
                E existingElement = get(element.getUID());
                if (existingElement != null) {
                    onRemoveElement(existingElement);
                    elements.remove(existingElement);
                    notifyListenersAboutRemovedElement(existingElement);
                } else {
                    logger.debug("{} with key '{}' could not be removed from provider {} because it does not exist!",
                            element.getClass().getSimpleName(), element.getUID(), provider.getClass().getSimpleName());
                }
            } catch (Exception ex) {
                logger.warn("Could not remove element: {}", ex.getMessage(), ex);
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
        if (elements != null && elements.contains(oldElement) && oldElement.getUID().equals(element.getUID())) {
            try {
                // the given "oldElement" might not be the live instance but
                // loaded from storage. operate on the real element:
                E existingElement = get(oldElement.getUID());
                if (existingElement != null) {
                    beforeUpdateElement(existingElement);
                } else {
                    logger.debug("{} with key '{}' could not be updated for provider {} because it does not exist!",
                            element.getClass().getSimpleName(), element.getUID(), provider.getClass().getSimpleName());
                }
                onUpdateElement(oldElement, element);
                if (existingElement != null) {
                    elements.remove(existingElement);
                }
                elements.add(element);
                notifyListenersAboutUpdatedElement(oldElement, element);
            } catch (Exception ex) {
                logger.warn("Could not update element: {}", ex.getMessage(), ex);
            }
        }
    }

    @Override
    public E get(K key) {
        for (final Map.Entry<Provider<E>, Collection<E>> entry : elementMap.entrySet()) {
            for (final E element : entry.getValue()) {
                if (key.equals(element.getUID())) {
                    return element;
                }
            }
        }
        return null;
    }

    public Entry<Provider<E>, E> getBoth(K key) {
        for (final Map.Entry<Provider<E>, Collection<E>> entry : elementMap.entrySet()) {
            for (final E element : entry.getValue()) {
                if (key.equals(element.getUID())) {
                    return new SimpleEntry<Provider<E>, E>(entry.getKey(), element);
                }
            }
        }
        return null;

    }

    @Override
    public E add(E element) {
        managedProvider.orElseThrow(() -> new IllegalStateException("ManagedProvider is not available")).add(element);
        return element;
    }

    @Override
    public E update(E element) {
        return managedProvider.orElseThrow(() -> new IllegalStateException("ManagedProvider is not available"))
                .update(element);
    }

    @Override
    public E remove(K key) {
        return managedProvider.orElseThrow(() -> new IllegalStateException("ManagedProvider is not available"))
                .remove(key);
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
                logger.error("Could not inform the listener '{}' about the '{}' event: {}", listener, eventType.name(),
                        throwable.getMessage(), throwable);
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

    protected void addProvider(Provider<E> provider) {
        // only add this provider if it does not already exist
        if (!elementMap.containsKey(provider)) {
            Collection<E> elementsOfProvider = provider.getAll();
            Collection<E> elements = new CopyOnWriteArraySet<E>();
            provider.addProviderChangeListener(this);
            elementMap.put(provider, elements);
            for (E element : elementsOfProvider) {
                added(provider, element);
            }
            logger.debug("Provider '{}' has been added.", provider.getClass().getName());
        }
    }

    public Provider<E> getProvider(K key) {
        return getProvider(get(key));
    }

    public Provider<E> getProvider(E element) {
        for (Entry<Provider<E>, Collection<E>> entry : elementMap.entrySet()) {
            if (entry.getValue().contains(element)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void forEach(Provider<E> provider, Consumer<E> consumer) {
        elementMap.get(provider).forEach(consumer);
    }

    public void forEach(Consumer<E> consumer) {
        for (Entry<Provider<E>, Collection<E>> entry : elementMap.entrySet()) {
            entry.getValue().forEach(consumer);
        }
    }

    public void forEach(BiConsumer<Provider<E>, E> consumer) {
        for (Entry<Provider<E>, Collection<E>> entry : elementMap.entrySet()) {
            entry.getValue().forEach(e -> consumer.accept(entry.getKey(), e));
        }
    }

    protected Optional<ManagedProvider<E, K>> getManagedProvider() {
        return managedProvider;
    }

    protected void setManagedProvider(ManagedProvider<E, K> provider) {
        managedProvider = Optional.ofNullable(provider);
    }

    protected void unsetManagedProvider(ManagedProvider<E, K> provider) {
        managedProvider = Optional.empty();
    }

    /**
     * This method is called before an element is added. The implementing class
     * can override this method to perform initialization logic or check the
     * validity of the element.
     *
     * <p>
     * To keep custom logic on the inheritance chain, you must call always the super implementation first.
     *
     * <p>
     * If the method throws an {@link IllegalArgumentException} the element will not be added.
     * <p>
     *
     * @param element element to be added
     * @throws IllegalArgumentException if the element is invalid and should not be added
     */
    protected void onAddElement(E element) throws IllegalArgumentException {
        // can be overridden by sub classes
    }

    /**
     * This method is called before an element is removed. The implementing
     * class can override this method to perform specific logic.
     *
     * <p>
     * To keep custom logic on the inheritance chain, you must call always the super implementation first.
     *
     * @param element element to be removed
     */
    protected void onRemoveElement(E element) {
        // can be overridden by sub classes
    }

    /**
     * This method is called before an element is updated. The implementing
     * class can override this method to perform specific logic.
     *
     * @param existingElement the previously existing element (as held in the element cache)
     */
    protected void beforeUpdateElement(E existingElement) {
        // can be overridden by sub classes
    }

    /**
     * This method is called before an element is updated. The implementing
     * class can override this method to perform specific logic or check the
     * validity of the updated element.
     *
     * <p>
     * To keep custom logic on the inheritance chain, you must call always the super implementation first.
     *
     * @param oldElement old element (before update, as given by the provider)
     * @param element updated element (after update)
     *            <p>
     *            If the method throws an {@link IllegalArgumentException} the element will not be updated.
     *            <p>
     * @throws IllegalArgumentException if the updated element is invalid and should not be updated
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
                    logger.warn("Could not remove element: {}", ex.getMessage(), ex);
                }
            }

            elementMap.remove(provider);

            provider.removeProviderChangeListener(this);

            logger.debug("Provider '{}' has been removed.", provider.getClass().getSimpleName());
        }
    }

    protected EventPublisher getEventPublisher() {
        return this.eventPublisher;
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
                logger.error("Could not post event of type '{}'.", event.getType(), ex);
            }
        }
    }

}
