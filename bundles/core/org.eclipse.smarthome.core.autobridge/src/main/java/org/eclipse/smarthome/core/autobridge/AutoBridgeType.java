/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.autobridge;

/**
 * <p>
 * This enum defines how <code>Command</code>s and <code>State</code>s are bridges between <code>Channel</code>s that
 * are bound to an <code>Item</code>
 * </p>
 *
 * <li><code>ALL</code></li> : bridge Command and States to all other Channels
 * <li><code>NONE</code></li> : no bridging
 * <li><code>INTER</code></li> : bridge Command and States to all other Channels that have a different Binding
 * ID
 * <li><code>INTRA</code></li> : bridge Command and States to all other Channels that have a the same Binding
 * ID
 * </ul>
 *
 * @author Karel Goderis - Initial Contribution
 *
 */
public enum AutoBridgeType {
    ALL,
    NONE,
    INTER,
    INTRA;
}
