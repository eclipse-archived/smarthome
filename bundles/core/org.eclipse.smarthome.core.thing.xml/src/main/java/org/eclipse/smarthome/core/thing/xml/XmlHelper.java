/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml;

import org.eclipse.smarthome.core.thing.type.SystemChannelTypeProvider;

/**
 * Utility class containing helper methods to be used in XML generation.
 * 
 * @author Ivan Iliev - Initial contribution
 * 
 */
public class XmlHelper {

    /**
     * Returns a UID in the format of {1}:{2}, where {1} is {@link SystemChannelTypeProvider #NAMESPACE} and {2} is the
     * given typeId stripped of the prefix {@link SystemChannelTypeProvider #NAMESPACE_PREFIX} if it exists.
     * 
     * @param typeId
     * @return system uid (e.g. "system:test")
     */
    public static String getSystemUID(String typeId) {
        int prefixIdx = typeId.indexOf(SystemChannelTypeProvider.NAMESPACE_PREFIX);

        if (prefixIdx != -1) {
            typeId = typeId.substring(prefixIdx + SystemChannelTypeProvider.NAMESPACE_PREFIX.length());
        }

        return String.format("%s:%s", SystemChannelTypeProvider.NAMESPACE, typeId);
    }
}
