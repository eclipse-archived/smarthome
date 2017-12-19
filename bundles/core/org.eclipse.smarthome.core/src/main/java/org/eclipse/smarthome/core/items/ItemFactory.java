/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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

import org.eclipse.jdt.annotation.NonNull;

/**
 * This Factory creates concrete instances of the known ItemTypes.
 *
 * @author Thomas.Eichstaedt-Engelen
 */
public interface ItemFactory {

    /**
     * Creates a new Item instance of type <code>itemTypeName</code> and the name <code>itemName</code>
     *
     * @param itemTypeName
     * @param itemName
     *
     * @return a new Item of type <code>itemTypeName</code> or <code>null</code> if no matching class is known.
     */
    GenericItem createItem(@NonNull String itemTypeName, @NonNull String itemName);

    /**
     * Returns the list of all supported ItemTypes of this Factory.
     *
     * @return the supported ItemTypes
     */
    String[] getSupportedItemTypes();
}
