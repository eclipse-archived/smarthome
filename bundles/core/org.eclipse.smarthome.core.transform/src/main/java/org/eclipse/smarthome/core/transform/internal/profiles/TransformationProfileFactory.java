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
package org.eclipse.smarthome.core.transform.internal.profiles;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import org.eclipse.smarthome.core.transform.profiles.TransformationProfiles;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Factory for transformation profiles
 *
 * @author Stefan Triller - initial contribution
 *
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class TransformationProfileFactory implements ProfileFactory, ProfileTypeProvider {

    private static final String SERVICE_PROPERTY = "smarthome.transform";

    private final HashMap<ProfileTypeUID, ProfileType> supportedProfileTypes = new HashMap<>();

    // private final Logger logger = LoggerFactory.getLogger(TransformationProfileFactory.class);

    private final HashMap<ProfileTypeUID, TransformationService> transformationServices = new HashMap<>();

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return supportedProfileTypes.values();
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        if (transformationServices.containsKey(profileTypeUID)) {
            TransformationService service = transformationServices.get(profileTypeUID);
            return new TransformationProfile(callback, profileContext, service, profileTypeUID);
        }
        return null;
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return transformationServices.keySet();
    }

    private @Nullable ProfileTypeUID createTransformationProfileTypeUID(Map<String, Object> properties) {
        Object property = properties.get(SERVICE_PROPERTY);
        if (property instanceof String) {
            return new ProfileTypeUID(TransformationProfiles.SCOPE, (String) property);
        }
        return null;
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
    public void addTransformationService(TransformationService service, Map<String, Object> properties) {
        ProfileTypeUID type = createTransformationProfileTypeUID(properties);
        if (type != null) {
            transformationServices.put(type, service);
            supportedProfileTypes.put(type, ProfileTypeBuilder.newState(type, type.getId()).build());
        }
    }

    public void removeTransformationService(TransformationService service, Map<String, Object> properties) {
        ProfileTypeUID type = createTransformationProfileTypeUID(properties);
        if (type != null) {
            transformationServices.remove(type);
            supportedProfileTypes.remove(type);
        }
    }
}
