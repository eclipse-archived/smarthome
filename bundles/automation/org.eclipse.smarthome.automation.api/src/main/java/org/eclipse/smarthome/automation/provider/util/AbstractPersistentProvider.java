/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.provider.util;

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.core.common.registry.AbstractManagedProvider;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * This class is base for {@link ModuleTypeProvider}s, {@link TemplateProvider}s
 * and Rule Importer.
 * <p>
 * It provides common functionality for persistence of the created {@link ModuleType}s and {@link Template}s, and
 * persists information about the vendor of the {@link ModuleType}s, {@link Template}s and {@link Rule}s.
 * <p>
 * It starts its own thread for loading the persistent {@link ModuleType}s or {@link Template}s. This provides
 * opportunity for high performance on startup of the system.
 *
 * @author Ana Dimova - Initial Contribution
 *
 * @param <E>
 *            type of the element
 * @param <K>
 *            type of the element key
 * @param <PE>
 *            type of the persistable element
 */
public abstract class AbstractPersistentProvider<E, PE> extends AbstractManagedProvider<E, String, PE>
        implements Runnable {

    /**
     * A bundle's execution context within the Framework.
     */
    protected BundleContext bc;

    /**
     * This field is an {@link AutomationFactory}. It uses for creation of
     * modules in deserializing the automation objects.
     */
    protected AutomationFactory factory;

    /**
     * This field marks the availability of the {@link StorageService} which is needed to provide the functionality for
     * persistence of the automation objects.
     */
    protected boolean storageAvailable = false;

    /**
     * This field will be set on {@code true} when the persisted objects are loaded into the memory.
     */
    protected boolean isReady = false;

    /**
     * Tracks the {@link AutomationFactory} and {@link StorageService} services.
     */
    private ServiceTracker<Object, Object> tracker;

    /**
     * This constructor is responsible for tracking the {@link AutomationFactory} service and {@link StorageService}.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for the {@link AutomationFactory} and
     *            {@link StorageService} services.
     */
    public AbstractPersistentProvider(BundleContext context) {
        bc = context;
        try {
            Filter filter = bc.createFilter("(|(objectClass=" + AutomationFactory.class.getName() + ")(objectClass="
                    + StorageService.class.getName() + "))");
            tracker = new ServiceTracker<Object, Object>(bc, filter, new ServiceTrackerCustomizer<Object, Object>() {

                @Override
                public Object addingService(ServiceReference<Object> reference) {
                    Object service = bc.getService(reference);
                    if (service != null) {
                        if (factory == null && service instanceof AutomationFactory) {
                            factory = (AutomationFactory) service;
                        }
                        if (service instanceof StorageService) {
                            setStorageService((StorageService) service);
                            storageAvailable = true;
                        }
                        if (factory != null && storageAvailable) {
                            new Thread(AbstractPersistentProvider.this,
                                    "Automation Storage Loader: " + getStorageName()).start();
                        }
                    }
                    return service;
                }

                @Override
                public void modifiedService(ServiceReference<Object> reference, Object service) {
                }

                @Override
                public void removedService(ServiceReference<Object> reference, Object service) {
                    if (service instanceof StorageService) {
                        unsetStorageService((StorageService) service);
                        storageAvailable = false;
                    } else if (service == factory) {
                        factory = null;
                    }
                }
            });
            tracker.open();
        } catch (InvalidSyntaxException notPossible) {
        }
    }

    /**
     * This method is used for loading the objects persisted in the storage, in
     * a separately executed thread with the idea of not delaying the startup of
     * the system.
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        getAll();
        setReady();
    }

    /**
     * This method is called when the bundle is stopping, to close the tracker for the {@link AutomationFactory} and
     * {@link StorageService} services.
     */
    public void close() {
        if (tracker != null) {
            tracker.close();
        }
    }

    /**
     * This method is used in {@link AutomationResourceBundlesEventQueue#open()} to ensure that all persistent objects
     * are loaded into the memory.
     *
     * @return {@code true} if all persistent objects are loaded into the memory and {@code false} in the
     *         other case.
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * This method is called when loading the storage is done to mark the end of this action.
     */
    public void setReady() {
        isReady = true;
    }

}
