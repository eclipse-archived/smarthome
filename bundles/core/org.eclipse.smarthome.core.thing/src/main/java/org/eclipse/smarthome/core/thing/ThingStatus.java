/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

/**
 * {@link ThingStatus} defines possible statuses of a {@link ThingStatusInfo}.
 *
 * @author Stefan Bußweiler - Initial contribution
 * @author Simon Kaufmann - added UNKNOWN, removed numbers
 *
 */
public enum ThingStatus {
    UNINITIALIZED,
    INITIALIZING,
    UNKNOWN,
    ONLINE,
    OFFLINE,
    REMOVING,
    REMOVED;
}
