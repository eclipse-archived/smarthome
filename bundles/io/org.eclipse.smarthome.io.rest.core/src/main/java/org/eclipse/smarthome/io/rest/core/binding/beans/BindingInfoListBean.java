/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.binding.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is a java bean that is used with JAXB to serialize binding info object
 * lists to JSON.
 * 
 * @author Dennis Nobel - Initial contribution
 *
 */
public class BindingInfoListBean {

    public final List<BindingInfoBean> entries = new ArrayList<>();

    public BindingInfoListBean() {
    };

    public BindingInfoListBean(Collection<BindingInfoBean> list) {
        entries.addAll(list);
    }
}
