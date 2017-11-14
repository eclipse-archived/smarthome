/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.dto;

/**
 * This is a data transfer object that is used to serialize options of a
 * parameter.
 *
 * @author Alex Tugarev - Initial contribution
 *
 */
public class ParameterOptionDTO {

    public String label;
    public String value;

    public ParameterOptionDTO() {
    }

    public ParameterOptionDTO(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
