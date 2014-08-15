package org.eclipse.smarthome.core.common.registry;

import java.util.Collection;

/**
 * The {@link Registry} interface represents a registry for elements of the type
 * E. The concrete subinterfaces are registered as OSGi services.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 * @param <E>
 *            type of the elements in the registry
 */
public interface Registry<E> {

    /**
     * Adds a {@link RegistryChangeListener} to the registry.
     * 
     * @param listener
     *            registry change listener
     */
    void addRegistryChangeListener(RegistryChangeListener<E> listener);

    /**
     * Returns a collection of all elements in the registry.
     * 
     * @return collection of all elements in the registry
     */
    Collection<E> getAll();

    /**
     * Removes a {@link RegistryChangeListener} from the registry.
     * 
     * @param listener
     *            registry change listener
     */
    void removeRegistryChangeListener(RegistryChangeListener<E> listener);

}