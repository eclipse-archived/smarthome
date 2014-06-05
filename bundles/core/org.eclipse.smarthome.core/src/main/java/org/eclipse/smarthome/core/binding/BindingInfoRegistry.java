/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link BindingInfoRegistry} tracks all {@link BindingInfo} OSGi services and
 * provides access to them.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 */
public class BindingInfoRegistry {

    private List<BindingInfo> bindingInfos = new CopyOnWriteArrayList<BindingInfo>();

    protected void addBindingInfo(BindingInfo bindingInfo) {
        bindingInfos.add(bindingInfo);
    }

    protected void removeBindingInfo(BindingInfo bindingInfo) {
        bindingInfos.remove(bindingInfo);
    }

    /**
     * Returns all {@link BindingInfo} that are registered as OSGi services.
     * 
     * @return all {@link BindingInfo} OSGi services
     */
    public List<BindingInfo> getBindingInfos() {
        return Collections.unmodifiableList(bindingInfos);
    }


}
