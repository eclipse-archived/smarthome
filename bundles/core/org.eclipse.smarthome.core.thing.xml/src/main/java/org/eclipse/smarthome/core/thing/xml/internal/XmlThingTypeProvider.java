/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.core.common.osgi.ServiceBinder.Bind;
import org.eclipse.smarthome.core.common.osgi.ServiceBinder.Unbind;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.i18n.ThingTypeI18nUtil;
import org.eclipse.smarthome.core.thing.type.BridgeType;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.osgi.framework.Bundle;

/**
 * The {@link XmlThingTypeProvider} is a concrete implementation of the {@link ThingTypeProvider} service interface.
 * <p>
 * This implementation manages any {@link ThingType} objects associated to specific modules. If a specific module
 * disappears, any registered {@link ThingType} objects associated with that module are released.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Dennis Nobel - Added locale support, Added cache for localized thing types
 * @author Ivan Iliev - Added support for system wide channel types
 */
public class XmlThingTypeProvider implements ThingTypeProvider {

    private class LocalizedThingTypeKey {
        public ThingTypeUID uid;
        public String locale;

        public LocalizedThingTypeKey(ThingTypeUID uid, String locale) {
            this.uid = uid;
            this.locale = locale;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((locale == null) ? 0 : locale.hashCode());
            result = prime * result + ((uid == null) ? 0 : uid.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LocalizedThingTypeKey other = (LocalizedThingTypeKey) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (locale == null) {
                if (other.locale != null)
                    return false;
            } else if (!locale.equals(other.locale))
                return false;
            if (uid == null) {
                if (other.uid != null)
                    return false;
            } else if (!uid.equals(other.uid))
                return false;
            return true;
        }

        private XmlThingTypeProvider getOuterType() {
            return XmlThingTypeProvider.this;
        }

    }

    private Map<LocalizedThingTypeKey, ThingType> localizedThingTypeCache = new HashMap<>();

    private Map<Bundle, List<ThingType>> bundleThingTypesMap;

    private ThingTypeI18nUtil thingTypeI18nUtil;

