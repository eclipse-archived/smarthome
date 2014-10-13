/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.io.mdns.MDNSService;
import org.eclipse.smarthome.io.mdns.ServiceDescription;
import org.eclipse.smarthome.io.rest.internal.resources.RootResource;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * This is the main component of the REST API; it gets all required services injected,
 * registers itself as a servlet on the HTTP service and adds the different rest resources
 * to this service.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
@ApplicationPath(RESTApplication.REST_SERVLET_ALIAS)
public class RESTApplication extends Application {

	public static final String REST_SERVLET_ALIAS = "/rest";

	private static final Logger logger = LoggerFactory.getLogger(RESTApplication.class);
	
	private int httpSSLPort;

	private int httpPort;
	
	private String mdnsName;

	private HttpService httpService;

	private MDNSService mdnsService;

	static private EventPublisher eventPublisher;

	static private ItemRegistry itemRegistry;

	static private List<RESTResource> restResources = new ArrayList<RESTResource>();

	private static BundleContext bundleContext;

	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}
	
	public void unsetHttpService(HttpService httpService) {
		this.httpService = null;
	}

	public void setEventPublisher(EventPublisher eventPublisher) {
		RESTApplication.eventPublisher = eventPublisher;
	}
	
	public void unsetEventPublisher(EventPublisher eventPublisher) {
		RESTApplication.eventPublisher = null;
	}

	static public EventPublisher getEventPublisher() {
		return eventPublisher;
	}

	public void setItemRegistry(ItemRegistry itemRegistry) {
		RESTApplication.itemRegistry = itemRegistry;
	}
	
	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		RESTApplication.itemRegistry = null;
	}

	static public ItemRegistry getItemRegistry() {
		return RESTApplication.itemRegistry;
	}
	
	public void setMDNSService(MDNSService mdnsService) {
		this.mdnsService = mdnsService;
	}
	
	public void unsetMDNSService(MDNSService mdnsService) {
		this.mdnsService = null;
	}
	
	static public BundleContext getBundleContext() {
		return RESTApplication.bundleContext;
	}

	public void addRESTResource(RESTResource resource) {
		RESTApplication.restResources.add(resource);
	}

	public void removeRESTResource(RESTResource resource) {
		RESTApplication.restResources.remove(resource);
	}

	public void activate(BundleContext bundleContext) {			    
        RESTApplication.bundleContext = bundleContext;
		try {
    		com.sun.jersey.spi.container.servlet.ServletContainer servletContainer =
    			       new ServletContainer(this);
    		
			httpService.registerServlet(REST_SERVLET_ALIAS,
					servletContainer, getJerseyServletParams(), createHttpContext());

 			logger.info("Started REST API at " + REST_SERVLET_ALIAS);

 			if (mdnsService != null) {
 				mdnsName = bundleContext.getProperty("mdnsName");
 				if(mdnsName==null) { mdnsName = "smarthome"; }
 	        	try {
 	        		httpPort = Integer.parseInt(bundleContext.getProperty("jetty.port"));
 	 				mdnsService.registerService(getDefaultServiceDescription());
 	        	} catch(NumberFormatException e) {}
 	        	try {
 	        		httpSSLPort = Integer.parseInt(bundleContext.getProperty("jetty.port.ssl"));
 	 				mdnsService.registerService(getSSLServiceDescription());
 	        	} catch(NumberFormatException e) {}
			}
        } catch (ServletException se) {
            throw new RuntimeException(se);
        } catch (NamespaceException se) {
            throw new RuntimeException(se);
        }
	}
	
	public void deactivate() {
        if (this.httpService != null) {
            httpService.unregister(REST_SERVLET_ALIAS);
            logger.info("Stopped REST API");
        }
        
        if (mdnsService != null) {
 			mdnsService.unregisterService(getDefaultServiceDescription());
			mdnsService.unregisterService(getSSLServiceDescription()); 			
 		}
        restResources.clear();
	}
	
	protected HttpContext createHttpContext() {
		HttpContext defaultHttpContext = httpService.createDefaultHttpContext();
		return defaultHttpContext;
	}
	
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> result = new HashSet<Class<?>>();
        result.add(RootResource.class);
        for(RESTResource resource : restResources) {
        	result.add(resource.getClass());
        }
        return result;
    }

    public static List<RESTResource> getRestResources() {
		return restResources;
	}

	private Dictionary<String, String> getJerseyServletParams() {
        Dictionary<String, String> jerseyServletParams = new Hashtable<String, String>();
        jerseyServletParams.put("javax.ws.rs.Application", RESTApplication.class.getName());
        // required because of bug http://java.net/jira/browse/JERSEY-361
        jerseyServletParams.put(FeaturesAndProperties.FEATURE_XMLROOTELEMENT_PROCESSING, "true");

        return jerseyServletParams;
    }
    
    private ServiceDescription getDefaultServiceDescription() {
    	Hashtable<String, String> serviceProperties = new Hashtable<String, String>();
		serviceProperties.put("uri", REST_SERVLET_ALIAS);
		return new ServiceDescription("_" + mdnsName + "-server._tcp.local.", mdnsName, httpPort, serviceProperties);
    }

    private ServiceDescription getSSLServiceDescription() {
    	ServiceDescription description = getDefaultServiceDescription();
    	description.serviceType = "_" + mdnsName + "-server-ssl._tcp.local.";
    	description.serviceName = "" + mdnsName + "-ssl";
		description.servicePort = httpSSLPort;
		return description;
    }
}
