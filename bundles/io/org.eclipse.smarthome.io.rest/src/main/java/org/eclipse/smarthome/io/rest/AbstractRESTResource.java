/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest;

import org.osgi.framework.BundleContext;

/**
 * This is an abstract class for all REST resource implementation, that provides
 * some common methods.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public abstract class AbstractRESTResource implements RESTResource {

    protected <T> T getService(Class<T> serviceInterface) {
        BundleContext bundleContext = RESTApplication.getBundleContext();
        return bundleContext.getService(bundleContext.getServiceReference(serviceInterface));
    }

}
