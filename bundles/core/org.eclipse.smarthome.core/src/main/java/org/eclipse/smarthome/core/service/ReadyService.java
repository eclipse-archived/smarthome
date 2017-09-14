/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.service;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Registry for {@link ReadyMarker}s.
 *
 * Services may use the {@link ReadyService} in order to denote they have completed loading/processing something.
 * <p>
 * Interested parties may register as a tracker for {@link ReadyMarker}s. Optionally they can provide a
 * {@link ReadyMarkerFilter} in order to restrict the {@link ReadyMarker}s they get notified for.
 * <p>
 * Alternatively, {@link #isReady(ReadyMarker)} can be used to check for any given {@link ReadyMarker}.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public interface ReadyService {

    /**
     * Register the given marker as being "ready".
     *
     * @param readyMarker
     */
    void markReady(@NonNull ReadyMarker readyMarker);

    /**
     * Removes the given marker.
     *
     * @param readyMarker
     */
    void unmarkReady(@NonNull ReadyMarker readyMarker);

    /**
     *
     * @param readyMarker
     * @return {@code true} if the given {@link ReadyMarker} is registered as being "ready".
     */
    boolean isReady(@NonNull ReadyMarker readyMarker);

    /**
     * Adds the given tracker.
     *
     * It will be notified for all {@link ReadyMarker}s.
     *
     * @param readyTracker
     */
    void registerTracker(@NonNull ReadyTracker readyTracker);

    /**
     * Adds the given tracker.
     *
     * It will be notified for a ReadyMarker changes related to those which match the given filter criteria.
     * <p>
     * The provided tracker will get notified about the addition of all existing readyMarkers right away.
     *
     * @param readyTracker
     * @param readyMarker
     */
    void registerTracker(@NonNull ReadyTracker readyTracker, @NonNull ReadyMarkerFilter filter);

    /**
     * Removes the given tracker.
     *
     * The provided tracker will get notified about the removal of all existing readyMarkers right away.
     *
     * @param readyTracker
     */
    void unregisterTracker(@NonNull ReadyTracker readyTracker);

    /**
     * Tracker for changes related to {@link ReadyMarker} registrations.
     *
     * @author Simon Kaufmann - initial contribution and API.
     *
     */
    interface ReadyTracker {

        /**
         * Gets called when a new {@link ReadyMarker} was registered as being "ready".
         *
         * @param readyMarker
         */
        void onReadyMarkerAdded(@NonNull ReadyMarker readyMarker);

        /**
         * Gets called when a {@link ReadyMarker} was unregistered.
         *
         * @param readyMarker
         */
        void onReadyMarkerRemoved(@NonNull ReadyMarker readyMarker);

    }

}
