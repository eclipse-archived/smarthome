/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.binding;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.core.binding.BindingInfo;
import org.eclipse.smarthome.core.binding.BindingInfoRegistry;
import org.eclipse.smarthome.core.binding.dto.BindingInfoDTO;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.RESTResource;

/**
 * This class acts as a REST resource for bindings and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector
 */
@Path("bindings")
public class BindingResource implements RESTResource {

    private BindingInfoRegistry bindingInfoRegistry;

    protected void setBindingInfoRegistry(BindingInfoRegistry bindingInfoRegistry) {
        this.bindingInfoRegistry = bindingInfoRegistry;
    }

    protected void unsetBindingInfoRegistry(BindingInfoRegistry bindingInfoRegistry) {
        this.bindingInfoRegistry = null;
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@HeaderParam("Accept-Language") String language) {
        Locale locale = LocaleUtil.getLocale(language);

        Set<BindingInfo> bindingInfos = bindingInfoRegistry.getBindingInfos(locale);
        Set<BindingInfoDTO> bindingInfoBeans = convertToListBean(bindingInfos, locale);

        return Response.ok(bindingInfoBeans).build();
    }

    private BindingInfoDTO convertToBindingBean(BindingInfo bindingInfo, Locale locale) {
        return new BindingInfoDTO(bindingInfo.getId(), bindingInfo.getName(), bindingInfo.getAuthor(),
                bindingInfo.getDescription());
    }

    private Set<BindingInfoDTO> convertToListBean(Set<BindingInfo> bindingInfos, Locale locale) {
        Set<BindingInfoDTO> bindingInfoBeans = new LinkedHashSet<>();
        for (BindingInfo bindingInfo : bindingInfos) {
            bindingInfoBeans.add(convertToBindingBean(bindingInfo, locale));
        }
        return bindingInfoBeans;
    }

}
