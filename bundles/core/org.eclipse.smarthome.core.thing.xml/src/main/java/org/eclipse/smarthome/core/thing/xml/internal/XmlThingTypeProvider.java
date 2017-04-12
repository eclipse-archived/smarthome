/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.AbstractXmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentBundleTracker;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProviderFactory;
import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.i18n.ThingTypeI18nLocalizationService;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link XmlThingTypeProvider} is a concrete implementation of the {@link ThingTypeProvider} service interface.
 * <p>
 * This implementation manages any {@link ThingType} objects associated to specific modules. If a specific module
 * disappears, any registered {@link ThingType} objects associated with that module are released.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Dennis Nobel - Added locale support, Added cache for localized thing types
 * @author Ivan Iliev - Added support for system wide channel types
 * @author Kai Kreuzer - fixed concurrency issues
 */
@Component(immediate = true, property = { "esh.scope=core.xml" })
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
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LocalizedThingTypeKey other = (LocalizedThingTypeKey) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (locale == null) {
                if (other.locale != null) {
                    return false;
                }
            } else if (!locale.equals(other.locale)) {
                return false;
            }
            if (uid == null) {
                if (other.uid != null) {
                    return false;
                }
            } else if (!uid.equals(other.uid)) {
                return false;
            }
            return true;
        }

        private XmlThingTypeProvider getOuterType() {
            return XmlThingTypeProvider.this;
        }

    }

    private static final String XML_DIRECTORY = "/ESH-INF/thing/";

    private Map<LocalizedThingTypeKey, ThingType> localizedThingTypeCache = new ConcurrentHashMap<>();

    private Map<Bundle, List<ThingType>> bundleThingTypesMap = new ConcurrentHashMap<>(10);

    private ThingTypeI18nLocalizationService thingTypeI18nLocalizationService;

    private AbstractXmlConfigDescriptionProvider configDescriptionProvider;

    private XmlChannelTypeProvider channelTypeProvider;

    private XmlDocumentBundleTracker<List<?>> thingTypeTracker;

    @Activate
    protected void activate(ComponentContext context) {
        XmlDocumentReader<List<?>> thingTypeReader = new ThingDescriptionReader();

        XmlDocumentProviderFactory<List<?>> thingTypeProviderFactory = new ThingTypeXmlProviderFactory(
                configDescriptionProvider, this, channelTypeProvider);

        thingTypeTracker = new XmlDocumentBundleTracker<List<?>>(context.getBundleContext(), XML_DIRECTORY,
                thingTypeReader, thingTypeProviderFactory);
        thingTypeTracker.open();

    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        thingTypeTracker.close();
        thingTypeTracker = null;
    }

    private List<ThingType> acquireThingTypes(Bundle bundle) {
        if (bundle != null) {
            List<ThingType> thingTypes = this.bundleThingTypesMap.get(bundle);

            if (thingTypes == null) {
                thingTypes = new CopyOnWriteArrayList<ThingType>();

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
        // Create a localized thing type key (used for caching localized thing types).
        final LocalizedThingTypeKey localizedThingTypeKey = getLocalizedThingTypeKey(thingType, locale);

        // Check if there is already an entry in our cache.
        final ThingType cacheEntry = localizedThingTypeCache.get(localizedThingTypeKey);
        if (cacheEntry != null) {
            return cacheEntry;
        }

        // Check if there is a localization service available.
        if (thingTypeI18nLocalizationService != null) {
            // Fetch the localized thing type.
            final ThingType localizedThingType = thingTypeI18nLocalizationService.createLocalizedThingType(bundle,
                    thingType, locale);
            // Put the localized thing type in our cache, so we could reuse it.
            localizedThingTypeCache.put(localizedThingTypeKey, localizedThingType);
            return localizedThingType;
        } else {
            // There is no localization service available, return the non-localized one.
            return thingType;
        }
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

    @Reference
    public void setThingTypeI18nLocalizationService(
            final ThingTypeI18nLocalizationService thingTypeI18nLocalizationService) {
        this.thingTypeI18nLocalizationService = thingTypeI18nLocalizationService;
    }

    public void unsetThingTypeI18nLocalizationService(
            final ThingTypeI18nLocalizationService thingTypeI18nLocalizationService) {
        this.thingTypeI18nLocalizationService = null;
    }

    @Reference(target = "(esh.scope=core.xml.thing)")
    public void setConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = (AbstractXmlConfigDescriptionProvider) configDescriptionProvider;
    }

    public void unsetConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = null;
    }

    @Reference(target = "(esh.scope=core.xml)")
    public void setChannelTypeProvider(ChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = (XmlChannelTypeProvider) channelTypeProvider;
    }

    public void unsetChannelTypeProvider(ChannelTypeProvider configDescriptionProvider) {
        this.channelTypeProvider = null;
    }

}
