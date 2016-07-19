/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

/**
 * This is an abstract parent exception to be extended by any exceptions
 * related to item lookups in the item registry.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public abstract class ItemLookupException extends Exception {

    public ItemLookupException(String string) {
        super(string);
    }

    private static final long serialVersionUID = -4617708589675048859L;

}
