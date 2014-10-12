/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.discovery;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.smarthome.io.rest.AbstractRESTResource;
import org.eclipse.smarthome.io.rest.core.discovery.beans.DiscoveryResultBean;
import org.eclipse.smarthome.io.rest.core.discovery.beans.DiscoveryResultListBean;

/**
 * This class acts as a REST resource for the inbox and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 */
@Path("inbox")
public class InboxResource extends AbstractRESTResource {

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/approve/{thingUID}")
    public Response approve(@PathParam("thingUID") String thingUID) {
        ThingUID thingUIDObject = new ThingUID(thingUID);
        Inbox inbox = getService(Inbox.class);
        List<DiscoveryResult> results = inbox.get(new InboxFilterCriteria(thingUIDObject, null));
        if (results.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        DiscoveryResult result = results.get(0);
        Configuration conf = new Configuration(result.getProperties());
        ManagedThingProvider managedThingProvider = getService(ManagedThingProvider.class);
        managedThingProvider.createThing(result.getThingTypeUID(), result.getThingUID(), result.getBridgeUID(), conf);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{thingUID}")
    public Response delete(@PathParam("thingUID") String thingUID) {
        Inbox inbox = getService(Inbox.class);
        inbox.remove(new ThingUID(thingUID));
        return Response.ok().build();
    }

    @GET
    @Produces({ MediaType.WILDCARD })
    public Response getAll() {

        Inbox inbox = getService(Inbox.class);

        List<DiscoveryResult> discoveryResults = inbox.getAll();
        DiscoveryResultListBean discoveryResultListBean = convertToListBean(discoveryResults);

        return Response.ok(discoveryResultListBean).build();
    }

    @POST
    @Path("/ignore/{thingUID}")
    public Response ignore(@PathParam("thingUID") String thingUID) {
        Inbox inbox = getService(Inbox.class);
        inbox.setFlag(new ThingUID(thingUID), DiscoveryResultFlag.IGNORED);
        return Response.ok().build();
    }

    private DiscoveryResultListBean convertToListBean(List<DiscoveryResult> discoveryResults) {
        List<DiscoveryResultBean> discoveryResultBeans = new ArrayList<>();
        for (DiscoveryResult discoveryResult : discoveryResults) {
            ThingUID thingUID = discoveryResult.getThingUID();
            ThingUID bridgeUID = discoveryResult.getBridgeUID();
            discoveryResultBeans.add(new DiscoveryResultBean(thingUID.toString(), bridgeUID != null ? bridgeUID
                    .toString() : null, discoveryResult.getLabel(), discoveryResult.getFlag(), discoveryResult
                    .getProperties()));
        }
        return new DiscoveryResultListBean(discoveryResultBeans);
    }

}
