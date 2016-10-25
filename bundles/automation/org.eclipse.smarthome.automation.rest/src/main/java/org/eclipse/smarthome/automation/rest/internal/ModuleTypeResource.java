/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.rest.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

import org.eclipse.smarthome.automation.dto.ActionTypeDTOMapper;
import org.eclipse.smarthome.automation.dto.ConditionTypeDTOMapper;
import org.eclipse.smarthome.automation.dto.ModuleTypeDTO;
import org.eclipse.smarthome.automation.dto.TriggerTypeDTOMapper;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.io.rest.LocaleUtil;
import org.eclipse.smarthome.io.rest.SatisfiableRESTResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class acts as a REST resource for module types and is registered with the Jersey servlet.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Markus Rathgeb - Use DTOs
 */
@Path("module-types")
@Api("module-types")
public class ModuleTypeResource implements SatisfiableRESTResource {

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
    @ApiOperation(value = "Get all available module types.", response = ModuleTypeDTO.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getAll(@HeaderParam("Accept-Language") @ApiParam(value = "language") String language,
            @QueryParam("tags") @ApiParam(value = "tags for filtering", required = false) String tagList,
            @QueryParam("type") @ApiParam(value = "filtering by action, condition or trigger", required = false) String type) {
        final Locale locale = LocaleUtil.getLocale(language);
        final String[] tags = tagList != null ? tagList.split(",") : null;
        final List<ModuleTypeDTO> modules = new ArrayList<ModuleTypeDTO>();

        if (type == null || type.equals("trigger")) {
            if (tags == null) {
                modules.addAll(TriggerTypeDTOMapper.map(moduleTypeRegistry.getTriggers(locale)));
            } else {
                modules.addAll(TriggerTypeDTOMapper.map(moduleTypeRegistry.<TriggerType> getByTags(locale, tags)));
            }
        }
        if (type == null || type.equals("condition")) {
            if (tags == null) {
                modules.addAll(ConditionTypeDTOMapper.map(moduleTypeRegistry.getConditions(locale)));
            } else {
                modules.addAll(ConditionTypeDTOMapper.map(moduleTypeRegistry.<ConditionType> getByTags(locale, tags)));
            }
        }
        if (type == null || type.equals("action")) {
            if (tags == null) {
                modules.addAll(ActionTypeDTOMapper.map(moduleTypeRegistry.getActions(locale)));
            } else {
                modules.addAll(ActionTypeDTOMapper.map(moduleTypeRegistry.<ActionType> getByTags(locale, tags)));
            }
        }
        return Response.ok(modules).build();
    }

    @GET
    @Path("/{moduleTypeUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a module type corresponding to the given UID.", response = ModuleTypeDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Module Type corresponding to the given UID does not found.") })
    public Response getByUID(@HeaderParam("Accept-Language") @ApiParam(value = "language") String language,
            @PathParam("moduleTypeUID") @ApiParam(value = "moduleTypeUID", required = true) String moduleTypeUID) {
        Locale locale = LocaleUtil.getLocale(language);
        final ModuleType moduleType = moduleTypeRegistry.get(moduleTypeUID, locale);
        if (moduleType != null) {
            return Response.ok(getModuleTypeDTO(moduleType)).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    private ModuleTypeDTO getModuleTypeDTO(final ModuleType moduleType) {
        if (moduleType instanceof ActionType) {
            return ActionTypeDTOMapper.map((ActionType) moduleType);
        } else if (moduleType instanceof ConditionType) {
            return ConditionTypeDTOMapper.map((ConditionType) moduleType);
        } else if (moduleType instanceof TriggerType) {
            return TriggerTypeDTOMapper.map((TriggerType) moduleType);
        } else {
            throw new IllegalArgumentException(
                    String.format("Cannot handle given module type class (%s)", moduleType.getClass()));
        }
    }

    @Override
    public boolean isSatisfied() {
        return moduleTypeRegistry != null;
    }

}