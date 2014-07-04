/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.binding.ThingTypeChangeListener;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link XmlThingTypeProvider} is a concrete implementation of the {@link ThingTypeProvider}
 * service interface.
 * <p>
 * This implementation manages any {@link ThingType} objects associated to specific modules.
 * If a specific module disappears, any registered {@link ThingType} objects associated with
 * that module are released.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class XmlThingTypeProvider implements ThingTypeProvider {

    private Logger logger = LoggerFactory.getLogger(XmlThingTypeProvider.class);

    private Map<Bundle, List<ThingType>> bundleThingTypesMap;
    private List<ThingTypeChangeListener> thingTypeChangeListeners;


    public XmlThingTypeProvider() {
        this.bundleThingTypesMap = new HashMap<>(10);
        this.thingTypeChangeListeners = new CopyOnWriteArrayList<>();
    }

    private List<ThingType> acquireThingTypes(Bundle bundle) {
        if (bundle != null) {
            List<ThingType> thingTypes = this.bundleThingTypesMap.get(bundle);

            if (thingTypes == null) {
                thingTypes = new ArrayList<ThingType>(10);

                this.bundleThingTypesMap.put(bundle, thingTypes);
            }

            return thingTypes;
        }

        return null;
    }

    /**
     * Adds a {@link ThingType} object to the internal list associated with the specified module.
     * <p>
     * The added {@link ThingType} object leads to an event.
     * <p>
     * This method returns silently, if any of the parameters is {@code null}.
     * 
     * @param bundle the module to which the Thing type to be added
     * @param thingType the Thing type to be added
     */
    public synchronized void addThingType(Bundle bundle, ThingType thingType) {
        if (thingType != null) {
            List<ThingType> thingTypes = acquireThingTypes(bundle);

            if (thingTypes != null) {
                sendThingTypeEvent(thingType, true);
                thingTypes.add(thingType);
            }
        }
    }

    /**
     * Removes all {@link ThingType} objects from the internal list associated
     * with the specified module.
     * <p>
     * Any removed {@link ThingType} object leads to a separate event.
     * <p>
     * This method returns silently if the module is {@code null}.
     * 
     * @param bundle the module for which all associated Thing types to be removed
     */
    public synchronized void removeAllThingTypes(Bundle bundle) {
        if (bundle != null) {
            List<ThingType> thingTypes = this.bundleThingTypesMap.get(bundle);

            if (thingTypes != null) {
                for (ThingType thingType : thingTypes) {
                    sendThingTypeEvent(thingType, false);
                }

                this.bundleThingTypesMap.remove(bundle);
            }
        }
    }

    @Override
    public synchronized void addThingTypeChangeListener(ThingTypeChangeListener listener) {
        if ((listener != null) && (!this.thingTypeChangeListeners.contains(listener))) {
            this.thingTypeChangeListeners.add(listener);
        }
    }

    @Override
    public synchronized void removeThingTypeChangeListener(ThingTypeChangeListener listener) {
        if (listener != null) {
            this.thingTypeChangeListeners.remove(listener);
        }
    }

    @Override
    public synchronized Collection<ThingType> getThingTypes() {
        List<ThingType> allThingTypes = new ArrayList<>();

        Collection<List<ThingType>> thingTypes = this.bundleThingTypesMap.values();

        if (thingTypes != null) {
            for (List<ThingType> thingType : thingTypes) {
                allThingTypes.addAll(thingType);
            }
        }

        return allThingTypes;
    }

    private void sendThingTypeEvent(ThingType thingType, boolean added) {
        for (ThingTypeChangeListener listener : this.thingTypeChangeListeners) {
            try {
                if (added) {
                    listener.thingTypeAdded(this, thingType);
                } else {
                    listener.thingTypeRemoved(this, thingType);
                }
            } catch (Exception ex) {
                this.logger.error("Could not send an " + ((added) ? "added" : "removed")
                        + " ThingType event to the listener '" + listener + "'!", ex);
            }
        }
    }

}
