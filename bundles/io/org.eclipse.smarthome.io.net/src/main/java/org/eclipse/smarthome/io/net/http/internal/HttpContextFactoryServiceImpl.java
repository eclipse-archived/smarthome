/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * The resulting {@link HttpContext} complies with the OSGi specification when it comes to resource resolving, see
 * https://www.knopflerfish.org/releases/6.0.0/docs/javadoc/org/osgi/service/http/HttpService.html#createDefaultHttpContext
 *
 * @author Henning Treu - initial contribution and API
 *
 */
@Component(service = HttpContextFactoryService.class)
public class HttpContextFactoryServiceImpl implements HttpContextFactoryService {

    @Override
    public HttpContext createHttpContext(Bundle bundle) {
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
                String resourceName = name;
                if (name.startsWith("/")) {
                    resourceName = name.substring(1);
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
