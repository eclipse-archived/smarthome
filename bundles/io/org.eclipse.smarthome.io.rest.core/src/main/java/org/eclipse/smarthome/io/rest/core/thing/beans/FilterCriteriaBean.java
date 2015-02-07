/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

/**
 * This is a java bean that is used with JAX-RS to serialize filter criteria of a
 * parameter to JSON.
 *
 * @author Alex Tugarev - Initial contribution
 *
 */
public class FilterCriteriaBean {

    public String value;
    public String name;

    public FilterCriteriaBean() {
    }

    public FilterCriteriaBean(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
