/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

/**
 * This is a java bean that is used with JAX-RS to serialize options of a
 * parameter to JSON.
 *
 * @author Alex Tugarev - Initial contribution
 *
 */
public class ParameterOptionBean {

    public String label;
    public String value;

    public ParameterOptionBean() {
    }

    public ParameterOptionBean(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
