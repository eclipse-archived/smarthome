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

@XmlRootElement(name = "bindingId")
public class BindingIdListBean {

    @XmlElement(name = "bindingId")
    public final List<String> entries = new ArrayList<>();

    public BindingIdListBean() {
    }

    public BindingIdListBean(Collection<String> list) {
        entries.addAll(list);
    }
}
