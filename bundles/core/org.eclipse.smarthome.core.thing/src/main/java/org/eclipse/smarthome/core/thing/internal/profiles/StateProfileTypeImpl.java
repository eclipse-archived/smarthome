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
package org.eclipse.smarthome.core.thing.internal.profiles;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfileType;

/**
 * Default implementation of a {@link StateProfileType}.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class StateProfileTypeImpl implements StateProfileType {

    private final ProfileTypeUID profileTypeUID;
    private final String label;
    private final Collection<String> supportedItemTypes;

    public StateProfileTypeImpl(ProfileTypeUID profileTypeUID, String label, Collection<String> supportedItemTypes) {
        this.profileTypeUID = profileTypeUID;
        this.label = label;
        this.supportedItemTypes = supportedItemTypes;
    }

    @Override
    public ProfileTypeUID getUID() {
        return profileTypeUID;
    }

    @Override
    public Collection<String> getSupportedItemTypes() {
        return supportedItemTypes;
    }

    @Override
    public String getLabel() {
        return label;
    }

}
