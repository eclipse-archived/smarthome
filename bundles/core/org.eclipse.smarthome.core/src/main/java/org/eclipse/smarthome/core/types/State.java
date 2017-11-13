/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.types;

/**
 * This is a marker interface for all state types.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface State extends Type {

    /**
     * Convert this {@link State}'s value into another type
     *
     * @param target the desired {@link State} type
     * @return the {@link State}'s value in the given type's representation, or <code>null</code> if the conversion was
     *         not possible
     */
    default State as(Class<? extends State> target) {
        if (target != null && target.isInstance(this)) {
            return this;
        } else {
            return null;
        }
    }
}
