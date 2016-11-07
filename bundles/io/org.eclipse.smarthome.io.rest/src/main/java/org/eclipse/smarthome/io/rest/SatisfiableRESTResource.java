/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest;

/**
 * Interface that extends {@link RESTResource} by adding the {@link #isSatisfied()} method.
 * This allows the implementation of {@link RESTResource}s that only optionally depend on their bound references and
 * will not deactivate/reactivate if they go missing.
 * The {@link #isSatisfied()} will be used at request time to determine if the {@link SatisfiableRESTResource} is ready
 * to process the current request and if not will fail early with a 503 Service Unavailable HTTP Status Code.
 * 
 * @author Ivan Iliev - Initial contribution and API
 *
 */
public interface SatisfiableRESTResource extends RESTResource {

    /**
     * Method used to determine availability of a {@link RESTResource}
     * 
     * @return true if this {@link RESTResource} is ready to process requests, false otherwise.
     */
    public boolean isSatisfied();
}
