/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding;

import java.net.URI;


/**
 * The {@link BindingInfo} is a service interface each <i>Binding</i> has to implement
 * to provide general information. Each <i>Binding</i> has to register its implementation
 * as service at the <i>OSGi</i> service registry.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public interface BindingInfo {

    /**
     * Returns an identifier for the binding (e.g. "hue").
     * 
     * @return an identifier for the binding (must neither be null nor empty)
     */
    String getId();

    /**
     * Returns a human readable name for the binding (e.g. "HUE Binding").
     * 
     * @return a human readable name for the binding (must neither be null nor empty)
     */
    String getName();

    /**
     * Returns a human readable description for the binding
     * (e.g. "Discovers and controls HUE bulbs").
     * 
     * @return a human readable description for the binding (could be null or empty)
     */
    String getDescription();

    /**
     * Returns the author of the binding (e.g. "Max Mustermann").
     * 
     * @return the author of the binding (must neither be null, nor empty)
     */
    String getAuthor();

    /**
     * Returns {@code true} if a link to a concrete {@link ConfigDescription} exists,
     * otherwise {@code false}.
     * 
     * @return true if a link to a concrete ConfigDescription exists, otherwise false
     */
    boolean hasConfigDescriptionURI();

    /**
     * Returns the link to a concrete {@link ConfigDescription}.
     * 
     * @return the link to a concrete ConfigDescription (could be null)
     */
    URI getConfigDescriptionURI();

}
