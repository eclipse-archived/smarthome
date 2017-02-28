/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

/**
 * Utility class containing helper methods to be used in XML generation.
 *
 * @author Ivan Iliev - Initial contribution
 *
 */
public class XmlHelper {

    public static final String SYSTEM_NAMESPACE_PREFIX = "system.";
    private static final String SYSTEM_NAMESPACE = "system";

    /**
     * Returns a UID in the format of {1}:{2}, where {1} is {@link #SYSTEM_NAMESPACE} and {2} is the
     * given typeId stripped of the prefix {@link #SYSTEM_NAMESPACE_PREFIX} if it exists.
     *
     * @param typeId
     * @return system uid (e.g. "system:test")
     */
    public static String getSystemUID(String typeId) {
        if (typeId.startsWith(SYSTEM_NAMESPACE_PREFIX)) {
            typeId = typeId.substring(SYSTEM_NAMESPACE_PREFIX.length());
        }
        return String.format("%s:%s", SYSTEM_NAMESPACE, typeId);
    }
}
