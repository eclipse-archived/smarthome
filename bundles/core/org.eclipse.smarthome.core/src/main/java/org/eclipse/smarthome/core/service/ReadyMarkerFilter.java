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
 * Filter for {@link ReadyMarker}s which a ReadyTracker is interested in.
 *
 * By default, this filter will match any {@link ReadyMarker}.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public final class ReadyMarkerFilter {

    private final String identifier;
    private final String type;

    public ReadyMarkerFilter() {
        this(null, null);
    }

    private ReadyMarkerFilter(String type, String identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    public boolean apply(@NonNull ReadyMarker readyMarker) {
        return isTracked(type, readyMarker.getType()) && isTracked(identifier, readyMarker.getIdentifier());
    }

    private boolean isTracked(String trackingSpec, String realValue) {
        return trackingSpec == null || trackingSpec.equals(realValue);
    }

    /**
     * Returns a {@link ReadyMarkerFilter} restricted to the given type.
     *
     * @param type
     * @return
     */
    @NonNull
    public ReadyMarkerFilter withType(String type) {
        return new ReadyMarkerFilter(type, identifier);
    }

    /**
     * Returns a {@link ReadyMarkerFilter} restricted to the given identifier.
     *
     * @param type
     * @return
     */
    @NonNull
    public ReadyMarkerFilter withIdentifier(String identifier) {
        return new ReadyMarkerFilter(type, identifier);
    }

}
