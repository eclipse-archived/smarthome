/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.rest.api;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
@SuppressWarnings("rawtypes")
public class Activator implements BundleActivator, ServiceTrackerCustomizer, HttpContext {

    static final String ALIAS = "/esh/automation/restdemo";

    BundleContext context;
    ServiceTracker httpTracker;

    @SuppressWarnings("unchecked")
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        httpTracker = new ServiceTracker(context, HttpService.class.getName(), this);
        httpTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        httpTracker.close();
        httpTracker = null;
    }

    @Override
    public Object addingService(ServiceReference reference) {
        @SuppressWarnings("unchecked")
        HttpService httpService = (HttpService) context.getService(reference);
        if (httpService != null) {
            try {
                httpService.registerResources(ALIAS, "/res", this);
            } catch (NamespaceException e) {
                e.printStackTrace();
            }
        }
        return httpService;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        ((HttpService) service).unregister(ALIAS);
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return true;
    }

    @Override
    public URL getResource(String name) {
        return context.getBundle().getResource(name);
    }

    @Override
    public String getMimeType(String name) {
        return null;
    }

}
