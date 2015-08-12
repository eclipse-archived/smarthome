/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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

import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * {@link BaseThingHandlerFactory} provides a base implementation for the {@link ThingHandlerFactory} interface. It
 * provides the OSGi service
 * registration logic.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Benedikt Niehues - fix for Bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=445137 considering
 *         default values
 *
 */
public abstract class BaseThingHandlerFactory implements ThingHandlerFactory {

    protected BundleContext bundleContext;

    private Map<String, ServiceRegistration<ThingHandler>> thingHandlers = new HashMap<>();
    private ServiceTracker<ThingTypeRegistry, ThingTypeRegistry> thingTypeRegistryServiceTracker;
    private ServiceTracker<ConfigDescriptionRegistry, ConfigDescriptionRegistry> configDescriptionRegistryServiceTracker;

    /**
     * Initializes the {@link BaseThingHandlerFactory}. If this method is
     * overridden by a sub class, the implementing method must call <code>super.activate(componentContext)</code> first.
     * 
     * @param componentContext
     *            component context (must not be null)
     */
    protected void activate(ComponentContext componentContext) {
        this.bundleContext = componentContext.getBundleContext();
        thingTypeRegistryServiceTracker = new ServiceTracker<>(bundleContext, ThingTypeRegistry.class.getName(), null);
        thingTypeRegistryServiceTracker.open();
        configDescriptionRegistryServiceTracker = new ServiceTracker<>(bundleContext,
                ConfigDescriptionRegistry.class.getName(), null);
        configDescriptionRegistryServiceTracker.open();
    }

    /**
     * Disposes the {@link BaseThingHandlerFactory}. If this method is
     * overridden by a sub class, the implementing method must call <code>super.deactivate(componentContext)</code>
     * first.
     * 
     * @param componentContext
     *            component context (must not be null)
     */
    protected void deactivate(ComponentContext componentContext) {
        for (ServiceRegistration<ThingHandler> serviceRegistration : this.thingHandlers.values()) {
            unregisterHandler(serviceRegistration);
        }
        thingTypeRegistryServiceTracker.close();
        configDescriptionRegistryServiceTracker.close();
        this.thingHandlers.clear();
        this.bundleContext = null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        ServiceRegistration<ThingHandler> serviceRegistration = thingHandlers.remove(thing.getUID().toString());
        if (serviceRegistration != null) {
            unregisterHandler(serviceRegistration);
        }
    }

    private void unregisterHandler(ServiceRegistration<ThingHandler> serviceRegistration) {
        ThingHandler thingHandler = bundleContext.getService(serviceRegistration.getReference());
        removeHandler(thingHandler);
        serviceRegistration.unregister();
        if (thingHandler instanceof BaseThingHandler) {
            ((BaseThingHandler) thingHandler).preDispose();
        }
        thingHandler.dispose();
        if (thingHandler instanceof BaseThingHandler) {
            ((BaseThingHandler) thingHandler).unsetBundleContext(bundleContext);
        }
    }

    @Override
    public void registerHandler(Thing thing, ThingHandlerCallback thingHandlerListener) {
        ThingHandler thingHandler = createHandler(thing);
        if (thingHandler == null) {
            throw new IllegalStateException(this.getClass().getSimpleName()
                    + " could not create a handler for the thing '" + thing.getUID() + "'.");
        }
        thingHandler.setCallback(thingHandlerListener);
        if (thingHandler instanceof BaseThingHandler) {
            if (bundleContext == null) {
                throw new IllegalStateException(
                        "Base thing handler factory has not been properly initialized. Did you forget to call super.activate()?");
            }
            ((BaseThingHandler) thingHandler).setBundleContext(bundleContext);
        }
        thingHandler.initialize();
        if (thingHandler instanceof BaseThingHandler) {
            ((BaseThingHandler) thingHandler).postInitialize();
        }
        
        ServiceRegistration<ThingHandler> serviceRegistration = registerAsService(thing, thingHandler);
        thingHandlers.put(thing.getUID().toString(), serviceRegistration);
    }

