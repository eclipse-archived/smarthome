/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.persistence.h2.internal;

import java.util.Date;

import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;

/**
 * This is a class used to return item information from the H2 database.
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class H2PersistenceItem implements PersistenceItemInfo {

    final private String name;
    final private Integer rows;
    final private Date earliest;
    final private Date latest;

    public H2PersistenceItem(String name, Integer rows, Date earliest, Date latest) {
        this.name = name;
        this.rows = rows;
        this.earliest = earliest;
        this.latest = latest;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getCount() {
        return rows;
    }

    @Override
    public Date getEarliest() {
        return earliest;
    }

    @Override
    public Date getLatest() {
        return latest;
    }
}