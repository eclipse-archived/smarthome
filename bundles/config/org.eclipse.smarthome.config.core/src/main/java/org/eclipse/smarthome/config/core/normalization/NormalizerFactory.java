/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.normalization;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Use this factory in order to obtain normalizers for any concrete type.
 *
 * @author Simon Kaufmann - initial contribution and API.
 */
public final class NormalizerFactory {

    private NormalizerFactory() {
        // prevent instantiation
    }

    public static INormalizer getNormalizer(ConfigDescriptionParameter configDescriptionParameter) {
        INormalizer ret = null;
        switch (configDescriptionParameter.getType()) {
            case BOOLEAN:
                ret = new BooleanNormalizer();
                break;
            case INTEGER:
                ret = new IntNormalizer();
                break;
            case DECIMAL:
                ret = new DecimalNormalizer();
                break;
            case TEXT:
                ret = new TextNormalizer();
                break;
            default:
                throw new IllegalArgumentException();
        }
        if (configDescriptionParameter.isMultiple()) {
            ret = new ListNormalizer(ret);
        }
        return ret;
    }

}
