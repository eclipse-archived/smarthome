/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.discovery;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.core.discovery.beans.DiscoveryResultBean;
import org.eclipse.smarthome.io.rest.core.util.BeanMapper;

/**
 * This class acts as a REST resource for the inbox and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector
 */
@Path("inbox")
public class InboxResource implements RESTResource {

	private Inbox inbox;
	private ManagedThingProvider managedThingProvider;
	
	protected void setInbox(Inbox inbox) {
		this.inbox = inbox;
	}

	protected void unsetInbox(Inbox inbox) {
		this.inbox = null;
	}

	protected void setManagedThingProvider(ManagedThingProvider managedThingProvider) {
		this.managedThingProvider = managedThingProvider;
	}

	protected void unsetManagedThingProvider(ManagedThingProvider managedThingProvider) {
		this.managedThingProvider = null;
	}

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/approve/{thingUID}")
    public Response approve(@PathParam("thingUID") String thingUID) {
        ThingUID thingUIDObject = new ThingUID(thingUID);
        List<DiscoveryResult> results = inbox.get(new InboxFilterCriteria(thingUIDObject, null));
        if (results.isEmpty()) {
            return Response.status(Status.NOT_FOUND).build();
        }
        DiscoveryResult result = results.get(0);
        Configuration conf = new Configuration(result.getProperties());
        managedThingProvider.createThing(result.getThingTypeUID(), result.getThingUID(), result.getBridgeUID(), conf);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{thingUID}")
    public Response delete(@PathParam("thingUID") String thingUID) {
        if(inbox.remove(new ThingUID(thingUID))) {
            return Response.ok().build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Produces({ MediaType.WILDCARD })
    public Response getAll() {
        List<DiscoveryResult> discoveryResults = inbox.getAll();
        Set<DiscoveryResultBean> discoveryResultBeans = convertToListBean(discoveryResults);

        return Response.ok(discoveryResultBeans).build();
    }

    @POST
    @Path("/ignore/{thingUID}")
    public Response ignore(@PathParam("thingUID") String thingUID) {
        inbox.setFlag(new ThingUID(thingUID), DiscoveryResultFlag.IGNORED);
        return Response.ok().build();
    }

    private Set<DiscoveryResultBean> convertToListBean(List<DiscoveryResult> discoveryResults) {
        Set<DiscoveryResultBean> discoveryResultBeans = new LinkedHashSet<>();
        for (DiscoveryResult discoveryResult : discoveryResults) {
            discoveryResultBeans.add(BeanMapper.mapDiscoveryResultToBean(discoveryResult));
        }
        return discoveryResultBeans;
    }

}
