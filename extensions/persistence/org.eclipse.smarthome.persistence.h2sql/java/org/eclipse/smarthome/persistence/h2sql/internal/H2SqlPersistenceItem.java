/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.persistence.h2sql.internal;

import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;

/**
 * This is a Java bean used to return items from a SQL database.
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class H2SqlPersistenceItem implements PersistenceItemInfo {

    final private String name;
    final private Integer rows;

    public H2SqlPersistenceItem(String name, Integer rows) {
        this.name = name;
        this.rows = rows;
    }

    public String getName() {
        return name;
    }

    public Integer getRows() {
        return rows;
    }
}