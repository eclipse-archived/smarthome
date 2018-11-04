/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.thing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;
import org.eclipse.smarthome.core.service.ReadyMarker;
import org.eclipse.smarthome.core.service.ReadyMarkerFilter;
import org.eclipse.smarthome.core.service.ReadyService;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.util.BundleResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * {@link ManagedThingProvider} is an OSGi service, that allows to add or remove
 * things at runtime by calling {@link ManagedThingProvider#addThing(Thing)} or
 * {@link ManagedThingProvider#removeThing(Thing)}. An added thing is
 * automatically exposed to the {@link ThingRegistry}.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Dennis Nobel - Integrated Storage
 * @author Michael Grammling - Added dynamic configuration update
 */
@Component(immediate = true, service = { ThingProvider.class, ManagedThingProvider.class })
public class ManagedThingProvider extends DefaultAbstractManagedProvider<Thing, ThingUID>
        implements ThingProvider, ReadyService.ReadyTracker {
    private static final String XML_THING_TYPE = "esh.xmlThingTypes";

    private Set<String> loadedXmlThingTypes = new CopyOnWriteArraySet<>();
    private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<ThingHandlerFactory>();

    private BundleResolver bundleResolver;

    private Collection<Thing> thingsList = new ArrayList<>();

    @Override
    protected String getStorageName() {
        return Thing.class.getName();
    }

    @Override
    protected String keyToString(ThingUID key) {
        return key.toString();
    }

    @Reference(policy = ReferencePolicy.DYNAMIC)
    @Override
    protected void setStorageService(StorageService storageService) {
        super.setStorageService(storageService);
    }

    @Override
    protected void unsetStorageService(StorageService storageService) {
        super.unsetStorageService(storageService);
    }

    @Reference
    protected void setBundleResolver(BundleResolver bundleResolver) {
        this.bundleResolver = bundleResolver;
    }

    protected void unsetBundleResolver(BundleResolver bundleResolver) {
        this.bundleResolver = null;
    }

    @Reference
    protected void setReadyService(ReadyService readyService) {
        readyService.registerTracker(this, new ReadyMarkerFilter().withType(XML_THING_TYPE));
        logger.info("set {}", readyService);
    }

    protected void unsetReadyService(ReadyService readyService) {
        readyService.unregisterTracker(this);
        logger.info("unset {}", readyService);
    }

    @Override
    public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        String bsn = readyMarker.getIdentifier();
        loadedXmlThingTypes.add(bsn);
        handleXmlThingTypesLoaded(bsn);
        logger.info("marker added {}", readyMarker);
    }

    private void handleXmlThingTypesLoaded(String bsn) {
        thingHandlerFactories.stream().filter(thingHandlerFactory -> getBundleName(thingHandlerFactory).equals(bsn))
                .forEach(thingHandlerFactory -> thingHandlerFactoryAdded(thingHandlerFactory));
    }

    @Override
    public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        String bsn = readyMarker.getIdentifier();
        loadedXmlThingTypes.remove(bsn);
        logger.info("marker removed {}", readyMarker);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        logger.debug("ThingHandlerFactory added {}", thingHandlerFactory);
        thingHandlerFactories.add(thingHandlerFactory);
        thingHandlerFactoryAdded(thingHandlerFactory);
    }

    protected void removeThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        thingHandlerFactories.remove(thingHandlerFactory);
        thingHandlerFactoryRemoved();
    }

    private void thingHandlerFactoryRemoved() {
        // Don't do anything, Things should not be deleted
    }

    private void thingHandlerFactoryAdded(ThingHandlerFactory thingHandlerFactory) {
        Collection<Thing> storageList = super.getAll();
        storageList.stream().forEach(thing -> createThingFromStorageForThingHandlerFactory(thing, thingHandlerFactory));
    }

    @Override
    public Collection<Thing> getAll() {
        return thingsList;
    }

    private void createThingFromStorageForThingHandlerFactory(Thing thing, ThingHandlerFactory factory) {
        if (!loadedXmlThingTypes.contains(getBundleName(factory))) {
            return;
        }

        if (factory.supportsThingType(thing.getThingTypeUID())) {
            if (thingsList.contains(thing)) {
                notifyListenersAboutUpdatedElement(thing, thing);
                logger.info("updated {} from {}", thing, factory);
            } else {
                thingsList.add(thing);
                notifyListenersAboutAddedElement(thing);
                logger.info("added {} from {}", thing, factory);
            }
        }
    }

    private String getBundleName(ThingHandlerFactory thingHandlerFactory) {
        return bundleResolver.resolveBundle(thingHandlerFactory.getClass()).getSymbolicName();
    }

}
