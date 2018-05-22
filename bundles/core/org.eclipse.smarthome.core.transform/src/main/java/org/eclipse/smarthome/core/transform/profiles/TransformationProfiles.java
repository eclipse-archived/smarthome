/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.transform.profiles;

import org.eclipse.smarthome.core.thing.profiles.ProfileTypeBuilder;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfileType;

/**
 * Profiles for state transformations
 *
 * @author Stefan Triller - initial contribution
 *
 */
public interface TransformationProfiles {

    static final String SCOPE = "transform";

    static final ProfileTypeUID OFFSET = new ProfileTypeUID(SCOPE, "offset");

    static final StateProfileType OFFSET_TYPE = ProfileTypeBuilder.newState(OFFSET, "Offset").build();

}
