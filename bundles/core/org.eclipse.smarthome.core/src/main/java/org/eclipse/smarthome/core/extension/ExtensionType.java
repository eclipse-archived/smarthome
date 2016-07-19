/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.extension;

/**
 * This class defines an extension type.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ExtensionType {

    private final String id;
    private final String label;

    /**
     * Creates a new type instance with the given id and label
     *
     * @param id
     * @param label
     */
    public ExtensionType(String id, String label) {
        this.id = id;
        this.label = label;
    }

    /**
     * The id of the type
     */
    public String getId() {
        return id;
    }

    /**
     * The label of the type to be used for headers (likely to be plural form)
     */
    public String getLabel() {
        return label;
    }

}
