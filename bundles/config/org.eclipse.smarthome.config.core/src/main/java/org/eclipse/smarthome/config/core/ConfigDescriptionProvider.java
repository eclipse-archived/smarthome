/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.util.Collection;


/**
 * The {@link ConfigDescriptionProvider} can be implemented and registered as an <i>OSGi</i>
 * service to provide {@link ConfigDescription}s. The {@link ConfigDescriptionRegistry} tracks
 * each {@link ConfigDescriptionProvider} and registers a {@link ConfigDescriptionsChangeListener}
 * on each provider.
 * 
 * @author Dennis Nobel - Initial contribution
 * @author Michael Grammling - Initial contribution
 */
public interface ConfigDescriptionProvider {

    /**
     * Provides a collection of {@link ConfigDescription}s.
     * 
     * @return the configuration descriptions provided by this provider (not null, could be empty)
     */
    Collection<ConfigDescription> getConfigDescriptions();

    /**
     * Adds a {@link ConfigDescriptionsChangeListener} which is notified, if there are changes concerning
     * the {@link ConfigDescription}s provided by this {@link ConfigDescriptionProvider}.
     * <p>
     * This method returns silently if the specified listener is {@code null} or has already been
     * registered before.
     * 
     * @param listener the listener to be added (could be null)
     */
    void addConfigDescriptionsChangeListener(ConfigDescriptionsChangeListener listener);

    /**
     * Removes a {@link ConfigDescriptionsChangeListener} from this {@link ConfigDescriptionProvider}.
     * <p>
     * This method returns silently if the specified listener is {@code null} or has not been
     * registered before.
     * 
     * @param listener the listener to be removed (could be null)
     */
    void removeConfigDescriptionsChangeListener(ConfigDescriptionsChangeListener listener);

}
