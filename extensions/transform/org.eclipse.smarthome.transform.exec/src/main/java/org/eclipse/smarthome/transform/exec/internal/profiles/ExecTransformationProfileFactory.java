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
package org.eclipse.smarthome.transform.exec.internal.profiles;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.profiles.Profile;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileFactory;
import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeBuilder;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeProvider;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Profilefactory that creates the transformation profile for the exec transformation service
 *
 * @author Stefan Triller - initial contribution
 *
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class ExecTransformationProfileFactory implements ProfileFactory, ProfileTypeProvider {

    @NonNullByDefault({})
    private TransformationService service;

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Arrays.asList(ProfileTypeBuilder.newState(ExecTransformationProfile.PROFILE_TYPE_UID,
                ExecTransformationProfile.PROFILE_TYPE_UID.getId()).build());
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        return new ExecTransformationProfile(callback, profileContext, service);
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Arrays.asList(ExecTransformationProfile.PROFILE_TYPE_UID);
    }

    @Reference(target = "(smarthome.transform=EXEC)")
    public void addTransformationService(TransformationService service) {
        this.service = service;
    }

    public void removeTransformationService(TransformationService service) {
        this.service = null;
    }
}
