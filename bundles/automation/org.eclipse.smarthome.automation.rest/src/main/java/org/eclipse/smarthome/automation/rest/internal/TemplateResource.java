/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.rest.internal;

import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.SatisfiableRESTResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class acts as a REST resource for templates and is registered with the Jersey servlet.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Path("templates")
@Api("templates")
public class TemplateResource implements SatisfiableRESTResource {

    private TemplateRegistry templateRegistry;

    @Context
    private UriInfo uriInfo;

    protected void setTemplateRegistry(TemplateRegistry templateRegistry) {
        this.templateRegistry = templateRegistry;
    }

    protected void unsetTemplateRegistry(TemplateRegistry templateRegistry) {
        this.templateRegistry = null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all available templates.", response = Template.class, responseContainer = "Collection")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getAll(@HeaderParam("Accept-Language") @ApiParam(value = "language") String language) {
        Locale locale = LocaleUtil.getLocale(language);
        return Response.ok(templateRegistry.getAll(locale)).build();
    }

    @GET
    @Path("/{templateUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a template corresponding to the given UID.", response = Template.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Template corresponding to the given UID does not found.") })
    public Response getByUID(@HeaderParam("Accept-Language") @ApiParam(value = "language") String language,
            @PathParam("templateUID") @ApiParam(value = "templateUID", required = true) String templateUID) {
        Locale locale = LocaleUtil.getLocale(language);
        Template template = templateRegistry.get(templateUID, locale);
        if (template != null) {
            return Response.ok(template).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @Override
    public boolean isSatisfied() {
        return templateRegistry != null;
    }
}