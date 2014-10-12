/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a java bean that is used with JAXB to serialize things to XML or
 * JSON.
 * 
 * @author Dennis Nobel - Initial contribution
 *
 */
@XmlRootElement(name = "thingType")
public class ThingTypeBean {

    @XmlElement(name = "configParameter")
    public List<ConfigDescriptionParameterBean> configParameters;
    public String description;
    public String label;

    public String UID;

    public ThingTypeBean() {
    }

    public ThingTypeBean(String UID, String label, String description,
            List<ConfigDescriptionParameterBean> configParameters) {
        this.UID = UID;
        this.label = label;
        this.description = description;
        this.configParameters = configParameters;
    }

}
