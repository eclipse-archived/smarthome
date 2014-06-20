/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import org.eclipse.smarthome.core.binding.BindingInfo;

/**
 * The {@link HueBindingInfo} is responsible for providing information about the
 * hue binding. In the future the binding info can specified via xml.
 * 
 * @author Oliver Libutzki - Initial contribution
 * 
 */
public class HueBindingInfo implements BindingInfo {

    public final static String BINDING_ID = "hue";

    @Override
    public String getId() {
        return BINDING_ID;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getName() {
        return "hue Binding";
    }

    @Override
    public String getAuthor() {
        return "Oliver Libutzki";
    }

    @Override
    public String getConfigDescriptionURI() {
        return "";
    }

    @Override
    public boolean hasConfigDescriptionURI() {
        return false;
    }

}
