/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

/**
 * {@link BaseThingHandlerFactory} provides a base implementation for the
 * {@link ThingHandlerFactory} interface. It provides the OSGi service
 * registration logic.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 */
public abstract class BaseThingHandlerFactory implements ThingHandlerFactory {

    private Map<String, ServiceRegistration<ThingHandler>> thingHandlers = new HashMap<>();
    private BundleContext bundleContext;

    protected void activate(ComponentContext componentContext) {
        this.bundleContext = componentContext.getBundleContext();
    }

    protected void deactivate(ComponentContext componentContext) {
        for (ServiceRegistration<ThingHandler> serviceRegistration : this.thingHandlers.values()) {

            ThingHandler thingHandler = (ThingHandler) bundleContext.getService(serviceRegistration
                    .getReference());
            thingHandler.dispose();
        }

        this.bundleContext = null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        ServiceRegistration<ThingHandler> serviceRegistration = thingHandlers.remove(thing.getUID().toString());
        if (serviceRegistration != null) {
            ThingHandler thingHandler = bundleContext
                    .getService(serviceRegistration.getReference());
            serviceRegistration.unregister();
            removeHandler(thingHandler);
            thingHandler.dispose();
        }
    }

    @Override
    public void registerHandler(Thing thing) {

        ThingHandler thingHandler = createHandler(thing);

        ServiceRegistration<ThingHandler> serviceRegistration = registerAsService(thing,
                thingHandler);
        thingHandlers.put(thing.getUID().toString(), serviceRegistration);

        thingHandler.initialize();

    }

    private ServiceRegistration<ThingHandler> registerAsService(Thing thing,
            ThingHandler thingHandler) {

        Dictionary<String, Object> serviceProperties = getServiceProperties(thing, thingHandler);

        @SuppressWarnings("unchecked")
        ServiceRegistration<ThingHandler> serviceRegistration = (ServiceRegistration<ThingHandler>) bundleContext
                .registerService(ThingHandler.class.getName(), thingHandler, serviceProperties);

        return serviceRegistration;
    }

    private Dictionary<String, Object> getServiceProperties(Thing thing, ThingHandler thingHandler) {
        Dictionary<String, Object> serviceProperties = new Hashtable<>();

        serviceProperties.put(ThingHandler.SERVICE_PROPERTY_THING_ID, thing.getUID());
        serviceProperties.put(ThingHandler.SERVICE_PROPERTY_THING_TYPE, thing.getThingTypeUID()
                .toString());

        Map<String, Object> additionalServiceProperties = getServiceProperties(thingHandler);
        if (additionalServiceProperties != null) {
            for (Entry<String, Object> additionalServiceProperty : additionalServiceProperties
                    .entrySet()) {
                serviceProperties.put(additionalServiceProperty.getKey(),
                        additionalServiceProperty.getValue());
            }
        }
        return serviceProperties;
    }

    /**
     * This method can be overridden to append additional service properties to
     * the registered OSGi {@link ThingHandler} service.
     * 
     * @param thingHandler
     *            thing handler, which will be registered as OSGi service
     * @return map of additional service properties
     */
    protected Map<String, Object> getServiceProperties(ThingHandler thingHandler) {
        return null;
    }

    /**
     * The method implementation must create and return the {@link ThingHandler}
     * for the given thing.
     * 
     * @param thing
     *            thing
     * @return thing handler
     */
    protected abstract ThingHandler createHandler(Thing thing);

    /**
     * This method is called when a thing handler should be removed. The
     * implementing caller can override this method to release specific
     * resources.
     * 
     * @param thing
     *            thing
     */
    protected void removeHandler(ThingHandler thingHandler) {
        // can be overridden
    }

    @Override
    public void removeThing(ThingUID thingUID) {
        // can be overridden
    }

}