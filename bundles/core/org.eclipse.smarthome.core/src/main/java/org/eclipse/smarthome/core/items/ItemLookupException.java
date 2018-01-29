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
