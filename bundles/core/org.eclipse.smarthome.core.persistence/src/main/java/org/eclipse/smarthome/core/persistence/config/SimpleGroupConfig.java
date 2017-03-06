/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence.config;

/**
 * This class represents the configuration that is used for group items.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class SimpleGroupConfig extends SimpleConfig {

    private final String group;

    public SimpleGroupConfig(final String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return String.format("%s [group=%s]", getClass().getSimpleName(), group);
    }

}
