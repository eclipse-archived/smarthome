/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
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

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.ConfigUtil;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.auth.Role;
import org.eclipse.smarthome.io.rest.SatisfiableRESTResource;
import org.eclipse.smarthome.io.rest.core.config.ConfigurationService;
import org.eclipse.smarthome.io.rest.core.internal.RESTCoreActivator;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
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
@RolesAllowed({ Role.ADMIN })
@Api(value = ConfigurableServiceResource.PATH_SERVICES)
public class ConfigurableServiceResource implements SatisfiableRESTResource {

    /** The URI path to this resource */
    public static final String PATH_SERVICES = "services";

    private static final String CONFIGURABLE_SERVICE_FILTER = "(" + ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI
            + "=*)";

    private final Logger logger = LoggerFactory.getLogger(ConfigurableServiceResource.class);

    private ConfigurationService configurationService;
    private ConfigDescriptionRegistry configDescRegistry;

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
        ConfigurableServiceDTO configurableService = getServiceById(serviceId);
        if (configurableService != null) {
            return Response.ok(configurableService).build();
        } else {
            return Response.status(404).build();
        }
    }

    private ConfigurableServiceDTO getServiceById(String serviceId) {
        List<ConfigurableServiceDTO> configurableServices = getConfigurableServices();
        for (ConfigurableServiceDTO configurableService : configurableServices) {
            if (configurableService.id.equals(serviceId)) {
                return configurableService;
            }
        }
        return null;
    }

    @GET
    @Path("/{serviceId}/config")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Get service configuration for given service ID.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Configuration can not be read due to internal error") })
    public Response getConfiguration(
            @PathParam("serviceId") @ApiParam(value = "service ID", required = true) String serviceId) {
        try {
            Configuration configuration = configurationService.get(serviceId);
            return configuration != null ? Response.ok(configuration.getProperties()).build()
                    : Response.ok(Collections.emptyMap()).build();
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
            Configuration oldConfiguration = configurationService.get(serviceId);
            configurationService.update(serviceId, new Configuration(normalizeConfiguration(configuration, serviceId)));
            return oldConfiguration != null ? Response.ok(oldConfiguration.getProperties()).build()
                    : Response.noContent().build();
        } catch (IOException ex) {
            logger.error("Cannot update configuration for service {}: " + ex.getMessage(), serviceId, ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Map<String, Object> normalizeConfiguration(Map<String, Object> properties, String serviceId) {
        if (properties == null || properties.isEmpty()) {
            return properties;
        }

        ConfigurableServiceDTO service = getServiceById(serviceId);
        if (service == null) {
            return properties;
        }

        URI uri;
        try {
            uri = new URI(service.configDescriptionURI);
        } catch (URISyntaxException e) {
            logger.warn("Not a valid URI: {}", service.configDescriptionURI);
            return properties;
        }

        ConfigDescription configDesc = configDescRegistry.getConfigDescription(uri);
        if (configDesc == null) {
            return properties;
        }

        return ConfigUtil.normalizeTypes(properties, configDesc);
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
            Configuration oldConfiguration = configurationService.get(serviceId);
            configurationService.delete(serviceId);
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

    private String getServiceId(ServiceReference<?> serviceReference) {
        Object pid = serviceReference.getProperty(Constants.SERVICE_PID);
        if (pid != null) {
            return (String) pid;
        } else {
            return (String) serviceReference.getProperty(ComponentConstants.COMPONENT_NAME);
        }
    }

    protected void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    protected void unsetConfigurationService(ConfigurationService configurationService) {
        this.configurationService = null;
    }

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescRegistry = null;
    }

    @Override
    public boolean isSatisfied() {
        return configurationService != null && configDescRegistry != null;
    }

}
