/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.normalization;

import java.util.ArrayList;
import java.util.List;

/**
 * The normalizer for configuration parameters allowing multiple values. It converts all collections/arrays to a
 * {@link List} and applies the underlying normalizer to each of the values inside that list.
 *
 * @author Simon Kaufmann - initial contribution and API.
 * @author Thomas Höfer - made class final and minor javadoc changes
 */
final class ListNormalizer extends AbstractNormalizer {

    private Normalizer delegate;

    ListNormalizer(Normalizer delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object doNormalize(Object value) {
        if (!isList(value)) {
            List ret = new ArrayList(1);
            ret.add(delegate.normalize(value));
            return ret;
        }
        if (isArray(value)) {
            List ret = new ArrayList(((Object[]) value).length);
            for (Object object : ((Object[]) value)) {
                ret.add(delegate.normalize(object));
            }
            return ret;
        }
        if (value instanceof List) {
            List ret = new ArrayList(((List) value).size());
            for (Object object : (List) value) {
                ret.add(delegate.normalize(object));
            }
            return ret;
        }
        if (value instanceof Iterable) {
            List ret = new ArrayList();
            for (Object object : (Iterable) value) {
                ret.add(delegate.normalize(object));
            }
            return ret;
        }
        return value;
    }

    static boolean isList(Object value) {
        return isArray(value) || value instanceof Iterable;
    }

    private static boolean isArray(Object object) {
        return object != null && object.getClass().isArray();
    }

}
