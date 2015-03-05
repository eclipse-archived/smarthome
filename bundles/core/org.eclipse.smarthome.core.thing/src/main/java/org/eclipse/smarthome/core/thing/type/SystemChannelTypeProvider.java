/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import java.util.Collection;

/**
 * A service responsible for providing systemwide {@link ChannelType}s.
 * 
 * @author Ivan Iliev - Initial contribution
 * 
 */
public interface SystemChannelTypeProvider {

    public static final String NAMESPACE_PREFIX = "system.";

    public static final String NAMESPACE = "system";

    /**
     * Returns all System wide channel types.
     * 
     * @return
     */
    public Collection<ChannelType> getSystemChannelTypes();

}