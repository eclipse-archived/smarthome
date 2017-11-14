/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.internal.resources.beans;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.io.rest.internal.RESTActivator;

/**
 * This is a java bean that is used to define the root entry
 * page of the REST interface.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class RootBean {

    final public String version = RESTActivator.getContext().getBundle().getVersion().toString();

    final public List<Links> links = new ArrayList<Links>();

    public static class Links {
        public Links(String type, String url) {
            this.type = type;
            this.url = url;
        }

        public String type;
        public String url;
    }
}