    public XmlThingTypeProvider() {
        this.bundleThingTypesMap = new HashMap<>(10);
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
     * Adds a {@link ThingType} object to the internal list associated with the
     * specified module.
     * <p>
     * This method returns silently, if any of the parameters is {@code null}.
     *
     * @param bundle
     *            the module to which the Thing type to be added
     * @param thingType
     *            the Thing type to be added
     */
    public synchronized void addThingType(Bundle bundle, ThingType thingType) {
        if (thingType != null) {
            List<ThingType> thingTypes = acquireThingTypes(bundle);

            if (thingTypes != null) {
                thingTypes.add(thingType);
                // just make sure no old entry remains in the cache
                removeCachedEntries(thingType);
            }
        }
    }

    private ThingType createLocalizedThingType(Bundle bundle, ThingType thingType, Locale locale) {

        LocalizedThingTypeKey localizedThingTypeKey = getLocalizedThingTypeKey(thingType, locale);

        ThingType cacheEntry = localizedThingTypeCache.get(localizedThingTypeKey);
        if (cacheEntry != null) {
            return cacheEntry;
        }

        if (this.thingTypeI18nUtil != null) {
            String label = this.thingTypeI18nUtil.getLabel(bundle, thingType.getUID(), thingType.getLabel(), locale);
            String description = this.thingTypeI18nUtil.getDescription(bundle, thingType.getUID(),
                    thingType.getDescription(), locale);

            List<ChannelDefinition> localizedChannelDefinitions = new ArrayList<>(
                    thingType.getChannelDefinitions().size());

            for (ChannelDefinition channelDefinition : thingType.getChannelDefinitions()) {
                localizedChannelDefinitions.add(channelDefinition);
            }

            List<ChannelGroupDefinition> localizedChannelGroupDefinitions = new ArrayList<>(
                    thingType.getChannelGroupDefinitions().size());
            for (ChannelGroupDefinition channelGroupDefinition : thingType.getChannelGroupDefinitions()) {
                localizedChannelGroupDefinitions.add(channelGroupDefinition);
            }

            if (thingType instanceof BridgeType) {
                BridgeType bridgeType = (BridgeType) thingType;
                BridgeType localizedBridgeType = new BridgeType(bridgeType.getUID(),
                        bridgeType.getSupportedBridgeTypeUIDs(), label, description, localizedChannelDefinitions,
                        localizedChannelGroupDefinitions, thingType.getProperties(),
                        bridgeType.getConfigDescriptionURI());
                localizedThingTypeCache.put(localizedThingTypeKey, localizedBridgeType);
                return localizedBridgeType;
            } else {
                ThingType localizedThingType = new ThingType(thingType.getUID(), thingType.getSupportedBridgeTypeUIDs(),
                        label, description, localizedChannelDefinitions, localizedChannelGroupDefinitions,
                        thingType.getProperties(), thingType.getConfigDescriptionURI());
                localizedThingTypeCache.put(localizedThingTypeKey, localizedThingType);
                return localizedThingType;
            }

        }
        return thingType;
    }

    private LocalizedThingTypeKey getLocalizedThingTypeKey(ThingType thingType, Locale locale) {
        String localeString = locale != null ? locale.toLanguageTag() : null;
        LocalizedThingTypeKey localizedThingTypeKey = new LocalizedThingTypeKey(thingType.getUID(),
                locale != null ? localeString : null);
        return localizedThingTypeKey;
    }

    private void removeCachedEntries(List<ThingType> thingTypes) {
        for (ThingType thingType : thingTypes) {
            removeCachedEntries(thingType);
        }
    }

    private void removeCachedEntries(ThingType thingType) {
        for (Iterator<Entry<LocalizedThingTypeKey, ThingType>> iterator = this.localizedThingTypeCache.entrySet()
                .iterator(); iterator.hasNext();) {
            Entry<LocalizedThingTypeKey, ThingType> entry = iterator.next();
            if (entry.getKey().uid.equals(thingType.getUID())) {
                iterator.remove();
            }
        }
    }

    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        Collection<Entry<Bundle, List<ThingType>>> thingTypesList = this.bundleThingTypesMap.entrySet();

        if (thingTypesList != null) {
            for (Entry<Bundle, List<ThingType>> thingTypes : thingTypesList) {
                for (ThingType thingType : thingTypes.getValue()) {
                    if (thingType.getUID().equals(thingTypeUID)) {
                        return createLocalizedThingType(thingTypes.getKey(), thingType, locale);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public synchronized Collection<ThingType> getThingTypes(Locale locale) {
        List<ThingType> allThingTypes = new ArrayList<>(10);

        Collection<Entry<Bundle, List<ThingType>>> thingTypesList = this.bundleThingTypesMap.entrySet();

        if (thingTypesList != null) {
            for (Entry<Bundle, List<ThingType>> thingTypes : thingTypesList) {
                for (ThingType thingType : thingTypes.getValue()) {
                    ThingType localizedThingType = createLocalizedThingType(thingTypes.getKey(), thingType, locale);

                    allThingTypes.add(localizedThingType);
                }
            }
        }

        return allThingTypes;
    }

    /**
     * Removes all {@link ThingType} objects from the internal list associated
     * with the specified module.
     * <p>
     * This method returns silently if the module is {@code null}.
     *
     * @param bundle
     *            the module for which all associated Thing types to be removed
     */
    public synchronized void removeAllThingTypes(Bundle bundle) {
        if (bundle != null) {
            List<ThingType> thingTypes = this.bundleThingTypesMap.get(bundle);

            if (thingTypes != null) {
                this.bundleThingTypesMap.remove(bundle);
                removeCachedEntries(thingTypes);
            }
        }
    }

    @Bind
    public void setI18nProvider(I18nProvider i18nProvider) {
        this.thingTypeI18nUtil = new ThingTypeI18nUtil(i18nProvider);
    }

    @Unbind
    public void unsetI18nProvider(I18nProvider i18nProvider) {
        this.thingTypeI18nUtil = null;
    }
}