    private ServiceRegistration<ThingHandler> registerAsService(Thing thing, ThingHandler thingHandler) {

        Dictionary<String, Object> serviceProperties = getServiceProperties(thing, thingHandler);

        @SuppressWarnings("unchecked")
        ServiceRegistration<ThingHandler> serviceRegistration = (ServiceRegistration<ThingHandler>) bundleContext
                .registerService(ThingHandler.class.getName(), thingHandler, serviceProperties);

        return serviceRegistration;
    }

    private Dictionary<String, Object> getServiceProperties(Thing thing, ThingHandler thingHandler) {
        Dictionary<String, Object> serviceProperties = new Hashtable<>();

        serviceProperties.put(ThingHandler.SERVICE_PROPERTY_THING_ID, thing.getUID());
        serviceProperties.put(ThingHandler.SERVICE_PROPERTY_THING_TYPE, thing.getThingTypeUID().toString());

        Map<String, Object> additionalServiceProperties = getServiceProperties(thingHandler);
        if (additionalServiceProperties != null) {
            for (Entry<String, Object> additionalServiceProperty : additionalServiceProperties.entrySet()) {
                serviceProperties.put(additionalServiceProperty.getKey(), additionalServiceProperty.getValue());
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
     * The method implementation must create and return the {@link ThingHandler} for the given thing.
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
     * @param thingHandler
     *            thing handler to be removed
     */
    protected void removeHandler(ThingHandler thingHandler) {
        // can be overridden
    }

    @Override
    public void removeThing(ThingUID thingUID) {
        // can be overridden
    }

    /**
     * Returns the {@link ThingType} which is represented by the given {@link ThingTypeUID}.
     * 
     * @param thingTypeUID the unique id of the thing type
     * @return the thing type represented by the given unique id
     */
    protected ThingType getThingTypeByUID(ThingTypeUID thingTypeUID) {
        if (thingTypeRegistryServiceTracker == null) {
            throw new IllegalStateException(
                    "Base thing handler factory has not been properly initialized. Did you forget to call super.activate()?");
        }
        ThingTypeRegistry thingTypeRegistry = thingTypeRegistryServiceTracker.getService();
        if (thingTypeRegistry != null) {
            return thingTypeRegistry.getThingType(thingTypeUID);
        }
        return null;
    }

    /**
     * Creates a thing based on given thing type uid.
     *
     * @param thingTypeUID
     *            thing type uid (can not be null)
     * @param thingUID
     *            thingUID (can not be null)
     * @param configuration
     *            (can not be null)
     * @return thing (can be null, if thing type is unknown)
     */
    protected Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID) {
        return createThing(thingTypeUID, configuration, thingUID, null);
    }

    /**
     * Creates a thing based on given thing type uid.
     *
     * @param thingTypeUID
     *            thing type uid (must not be null)
     * @param thingUID
     *            thingUID (can be null)
     * @param configuration
     *            (must not be null)
     * @param bridgeUID
     *            (can be null)
     * @return thing (can be null, if thing type is unknown)
     */
    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if(thingTypeUID == null) {
            throw new IllegalArgumentException("Thing Type UID must not be null");
        }
        if(thingUID == null) {
            thingUID = ThingFactory.generateRandomThingUID(thingTypeUID);
        }
        ThingType thingType = getThingTypeByUID(thingTypeUID);
        if (thingType != null) {
            Thing thing = ThingFactory.createThing(thingType, thingUID, configuration, bridgeUID,
                    getConfigDescriptionRegistry());
            return thing;
        } else {
            return null;
        }
    }

    protected ConfigDescriptionRegistry getConfigDescriptionRegistry() {
        if (configDescriptionRegistryServiceTracker == null) {
            throw new IllegalStateException(
                    "Config Description Registry has not been properly initialized. Did you forget to call super.activate()?");
        }
        return configDescriptionRegistryServiceTracker.getService();
    }

}