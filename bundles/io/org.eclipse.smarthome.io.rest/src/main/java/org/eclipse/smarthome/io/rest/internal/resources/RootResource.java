/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.internal.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.internal.Constants;
import org.eclipse.smarthome.io.rest.internal.resources.beans.RootBean;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class acts as an entry point / root resource for the REST API.
 * </p>
 * <p>
 * In good HATEOAS manner, it provides links to other offered resources.
 * </p>
 *
 * <p>
 * The result is returned as JSON
 * </p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
@Path("/")
public class RootResource {

    private final transient Logger logger = LoggerFactory.getLogger(RootResource.class);

    private List<RESTResource> restResources = new ArrayList<RESTResource>();

    private ConfigurationAdmin configurationAdmin;

    @Context
    UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoot(@Context HttpHeaders headers) {
        return Response.ok(getRootBean()).build();
    }

    private RootBean getRootBean() {
        RootBean bean = new RootBean();

        for (RESTResource resource : restResources) {
            String path = resource.getClass().getAnnotation(Path.class).value();
            bean.links.add(new RootBean.Links(path, uriInfo.getBaseUriBuilder().path(path).build().toASCIIString()));
        }

        return bean;
    }

    public void addRESTResource(RESTResource resource) {
        restResources.add(resource);
    }

    public void removeRESTResource(RESTResource resource) {
        restResources.remove(resource);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void activate() {
        Configuration configuration;
        try {
            configuration = configurationAdmin.getConfiguration(Constants.JAXRS_CONNECTOR_CONFIG, null);

            if (configuration != null) {
                Dictionary properties = configuration.getProperties();

                if (properties == null) {
                    properties = new Properties();
                }

                String rootAlias = (String) properties.get(Constants.JAXRS_CONNECTOR_ROOT_PROPERTY);
                if (!Constants.REST_SERVLET_ALIAS.equals(rootAlias)) {
                    properties.put(Constants.JAXRS_CONNECTOR_ROOT_PROPERTY, Constants.REST_SERVLET_ALIAS);

                    configuration.update(properties);
                }
            }
        } catch (IOException e) {
            logger.error("Could not set REST configuration properties!", e);
        }
    }

    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    protected void unsetConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = null;
    }

}
