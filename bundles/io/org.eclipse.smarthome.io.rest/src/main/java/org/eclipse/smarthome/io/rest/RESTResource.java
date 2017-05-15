/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest;

/**
 * This is a marker interface for REST resource implementations
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Stefan Triller - Added default implementation for isSatisfied
 */
public interface RESTResource {

    /**
     * Method used to determine availability of a RESTResource
     *
     * @return true if this RESTResource is ready to process requests, false otherwise.
     */
    default boolean isSatisfied() {
        return true;
    }

}
