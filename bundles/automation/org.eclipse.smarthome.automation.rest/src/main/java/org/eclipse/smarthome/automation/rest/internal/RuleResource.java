/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.rest.internal;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.rest.internal.dto.EnrichedRuleDTO;
import org.eclipse.smarthome.io.rest.ConfigUtil;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

/**
 * This class acts as a REST resource for rules and is registered with the Jersey servlet.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Path("rules")
@Api("rules")
public class RuleResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(RuleResource.class);

    private RuleRegistry ruleRegistry;

    @Context
    private UriInfo uriInfo;

    protected void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    protected void unsetRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all available rules.", response = EnrichedRuleDTO.class, responseContainer = "Collection")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getAll() {
        Collection<EnrichedRuleDTO> rules = enrich(ruleRegistry.getAll());
        return Response.ok(rules).build();
    }

    private Collection<EnrichedRuleDTO> enrich(Collection<Rule> rules) {
        Collection<EnrichedRuleDTO> enrichedRules = new ArrayList<EnrichedRuleDTO>(rules.size());
        for (Rule rule : rules) {
            enrichedRules.add(enrich(rule));
        }
        return enrichedRules;
    }

    private EnrichedRuleDTO enrich(Rule rule) {
        EnrichedRuleDTO enrichedRule = new EnrichedRuleDTO(rule);
        enrichedRule.enabled = ruleRegistry.isEnabled(rule.getUID());
        enrichedRule.status = ruleRegistry.getStatus(rule.getUID());
        return enrichedRule;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a rule.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", responseHeaders = @ResponseHeader(name = "Location", description = "Newly created Rule", response = String.class) ),
            @ApiResponse(code = 409, message = "Creation of the rule is refused. Rule with the same UID already exists."),
            @ApiResponse(code = 400, message = "Creation of the rule is refused. Missing required parameter.") })
    public Response create(@ApiParam(value = "rule data", required = true) Rule rule) throws IOException {
        try {
            Rule newRule = ruleRegistry.add(rule);
            return Response.status(Status.CREATED)
                    .header("Location", "rules/" + URLEncoder.encode(newRule.getUID(), "UTF-8")).build();

        } catch (IllegalArgumentException e) {
            String errMessage = "Creation of the rule is refused: " + e.getMessage();
            logger.warn(errMessage);
            return JSONResponse.createErrorResponse(Status.CONFLICT, errMessage);

        } catch (RuntimeException e) {
            String errMessage = "Creation of the rule is refused: " + e.getMessage();
            logger.warn(errMessage);
            return JSONResponse.createErrorResponse(Status.BAD_REQUEST, errMessage);
        }
    }

    @GET
    @Path("/{ruleUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the rule corresponding to the given UID.", response = Rule.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule not found") })
    public Response getByUID(@PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule != null) {
            return Response.ok(enrich(rule)).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{ruleUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Removes an existing rule corresponding to the given UID.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found.") })
    public Response remove(@PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID) {
        Rule removedRule = ruleRegistry.remove(ruleUID);
        if (removedRule == null) {
            logger.info("Received HTTP DELETE request at '{}' for the unknown rule '{}'.", uriInfo.getPath(), ruleUID);
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok().build();
    }

    @PUT
    @Path("/{ruleUID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates an existing rule corresponding to the given UID.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found.") })
    public Response update(@PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID,
            @ApiParam(value = "rule data", required = true) Rule rule) throws IOException {
        Rule oldRule = ruleRegistry.update(rule);
        if (oldRule == null) {
            logger.info("Received HTTP PUT request for update at '{}' for the unknown rule '{}'.", uriInfo.getPath(),
                    ruleUID);
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok().build();
    }

    @GET
    @Path("/{ruleUID}/config")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the rule configuration values.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found.") })
    public Response getConfiguration(@PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID)
            throws IOException {

        Rule rule = ruleRegistry.get(ruleUID);
        if (rule == null) {
            logger.info("Received HTTP GET request for config at '{}' for the unknown rule '{}'.", uriInfo.getPath(),
                    ruleUID);
            return Response.status(Status.NOT_FOUND).build();
        } else {
            return Response.ok(rule.getConfiguration()).build();
        }
    }

    @PUT
    @Path("/{ruleUID}/config")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Sets the rule configuration values.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found.") })
    public Response updateConfiguration(
            @PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID,
            @ApiParam(value = "config") Map<String, Object> configurationParameters) throws IOException {
        Map<String, Object> config = ConfigUtil.normalizeTypes(configurationParameters);
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule == null) {
            logger.info("Received HTTP PUT request for update config at '{}' for the unknown rule '{}'.",
                    uriInfo.getPath(), ruleUID);
            return Response.status(Status.NOT_FOUND).build();
        } else {
            rule.setConfiguration(config);
            ruleRegistry.update(rule);
            return Response.ok().build();
        }
    }

    @POST
    @Path("/{ruleUID}/enable")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Sets the rule enabled status.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found.") })
    public Response enableRule(@PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID,
            @ApiParam(value = "enable", required = true) String enabled) throws IOException {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule == null) {
            logger.info("Received HTTP PUT request for update config at '{}' for the unknown rule '{}'.",
                    uriInfo.getPath(), ruleUID);
            return Response.status(Status.NOT_FOUND).build();
        } else {
            ruleRegistry.setEnabled(ruleUID, !"false".equalsIgnoreCase(enabled));
            ruleRegistry.update(rule);
            return Response.ok().build();
        }
    }

    @GET
    @Path("/{ruleUID}/triggers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the rule triggers.", response = Trigger.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found.") })
    public Response getTriggers(@PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule != null) {
            return Response.ok(rule.getTriggers()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{ruleUID}/conditions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the rule conditions.", response = Condition.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found.") })
    public Response getConditions(@PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule != null) {
            return Response.ok(rule.getConditions()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{ruleUID}/actions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the rule actions.", response = Action.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found.") })
    public Response getActions(@PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule != null) {
            return Response.ok(rule.getActions()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{ruleUID}/{moduleCategory}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the rule's module corresponding to the given Category and ID.", response = Module.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found or does not have a module with such Category and ID.") })
    public Response getModuleById(@PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID,
            @PathParam("moduleCategory") @ApiParam(value = "moduleCategory", required = true) String moduleCategory,
            @PathParam("id") @ApiParam(value = "id", required = true) String id) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule != null) {
            Module module = getModule(rule, moduleCategory, id);
            if (module != null) {
                return Response.ok(module).build();
            }
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{ruleUID}/{moduleCategory}/{id}/config")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the module's configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found or does not have a module with such Category and ID.") })
    public Response getModuleConfig(@PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID,
            @PathParam("moduleCategory") @ApiParam(value = "moduleCategory", required = true) String moduleCategory,
            @PathParam("id") @ApiParam(value = "id", required = true) String id) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule != null) {
            Module module = getModule(rule, moduleCategory, id);
            if (module != null) {
                return Response.ok(module.getConfiguration()).build();
            }
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{ruleUID}/{moduleCategory}/{id}/config/{param}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Gets the module's configuration parameter.", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found or does not have a module with such Category and ID.") })
    public Response getModuleConfigParam(
            @PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID,
            @PathParam("moduleCategory") @ApiParam(value = "moduleCategory", required = true) String moduleCategory,
            @PathParam("id") @ApiParam(value = "id", required = true) String id,
            @PathParam("param") @ApiParam(value = "param", required = true) String param) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule != null) {
            Module module = getModule(rule, moduleCategory, id);
            if (module != null) {
                return Response.ok(module.getConfiguration().get(param)).build();
            }
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{ruleUID}/{moduleCategory}/{id}/config/{param}")
    @ApiOperation(value = "Sets the module's configuration parameter value.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Rule corresponding to the given UID does not found or does not have a module with such Category and ID.") })
    @Consumes(MediaType.TEXT_PLAIN)
    public Response setModuleConfigParam(
            @PathParam("ruleUID") @ApiParam(value = "ruleUID", required = true) String ruleUID,
            @PathParam("moduleCategory") @ApiParam(value = "moduleCategory", required = true) String moduleCategory,
            @PathParam("id") @ApiParam(value = "id", required = true) String id,
            @PathParam("param") @ApiParam(value = "param", required = true) String param,
            @ApiParam(value = "value", required = true) String value) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule != null) {
            Module module = getModule(rule, moduleCategory, id);
            if (module != null) {
                Map<String, Object> configuration = module.getConfiguration();
                configuration.put(param, ConfigUtil.normalizeType(value));
                module.setConfiguration(configuration);
                ruleRegistry.update(rule);
                return Response.ok().build();
            }
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    protected Trigger getTrigger(Rule rule, String id) {
        for (Trigger trigger : rule.getTriggers()) {
            if (trigger.getId().equals(id)) {
                return trigger;
            }
        }
        return null;
    }

    protected Condition getCondition(Rule rule, String id) {
        for (Condition condition : rule.getConditions()) {
            if (condition.getId().equals(id)) {
                return condition;
            }
        }
        return null;
    }

    protected Action getAction(Rule rule, String id) {
        for (Action action : rule.getActions()) {
            if (action.getId().equals(id)) {
                return action;
            }
        }
        return null;
    }

    protected Module getModule(Rule rule, String moduleCategory, String id) {
        Module module = null;
        if (moduleCategory.equals("triggers")) {
            module = getTrigger(rule, id);
        } else if (moduleCategory.equals("conditions")) {
            module = getCondition(rule, id);
        } else if (moduleCategory.equals("actions")) {
            module = getAction(rule, id);
        }
        return module;
    }
}