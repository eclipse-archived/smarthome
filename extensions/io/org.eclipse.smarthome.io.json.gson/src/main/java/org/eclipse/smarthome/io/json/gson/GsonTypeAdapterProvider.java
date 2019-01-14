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
package org.eclipse.smarthome.io.json.gson;

import java.util.Map;

/**
 * Provider of Gson Type Adapter instances.
 *
 * @author Flavio Costa - Initial contribution
 */
public interface GsonTypeAdapterProvider {

    /**
     * Returns the map of type adapters.
     *
     * @return Map for each class to the type adapter to be registered for it.
     */
    Map<Class<?>, Object> getTypeAdapters();

}
