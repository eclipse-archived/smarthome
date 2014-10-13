/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.binding;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.core.binding.BindingInfo;
import org.eclipse.smarthome.core.binding.BindingInfoRegistry;
import org.eclipse.smarthome.io.rest.AbstractRESTResource;
import org.eclipse.smarthome.io.rest.core.binding.beans.BindingInfoBean;
import org.eclipse.smarthome.io.rest.core.binding.beans.BindingInfoListBean;
import org.eclipse.smarthome.io.rest.core.thing.ThingTypeResource;
import org.eclipse.smarthome.io.rest.core.thing.beans.ThingTypeBean;

/**
 * This class acts as a REST resource for bindings and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 */
@Path("bindings")
public class BindingResource extends AbstractRESTResource {

    @Context
    UriInfo uriInfo;

    @GET
    @Produces({ MediaType.WILDCARD })
    public Response getAll() {

        BindingInfoRegistry bindingInfoRegistry = getService(BindingInfoRegistry.class);

        List<BindingInfo> bindingInfos = bindingInfoRegistry.getBindingInfos();
        BindingInfoListBean bindingInfoListBean = convertToListBean(bindingInfos);

        return Response.ok(bindingInfoListBean).build();
    }

    private BindingInfoBean convertToBindingBean(BindingInfo bindingInfo) {
        List<ThingTypeBean> thingTypeBeans = new ThingTypeResource().getThingTypeBeans(bindingInfo.getId());
        return new BindingInfoBean(bindingInfo.getId(), bindingInfo.getName(), bindingInfo.getAuthor(),
                bindingInfo.getDescription(), thingTypeBeans);
    }

    private BindingInfoListBean convertToListBean(List<BindingInfo> bindingInfos) {
        List<BindingInfoBean> bindingInfoBeans = new ArrayList<>();
        for (BindingInfo bindingInfo : bindingInfos) {
            bindingInfoBeans.add(convertToBindingBean(bindingInfo));
        }
        return new BindingInfoListBean(bindingInfoBeans);
    }

}
