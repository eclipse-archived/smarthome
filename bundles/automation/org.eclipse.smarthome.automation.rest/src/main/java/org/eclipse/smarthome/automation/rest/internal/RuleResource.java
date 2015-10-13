/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.rest.internal;

import java.io.IOException;
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
import org.eclipse.smarthome.io.rest.ConfigUtil;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a REST resource for rules and is registered with the Jersey servlet.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Path("rules")
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
    public Response getAll() {
        Collection<Rule> rules = ruleRegistry.getAll();
        return Response.ok(rules).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Rule rule) throws IOException {

        try {
            ruleRegistry.add(rule);
            return Response.status(Status.CREATED).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.CONFLICT).build();
        }
    }

    @GET
    @Path("/{ruleUID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByUID(@PathParam("ruleUID") String ruleUID) {
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule != null) {
            return Response.ok(rule).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{ruleUID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("ruleUID") String ruleUID) {
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
    public Response update(@PathParam("ruleUID") String ruleUID, Rule rule) throws IOException {
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
    public Response getConfiguration(@PathParam("ruleUID") String ruleUID) throws IOException {

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
    public Response updateConfiguration(@PathParam("ruleUID") String ruleUID,
            Map<String, Object> configurationParameters) throws IOException {
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

    @GET
    @Path("/{ruleUID}/triggers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTriggers(@PathParam("ruleUID") String ruleUID) {
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
    public Response getConditions(@PathParam("ruleUID") String ruleUID) {
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
    public Response getActions(@PathParam("ruleUID") String ruleUID) {
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
    public Response getModuleById(@PathParam("ruleUID") String ruleUID,
            @PathParam("moduleCategory") String moduleCategory, @PathParam("id") String id) {
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
    public Response getModuleConfig(@PathParam("ruleUID") String ruleUID,
            @PathParam("moduleCategory") String moduleCategory, @PathParam("id") String id) {
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
    public Response getModuleConfigParam(@PathParam("ruleUID") String ruleUID,
            @PathParam("moduleCategory") String moduleCategory, @PathParam("id") String id,
            @PathParam("param") String param) {
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
    @Consumes(MediaType.TEXT_PLAIN)
    public Response setModuleConfigParam(@PathParam("ruleUID") String ruleUID,
            @PathParam("moduleCategory") String moduleCategory, @PathParam("id") String id,
            @PathParam("param") String param, String value) {
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
        switch (moduleCategory) {
            case "triggers":
                module = getTrigger(rule, id);
                break;
            case "conditions":
                module = getCondition(rule, id);
                break;
            case "actions":
                module = getAction(rule, id);
                break;
        }
        return module;
    }
}