/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding.dto;

/**
 * This is a data transfer object that is used to serialize binding info objects.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class BindingInfoDTO {

    public String author;
    public String description;
    public String id;
    public String name;
    public String configDescriptionURI;

    public BindingInfoDTO() {
    }

    public BindingInfoDTO(String id, String name, String author, String description, String configDescriptionURI) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.description = description;
        this.configDescriptionURI = configDescriptionURI;
    }

}
