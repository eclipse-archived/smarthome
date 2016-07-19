/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.inbox;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;

/**
 * The {@link InboxListener} interface for receiving {@link Inbox} events.
 * <p>
 * A class that is interested in processing {@link Inbox} events fired synchronously by the {@link Inbox} service has to
 * implement this interface.
 *
 * @author Michael Grammling - Initial Contribution.
 *
 * @see Inbox
 */
public interface InboxListener {

    /**
     * Invoked synchronously when a <i>NEW</i> {@link DiscoveryResult} has been added
     * to the {@link Inbox}.
     *
     * @param source the inbox which is the source of this event (not null)
     * @param result the discovery result which has been added to the inbox (not null)
     */
    void thingAdded(Inbox source, DiscoveryResult result);

    /**
     * Invoked synchronously when an <i>EXISTING</i> {@link DiscoveryResult} has been
     * updated in the {@link Inbox}.
     *
     * @param source the inbox which is the source of this event (not null)
     * @param result the discovery result which has been updated in the inbox (not null)
     */
    void thingUpdated(Inbox source, DiscoveryResult result);

    /**
     * Invoked synchronously when an <i>EXISTING</i> {@link DiscoveryResult} has been
     * removed from the {@link Inbox}.
     *
     * @param source the inbox which is the source of this event (not null)
     * @param result the discovery result which has been removed from the inbox (not null)
     */
    void thingRemoved(Inbox source, DiscoveryResult result);

}
