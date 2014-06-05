/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;


/**
 * {@link ConfigDescriptionProvider} can be implemented and registered as an
 * OSGi service to provide {@link ConfigDescription}s. The
 * {@link ConfigDescriptionRegistry} tracks each
 * {@link ConfigDescriptionProvider} and registers a
 * {@link ConfigDescriptionListener}.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 */
public interface ConfigDescriptionProvider {

    /**
     * Adds a {@link ConfigDescriptionListener} to the
     * {@link ConfigDescriptionProvider}. The implementation must call
     * {@link ConfigDescriptionListener#configDescriptionAdded(ConfigDescription)}
     * for each {@link ConfigDescription} that exists, before the listener was
     * added. Whenever a new config description should be added the
     * implementation must call
     * {@link ConfigDescriptionListener#configDescriptionAdded(ConfigDescription)}
     * , too.
     * 
     * @param listener
     *            listener
     */
    public void addConfigDescriptionListener(ConfigDescriptionListener listener);

    /**
     * Removes a {@link ConfigDescriptionListener} from the
     * {@link ConfigDescriptionProvider}. The implementation must call
     * {@link ConfigDescriptionListener#configDescriptionRemoved(ConfigDescription)}
     * for each {@link ConfigDescription} that was added by the
     * {@link ConfigDescriptionProvider}. Moreover the implementation must
     * release all references to the listener and is not allowed to call the
     * listener anymore.
     * 
     * @param listener
     *            listener
     */
    public void removeConfigDescriptionListener(ConfigDescriptionListener listener);
}
