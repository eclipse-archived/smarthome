/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * This interface provides helper methods for {@link DiscoveryService}s in order to access core framework capabilities.
 * <p>
 * This interface must not be implemented by bindings.
 *
 * @author Simon Kaufmann - initial contribution and API.
 */
@NonNullByDefault
public interface DiscoveryServiceCallback {

    /**
     * Get an existing {@link Thing} from the ThingRegistry, it it exists.
     *
     * @param thingUID the {@link ThingUID} by which the {@link Thing} is identified
     * @return the {@link Thing} if it exists or <code>null</code> otherwise
     */
    public @Nullable Thing getExistingThing(ThingUID thingUID);

    /**
     * Get the already existing {@link DiscoveryResult}s from the Inbox(es).
     *
     * @param thingUID the {@link ThingUID} which identify the {@link DiscoveryResult}
     * @return a {@link List} of {@link DiscoveryResult}s which are stored in the Inbox(es), can be <code>null</code>
     */
    public @Nullable DiscoveryResult getExistingDiscoveryResult(ThingUID thingUID);

}
