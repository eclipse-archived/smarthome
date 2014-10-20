/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.internal.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.internal.resources.beans.RootBean;

/**
 * <p>This class acts as an entry point / root resource for the REST API.</p>
 * <p>In good HATEOAS manner, it provides links to other offered resources.</p>
 * 
 * <p>The result is returned as JSON</p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
@Path("/")
public class RootResource {

	private List<RESTResource> restResources = new ArrayList<RESTResource>();

    @Context UriInfo uriInfo;

    @GET 
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoot(
    		@Context HttpHeaders headers) {
    	return Response.ok(getRootBean()).build();
    }

	private RootBean getRootBean() {
		RootBean bean = new RootBean();
	    
		for(RESTResource resource : restResources) {
			String path = resource.getClass().getAnnotation(Path.class).value();
			bean.links.add(new RootBean.Links(path, uriInfo.getBaseUriBuilder().path(path).build().toASCIIString()));
		}
	    
	    return bean;
	}

	public void addRESTResource(RESTResource resource) {
		restResources.add(resource);
	}

	public void removeRESTResource(RESTResource resource) {
		restResources.remove(resource);
	}

}
