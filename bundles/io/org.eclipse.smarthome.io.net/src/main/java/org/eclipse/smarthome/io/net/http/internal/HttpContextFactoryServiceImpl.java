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
package org.eclipse.smarthome.io.net.http.internal;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.smarthome.io.net.http.HttpContextFactoryService;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

/**
 * Create {@link HttpContext} instances when registering servlets, resources or filters using the
 * {@link HttpService#registerServlet} and corresponding methods.
 * The resulting {@link HttpContext} complies with the OSGi specification when it comes to resource resolving.
 *
 * @author Henning Treu - initial contribution and API
 *
 */
@Component(service = HttpContextFactoryService.class)
public class HttpContextFactoryServiceImpl implements HttpContextFactoryService {

    @Override
    public HttpContext createDefaultHttpContext(Bundle bundle) {
        return new DefaultHttpContext(bundle);
    }

    private class DefaultHttpContext implements HttpContext {
        private final Bundle bundle;

        private DefaultHttpContext(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
            return true;
        }

        @Override
        public URL getResource(String name) {
            if (name != null) {
                String resourceName;
                if (name.startsWith("/")) {
                    resourceName = name.substring(1);
                } else {
                    resourceName = name;
                }

                return bundle.getResource(resourceName);
            }
            return null;
        }

        @Override
        public String getMimeType(String name) {
            return null;
        }
    }

}
