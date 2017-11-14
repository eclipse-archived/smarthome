/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence.dto;

/**
 * This is a java bean that is used to serialize services to JSON.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class PersistenceServiceDTO {
    public PersistenceServiceDTO() {
    }

    /**
     * Service Id
     */
    public String id;

    /**
     * Service label
     */
    public String label;

    /**
     * Persistence service class
     */
    public String type;
}
