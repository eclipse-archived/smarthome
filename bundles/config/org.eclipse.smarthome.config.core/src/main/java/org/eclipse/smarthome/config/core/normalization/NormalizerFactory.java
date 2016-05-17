/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.normalization;

import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * The {@link NormalizerFactory} can be used in order to obtain the {@link Normalizer} for any concrete
 * {@link ConfigDescriptionParameter.Type}.
 *
 * @author Simon Kaufmann - initial contribution and API.
 * @author Thomas Höfer - introduced normalizers map and added precondition check as well as some additional javadoc
 */
public final class NormalizerFactory {

    private static final Map<Type, Normalizer> normalizers = new ImmutableMap.Builder<Type, Normalizer>()
            .put(Type.BOOLEAN, new BooleanNormalizer()).put(Type.TEXT, new TextNormalizer())
            .put(Type.INTEGER, new IntNormalizer()).put(Type.DECIMAL, new DecimalNormalizer()).build();

    private NormalizerFactory() {
        // prevent instantiation
    }

    /**
     * Returns the {@link Normalizer} for the type of the given config description parameter.
     *
     * @param configDescriptionParameter the config description parameter (must not be null)
     * @return the corresponding {@link Normalizer} (not null)
     * @throws NullPointerException if the given config description parameter is null
     */
    public static Normalizer getNormalizer(ConfigDescriptionParameter configDescriptionParameter) {
        Preconditions.checkNotNull(configDescriptionParameter, "The config description parameter must not be null.");

        Normalizer ret = normalizers.get(configDescriptionParameter.getType());

        if (configDescriptionParameter.isMultiple()) {
            ret = new ListNormalizer(ret);
        }

        return ret;
    }

}
