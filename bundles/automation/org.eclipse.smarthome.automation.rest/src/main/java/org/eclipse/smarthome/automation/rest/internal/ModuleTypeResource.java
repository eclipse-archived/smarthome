/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.rest.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a REST resource for module types and is registered with the Jersey servlet.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Path("module-types")
public class ModuleTypeResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(ModuleTypeResource.class);

    private ModuleTypeRegistry moduleTypeRegistry;

    @Context
    private UriInfo uriInfo;

    protected void setModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        this.moduleTypeRegistry = moduleTypeRegistry;
    }

    protected void unsetModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        this.moduleTypeRegistry = null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@HeaderParam("Accept-Language") String language, @QueryParam("tags") String tagList,
            @QueryParam("type") String type) {
        Locale locale = LocaleUtil.getLocale(language);
        Set<String> tags = tagList != null ? new HashSet<>(Arrays.asList(tagList.split(","))) : null;
        List<ModuleType> allModules = new ArrayList<>();
        if (type == null || type.equals("trigger")) {
            if (tags == null) {
                allModules.addAll(moduleTypeRegistry.getAll(TriggerType.class, locale));
            } else {
                Collection<TriggerType> triggers = moduleTypeRegistry.getByTags(tags, locale);
                allModules.addAll(triggers);
            }
        }
        if (type == null || type.equals("condition")) {
            if (tags == null) {
                allModules.addAll(moduleTypeRegistry.getAll(ConditionType.class, locale));
            } else {
                allModules.addAll(moduleTypeRegistry.getByTags(tags, locale));
            }
        }
        if (type == null || type.equals("action")) {
            if (tags == null) {
                allModules.addAll(moduleTypeRegistry.getAll(ActionType.class, locale));
            } else {
                allModules.addAll(moduleTypeRegistry.getByTags(tags, locale));
            }
        }
        return Response.ok(allModules).build();
    }

    @GET
    @Path("/{moduleTypeUID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByUID(@HeaderParam("Accept-Language") String language,
            @PathParam("moduleTypeUID") String moduleTypeUID) {
        Locale locale = LocaleUtil.getLocale(language);
        ModuleType moduleType = moduleTypeRegistry.get(moduleTypeUID, locale);
        if (moduleType != null) {
            return Response.ok(moduleType).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}