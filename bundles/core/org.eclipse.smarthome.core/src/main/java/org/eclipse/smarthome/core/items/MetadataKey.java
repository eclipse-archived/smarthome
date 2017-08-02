/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the key of a {@link Metadata} entity.
 * It is a simple combination of a namespace and an item name.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public class MetadataKey {

    private final String namespace;
    private final String itemName;

    /**
     * Creates a new instance.
     *
     * @param namespace
     * @param itemName
     */
    public MetadataKey(String namespace, String itemName) {
        this.namespace = namespace;
        this.itemName = itemName;
    }

    /**
     * Provides the item name of this key
     *
     * @return the item name
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Provides the namespace of this key
     * 
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((itemName == null) ? 0 : itemName.hashCode());
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MetadataKey other = (MetadataKey) obj;
        if (!itemName.equals(other.itemName)) {
            return false;
        }
        if (!namespace.equals(other.namespace)) {
            return false;
        }
        return true;
    }

    @Override
    public @NonNull String toString() {
        return namespace + ":" + itemName;
    }
}
