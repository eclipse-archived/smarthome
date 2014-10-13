/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.discovery.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a java bean that is used with JAXB to serialize discovery result
 * lists to XML or JSON.
 * 
 * @author Dennis Nobel - Initial contribution
 *
 */
@XmlRootElement(name = "discoveryResults")
public class DiscoveryResultListBean {

    @XmlElement(name = "discoveryResult")
    public final List<DiscoveryResultBean> entries = new ArrayList<>();

    public DiscoveryResultListBean() {
    }

    public DiscoveryResultListBean(Collection<DiscoveryResultBean> list) {
        entries.addAll(list);
    }
}
