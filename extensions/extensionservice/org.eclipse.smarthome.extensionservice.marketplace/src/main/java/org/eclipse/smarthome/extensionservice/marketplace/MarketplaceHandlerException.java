/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.extensionservice.marketplace;

/**
 * This is an exception that can be thrown by {@link MarketplaceExtensionHandler}s if some operation fails.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class MarketplaceHandlerException extends Exception {

    private static final long serialVersionUID = -5652014141471618161L;

    /**
     * Main constructor
     *
     * @param message A message describing the issue
     */
    public MarketplaceHandlerException(String message) {
        super(message);
    }
}
