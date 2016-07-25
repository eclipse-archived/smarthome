/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.persistence;

/**
 * This is a java bean that is used to serialize services to JSON.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ServiceBean {
    ServiceBean() {
    }

    /**
     * Service Name
     */
    public String name;

    /**
     * Persistence service class
     */
    public String classname;
}
