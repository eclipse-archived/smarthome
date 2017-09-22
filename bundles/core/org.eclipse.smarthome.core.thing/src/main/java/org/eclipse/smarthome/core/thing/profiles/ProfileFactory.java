/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Implementors are capable of creating a {@link Profile} instances.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public interface ProfileFactory {

    /**
     * Create a {@link Profile} instance for the given profile tye ID.
     *
     * @param profileTypeUID the profile identifier
     * @return a profile instance or {@code null} if this factory cannot handle the given link
     */
    @Nullable
    Profile createProfile(ProfileTypeUID profileTypeUID);

    /**
     * Return the identifiers of all supported profile types
     *
     * @return a collection of all profile type identifier which this class is capable of creating
     */
    Collection<ProfileTypeUID> getSupportedProfileTypeUIDs();

}
