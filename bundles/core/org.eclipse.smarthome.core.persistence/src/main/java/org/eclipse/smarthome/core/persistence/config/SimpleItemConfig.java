/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence.config;

/**
 * This class represents the configuration that identify item(s) by name.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class SimpleItemConfig extends SimpleConfig {
    final String item;

    public SimpleItemConfig(final String item) {
        this.item = item;
    }

    public String getItem() {
        return item;
    }

    @Override
    public String toString() {
        return String.format("%s [item=%s]", getClass().getSimpleName(), item);
    }
}
