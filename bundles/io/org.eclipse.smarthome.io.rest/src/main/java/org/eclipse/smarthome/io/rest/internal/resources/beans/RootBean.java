/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
