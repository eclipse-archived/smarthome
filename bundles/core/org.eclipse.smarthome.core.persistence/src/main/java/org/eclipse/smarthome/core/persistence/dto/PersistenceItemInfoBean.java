/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence.dto;

/**
 * This is a java bean that is used to serialize information about items in a persistence service to JSON.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class PersistenceItemInfoBean {
    PersistenceItemInfoBean() {
    }

    /**
     * The name of the item in the persistence service
     */
    public String name;

    /**
     * A count of the number of rows of data in the persistence store for the item
     */
    public Integer count;

    /**
     * The earliest time for the data stored in the database
     */
    public String earliest;

    /**
     * The latest time for the data stored in the database
     */
    public String latest;
}
