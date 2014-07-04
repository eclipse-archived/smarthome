/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeChangeListener;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * The {@link ThingTypeRegistry} tracks all {@link ThingType}s provided by registered {@link ThingTypeProvider}s.
 * 
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class ThingTypeRegistry implements ThingTypeChangeListener {

    private Logger logger = LoggerFactory.getLogger(ThingTypeRegistry.class
			.getName());
	
	private Map<ThingTypeProvider, Collection<ThingType>> thingTypeMap = new ConcurrentHashMap<>();
	
    /**
     * Returns all thing types.
     * 
     * @return all thing types
     */
	public List<ThingType> getThingTypes() {
        return Collections.unmodifiableList(Lists.newArrayList(Iterables.concat(thingTypeMap
                .values())));
    }
	
    /**
     * Returns thing types for a given binding id.
     * 
     * @param bindingId
     *            binding id
     * @return thing types for given binding id
     */
	public List<ThingType> getThingTypes(String bindingId) {
        List<ThingType> thingTypesForBinding = Lists.newArrayList();

        for (ThingType thingType : getThingTypes()) {
            if (thingType.getBindingId().equals(bindingId)) {
                thingTypesForBinding.add(thingType);
            }
        }

        return thingTypesForBinding;
    }

    /**
     * Returns a thing type for a given thing type UID.
     * 
     * @param thingTypeUID
     *            thing type UID
     * @return thing type for given UID or null if no thing type with this UID
     *         was found
     */
    public ThingType getThingType(ThingTypeUID thingTypeUID) {

        for (ThingType thingType : getThingTypes()) {
            if (thingType.getUID().equals(thingTypeUID)) {
                return thingType;
            }
        }

        return null;
    }
	
	@Override
    public void thingTypeAdded(ThingTypeProvider provider, ThingType thingType) {
        Collection<ThingType> thingTypes = thingTypeMap.get(provider);
        if (thingTypes != null) {
            thingTypes.add(thingType);
        }
    }
	
	@Override
    public void thingTypeRemoved(ThingTypeProvider provider, ThingType thingType) {
        Collection<ThingType> thingTypes = thingTypeMap.get(provider);
        if (thingTypes != null) {
            thingTypes.remove(thingType);
        }
    }
	
    protected void addThingTypeProvider(ThingTypeProvider thingTypeProvider) {
        // only add this provider if it does not already exist
        if (!thingTypeMap.containsKey(thingTypeProvider)) {
            Collection<ThingType> thingTypes = new CopyOnWriteArraySet<>(
                    thingTypeProvider.getThingTypes());
            thingTypeProvider.addThingTypeChangeListener(this);
            thingTypeMap.put(thingTypeProvider, thingTypes);
            logger.debug("Thing type provider '{}' has been added.", thingTypeProvider.getClass()
                    .getName());
        }
    }
    

    protected void removeThingTypeProvider(ThingTypeProvider thingTypeProvider) {
        if (thingTypeMap.containsKey(thingTypeProvider)) {
            thingTypeMap.remove(thingTypeProvider);
            thingTypeProvider.removeThingTypeChangeListener(this);
            logger.debug("Thing type provider '{}' has been removed.", thingTypeProvider.getClass()
                    .getName());
        }
    }

}
