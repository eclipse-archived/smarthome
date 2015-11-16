/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.core.internal.RESTCoreActivator;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * {@link ConfigurableServiceResource} provides access to configurable services. It lists the available services and
 * allows to get, update and delete the configuration for a service ID. See also {@link ConfigurableService}.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
@Path(ConfigurableServiceResource.PATH_SERVICES)
@Api
public class ConfigurableServiceResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_SERVICES = "services";

    private static final String CONFIGURABLE_SERVICE_FILTER = "(" + ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI
            + "=*)";

    private final Logger logger = LoggerFactory.getLogger(ConfigurableServiceResource.class);

    private ConfigurationAdmin configurationAdmin;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Get all configurable services.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public List<ConfigurableServiceDTO> getAll() {
        List<ConfigurableServiceDTO> services = getConfigurableServices();
        return services;
    }

    @GET
    @Path("/{serviceId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Get configurable service for given service ID.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 404, message = "Not found") })
    public Response getById(@PathParam("serviceId") @ApiParam(value = "service ID", required = true) String serviceId) {
        List<ConfigurableServiceDTO> configurableServices = getConfigurableServices();
        for (ConfigurableServiceDTO configurableService : configurableServices) {
            if (configurableService.id.equals(serviceId)) {
                return Response.ok(configurableService).build();
            }
        }
        return Response.status(404).build();
    }

    @GET
    @Path("/{serviceId}/config")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Get service configuration for given service ID.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Configuration can not be read due to internal error") })
    public Response getConfiguration(
            @PathParam("serviceId") @ApiParam(value = "service ID", required = true) String serviceId) {
        try {
            Configuration servieConfiguration = configurationAdmin.getConfiguration(serviceId);
            Dictionary<String, Object> properties = servieConfiguration.getProperties();
            Map<String, Object> configuration = toMap(properties);
            return configuration != null ? Response.ok(configuration).build() : Response.status(404).build();
        } catch (IOException ex) {
            logger.error("Cannot get configuration for service {}: " + ex.getMessage(), serviceId, ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{serviceId}/config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Updates a service configuration for given service ID and returns the old configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 204, message = "No old configuration"),
            @ApiResponse(code = 500, message = "Configuration can not be updated due to internal error") })
    public Response updateConfiguration(
            @PathParam("serviceId") @ApiParam(value = "service ID", required = true) String serviceId,
            Map<String, Object> configuration) {
        try {
            Configuration serviceConfiguration = configurationAdmin.getConfiguration(serviceId);
            Map<String, Object> oldConfiguration = toMap(serviceConfiguration.getProperties());
            Dictionary<String, Object> properties = getProperties(serviceConfiguration);
            Set<Entry<String, Object>> configurationParameters = configuration.entrySet();
            for (Entry<String, Object> configurationParameter : configurationParameters) {
                properties.put(configurationParameter.getKey(), configurationParameter.getValue());
            }
            serviceConfiguration.update(properties);
            return oldConfiguration != null ? Response.ok(oldConfiguration).build() : Response.noContent().build();
        } catch (IOException ex) {
            logger.error("Cannot update configuration for service {}: " + ex.getMessage(), serviceId, ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{serviceId}/config")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Deletes a service configuration for given service ID and returns the old configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 204, message = "No old configuration"),
            @ApiResponse(code = 500, message = "Configuration can not be deleted due to internal error") })
    public Response deleteConfiguration(
            @PathParam("serviceId") @ApiParam(value = "service ID", required = true) String serviceId) {
        try {
            Configuration serviceConfiguration = configurationAdmin.getConfiguration(serviceId);
            Map<String, Object> oldConfiguration = toMap(serviceConfiguration.getProperties());
            serviceConfiguration.delete();
            return oldConfiguration != null ? Response.ok(oldConfiguration).build() : Response.noContent().build();
        } catch (IOException ex) {
            logger.error("Cannot delete configuration for service {}: " + ex.getMessage(), serviceId, ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<ConfigurableServiceDTO> getConfigurableServices() {
        List<ConfigurableServiceDTO> services = new ArrayList<>();
        try {
            ServiceReference<?>[] serviceReferences = RESTCoreActivator.getBundleContext()
                    .getServiceReferences((String) null, CONFIGURABLE_SERVICE_FILTER);
            if (serviceReferences != null) {
                for (ServiceReference<?> serviceReference : serviceReferences) {
                    String id = getServiceId(serviceReference);
                    String label = (String) serviceReference.getProperty(ConfigurableService.SERVICE_PROPERTY_LABEL);
                    String category = (String) serviceReference
                            .getProperty(ConfigurableService.SERVICE_PROPERTY_CATEGORY);
                    String configDescriptionURI = (String) serviceReference
                            .getProperty(ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI);
                    services.add(new ConfigurableServiceDTO(id, label, category, configDescriptionURI));
                }
            }
        } catch (InvalidSyntaxException ex) {
            logger.error("Cannot get service references, because syntax is invalid: " + ex.getMessage(), ex);
        }
        return services;
    }

    private Dictionary<String, Object> getProperties(Configuration serviceConfiguration) {
        Dictionary<String, Object> properties = serviceConfiguration.getProperties();
        return properties != null ? properties : new Hashtable<String, Object>();
    }

    private String getServiceId(ServiceReference<?> serviceReference) {
        Object pid = serviceReference.getProperty(Constants.SERVICE_PID);
        if (pid != null) {
            return (String) pid;
        } else {
            return (String) serviceReference.getProperty(ComponentConstants.COMPONENT_NAME);
        }
    }

    private Map<String, Object> toMap(Dictionary<String, Object> dictionary) {
        if (dictionary == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>(dictionary.size());
        Enumeration<String> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (!key.equals(Constants.SERVICE_PID)) {
                map.put(key, dictionary.get(key));
            }
        }
        return map;
    }

    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    protected void unsetConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = null;
    }

}
