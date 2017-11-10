/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeProvider;

/**
 * The {@link ProfileTypeRegistry} allows access to the {@link ProfileType}s provided by all
 * {@link ProfileTypeProvider}s.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public interface ProfileTypeRegistry {

    public List<ProfileType> getProfileTypes();

    public List<ProfileType> getProfileTypes(Locale locale);

}
