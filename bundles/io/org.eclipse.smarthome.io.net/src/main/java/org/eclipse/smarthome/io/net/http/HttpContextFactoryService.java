/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.http;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

/**
 * Create {@link HttpContext} instances when registering servlets, resources or filters using the
 * {@link HttpService#registerServlet} and corresponding methods.
 *
 * @author Henning Treu - initial contribution and API
 *
 */
public interface HttpContextFactoryService {

    /**
     * Creates an {@link HttpContext} according to the OSGi specification of
     * {@link HttpService#createDefaultHttpContext()} as described here:
     * https://www.knopflerfish.org/releases/6.0.0/docs/javadoc/org/osgi/service/http/HttpService.html#createDefaultHttpContext
     *
     * @param bundle the bundle which will be used by this {@link HttpContext} to resolve resources.
     * @return the {@link HttpContext} for the given bundle.
     */
    HttpContext createHttpContext(Bundle bundle);

}
