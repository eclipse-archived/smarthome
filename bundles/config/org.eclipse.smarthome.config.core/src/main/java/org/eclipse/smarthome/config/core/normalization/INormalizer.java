/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.normalization;

/**
 * Normali
 *
 * @author Simon Kaufmann - initial contribution and API
 */
public interface INormalizer {

    /**
     * Normalize the given object to the expected type, if possible.
     *
     * @param value the object to normalize
     * @return the well-defined type or the given object, if it was not possible to convert it
     */
    public Object normalize(Object value);

}
