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
 * This is a token, identifying something to be completed.
 *
 * A caller may use it to identify which action or process is completed or to which completion it wants to listen to.
 * Thereby the {@code type} denotes the category of readyness, the {@code identifier}, e.g.
 * {@code new ReadyMarker("xmlProcessing", "o.e.sh.binding.sample")} would denote that the "sample binding" has finished
 * processing some xmls.
 * <p>
 * When the action or process is being marked as "ready" or removed from the "ready" state, a registered tracker will be
 * notified by the ReadyService.
 * <p>
 * This class overrides {@link #hashCode()} and {@link #equals(Object)} so that any using class does not have to keep
 * original references, but new instances can be used for calls to the {@link ReadyService} every time.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public final class ReadyMarker {

    @NonNull
    private final String type;

    @NonNull
    private final String identifier;

    public ReadyMarker(@NonNull String type, @NonNull String identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    @NonNull
    public String getType() {
        return type;
    }

    @NonNull
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReadyMarker other = (ReadyMarker) obj;
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getType() + "=" + getIdentifier();
    }

}
