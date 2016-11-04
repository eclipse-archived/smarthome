/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.List;

import org.eclipse.smarthome.core.thing.binding.BridgeHandler;

/**
 * A {@link Bridge} is a {@link Thing} that connects other {@link Thing}s.
 *
 * @author Dennis Nobel - Initial contribution and API
 */
public interface Bridge extends Thing {

    /**
     * Returns the children of the bridge.
     *
     * @return children
     */
    List<Thing> getThings();

    /**
     * Gets the bridge handler.
     *
     * @return the handler (can be null)
     */
    @Override
    BridgeHandler getHandler();
}
