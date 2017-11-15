/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
