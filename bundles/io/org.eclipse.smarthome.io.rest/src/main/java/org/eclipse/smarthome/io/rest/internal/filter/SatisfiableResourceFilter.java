/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.internal.filter;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.eclipse.smarthome.io.rest.RESTResource;
import org.osgi.service.component.annotations.Component;

/**
 * A filter that only affects resources implementing the {@link SatisfiableRESTResource} interface.
 * If the current request is going to be fulfilled by a Resource implementing this interface and the
 * {@link SatisfiableRESTResource#isSatisfied()} returns false then the request will be aborted with HTTP Status Code
 * 503 - Service Unavailable.
 *
 * @author Ivan Iliev - Initial contribution and API
 *
 */
@Provider
@Component(immediate = true, service = SatisfiableResourceFilter.class)
public class SatisfiableResourceFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {

        UriInfo uriInfo = ctx.getUriInfo();

        if (uriInfo != null) {
            List<Object> matchedResources = uriInfo.getMatchedResources();

            if (matchedResources != null && !matchedResources.isEmpty()) {
                // current resource is always first as per documentation
                Object matchedResource = matchedResources.get(0);

                if (matchedResource instanceof RESTResource && !((RESTResource) matchedResource).isSatisfied()) {
                    ctx.abortWith(Response.status(Status.SERVICE_UNAVAILABLE).build());
                }
            }
        }
    }

}
