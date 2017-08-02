/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import java.util.function.Predicate;

/**
 * Provides some default predicates that are helpful when working with metadata.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class MetadataPredicates {

    /**
     * Creates a {@link Predicate} which can be used to filter {@link Metadata} for a given namespace.
     *
     * @param namespace to filter
     * @return created {@link Predicate}
     */
    public static Predicate<Metadata> hasNamespace(String namespace) {
        return md -> md.getUID().getNamespace().equals(namespace);
    }

    /**
     * Creates a {@link Predicate} which can be used to filter {@link Metadata} of a given item.
     *
     * @param itemname to filter
     * @return created {@link Predicate}
     */
    public static Predicate<Metadata> ofItem(String itemname) {
        return md -> md.getUID().getItemName().equals(itemname);
    }
}
