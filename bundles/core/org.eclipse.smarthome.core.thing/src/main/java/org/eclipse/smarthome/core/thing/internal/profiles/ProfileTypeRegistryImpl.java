/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeProvider;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * {@link ProfileTypeRegistry} implementation.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@Component(service = ProfileTypeRegistry.class)
public class ProfileTypeRegistryImpl extends AbstractRegistry<ProfileType, ProfileTypeUID, ProfileTypeProvider>
        implements ProfileTypeRegistry {

    public ProfileTypeRegistryImpl() {
        super(ProfileTypeProvider.class);
    }

}
