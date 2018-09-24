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
package org.eclipse.smarthome.binding.astro.handler;

import org.eclipse.smarthome.binding.astro.internal.model.Planet;

/**
 * Astro Handler listener interface
 *
 * @author Gaël L'hopital - Initial contribution
 */
public interface AstroHandlerListener {

    void publishPlanet(Planet planet);

}
