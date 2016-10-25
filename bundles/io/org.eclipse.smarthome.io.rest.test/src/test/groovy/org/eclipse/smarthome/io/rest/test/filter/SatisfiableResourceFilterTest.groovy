/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.test.filter;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo

import org.eclipse.smarthome.io.rest.RESTResource
import org.eclipse.smarthome.io.rest.SatisfiableRESTResource;
import org.eclipse.smarthome.io.rest.internal.filter.SatisfiableResourceFilter
import org.eclipse.smarthome.io.rest.test.filter.SatisfiableResourceFilterTest.ResponseHolder;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder
import org.junit.Test

/**
 * Test for {@link SatisfiableResourceFilter}
 * 
 * @author Ivan Iliev - Initial contribution
 *
 */
class SatisfiableResourceFilterTest {

    private static class ResponseHolder {
        public Response response = null;
        public boolean matchesObtained = false;
        public boolean satisfiedCalled = false;
    }


    private static final SatisfiableResourceFilter filter = new SatisfiableResourceFilter();

    @Test
    public void testWithBasicRESTResource() {
        
        RESTResource resource = [] as RESTResource;
        
        ResponseHolder responseHolder = new ResponseHolder();
        
        ContainerRequestContext context = getContextMock(resource, responseHolder);
        
        filter.filter(context);
        
        assertNull responseHolder.response
        assertTrue responseHolder.matchesObtained
        assertFalse responseHolder.satisfiedCalled
    }
    
    @Test
    public void testWithSatisfiableRESTResourceSatisfied() {
        
        ResponseHolder responseHolder = new ResponseHolder();
        
        RESTResource resource = [
                isSatisfied: {
                    responseHolder.satisfiedCalled = true
                    true
                }
            ] as SatisfiableRESTResource;
        
        
        ContainerRequestContext context = getContextMock(resource, responseHolder);
        
        filter.filter(context);
        
        assertNull responseHolder.response
        assertTrue responseHolder.matchesObtained
        assertTrue responseHolder.satisfiedCalled
    }

    @Test
    public void testWithSatisfiableRESTResourceNOTSatisfied() {
        ResponseHolder responseHolder = new ResponseHolder();
        
        RESTResource resource = [
                isSatisfied: {
                    responseHolder.satisfiedCalled=true
                    false
                }
            ] as SatisfiableRESTResource;
        

        
        ContainerRequestContext context = getContextMock(resource, responseHolder);
        
        filter.filter(context);
        
        assertNotNull responseHolder.response
        assertEquals responseHolder.response.status, Status.SERVICE_UNAVAILABLE.statusCode
        assertTrue responseHolder.matchesObtained
        assertTrue responseHolder.satisfiedCalled
    }


    private ContainerRequestContext getContextMock(final Object matchedResource, final ResponseHolder responseHolder) {

        final uriInfo = [
            getMatchedResources : {
                responseHolder.matchesObtained = true
                [matchedResource]
            }
        ] ;


        return [
            getUriInfo: { return uriInfo as UriInfo },
            abortWith: { Response resp ->
                responseHolder.response = resp;
            }
            
        ] as ContainerRequestContext
    }
}
