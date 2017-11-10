/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link ProfileTypeRegistry} implementation.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@Component(service = ProfileTypeRegistry.class)
public class ProfileTypeRegistryImpl implements ProfileTypeRegistry {

    private final List<ProfileTypeProvider> profileTypeProviders = new CopyOnWriteArrayList<>();

    @Override
    public List<ProfileType> getProfileTypes() {
        return getProfileTypes(null);
    }

    @Override
    public List<ProfileType> getProfileTypes(Locale locale) {
        List<ProfileType> profileTypes = new ArrayList<>();
        for (ProfileTypeProvider profileTypeProvider : profileTypeProviders) {
            profileTypes.addAll(profileTypeProvider.getProfileTypes(locale));
        }
        return Collections.unmodifiableList(profileTypes);
    }

    @Reference
    protected void addProfileTypeProvider(ProfileTypeProvider profileTypeProvider) {
        profileTypeProviders.add(profileTypeProvider);
    }

    protected void removeProfileTypeProvider(ProfileTypeProvider profileTypeProvider) {
        profileTypeProviders.remove(profileTypeProvider);
    }

}
