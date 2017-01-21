/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.autobridge;

/**
 * This interface is implemented by classes that can provide configuration
 * information of the AutoBridge feature.
 *
 * Implementing classes should register themselves as a service in order to be
 * taken into account.
 *
 * @author Karel Goderis - Initial contribution
 */
public interface AutoBridgeBindingConfigProvider {

    /**
     * Indicates how an Item with the given <code>itemName</code> is
     * configured to bridge it's State after receiving a Command
     *
     * @param itemName the name of the Item for which to find the configuration
     * @return the <code>AutoBridgeType</code> to use
     */
    AutoBridgeType bridgeType(String itemName);

}
