/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.i18n;

import org.eclipse.smarthome.core.library.types.PointType;

/**
 * This interface describes a provider for a location.
 *
 * @author Stefan Triller - Initial contribution and API
 */
public interface LocationProvider {

    /**
     * Provides access to the location of the installation
     *
     * @return location of the current installation or null if the location is not set
     */
    PointType getLocation();

}
