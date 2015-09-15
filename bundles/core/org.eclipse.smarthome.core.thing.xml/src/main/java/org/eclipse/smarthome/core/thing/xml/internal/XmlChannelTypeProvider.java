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
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.i18n.ThingTypeI18nUtil;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.framework.Bundle;

/**
 * {@link XmlChannelTypeProvider} provides channel types from XML files.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class XmlChannelTypeProvider implements ChannelTypeProvider {

    private class LocalizedChannelTypeKey {
        public String locale;
        public UID uid;

        public LocalizedChannelTypeKey(UID uid, String locale) {
            this.uid = uid;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LocalizedChannelTypeKey other = (LocalizedChannelTypeKey) obj;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((locale == null) ? 0 : locale.hashCode());
            result = prime * result + ((uid == null) ? 0 : uid.hashCode());
            return result;
        }

        private XmlChannelTypeProvider getOuterType() {
            return XmlChannelTypeProvider.this;
        }

    }

    private Map<Bundle, List<ChannelGroupType>> bundleChannelGroupTypesMap;

    private Map<Bundle, List<ChannelType>> bundleChannelTypesMap;

    private Map<LocalizedChannelTypeKey, ChannelGroupType> localizedChannelGroupTypeCache = new HashMap<>();
    private Map<LocalizedChannelTypeKey, ChannelType> localizedChannelTypeCache = new HashMap<>();

    private ThingTypeI18nUtil thingTypeI18nUtil;

    public XmlChannelTypeProvider() {
        this.bundleChannelTypesMap = new HashMap<>(10);
        this.bundleChannelGroupTypesMap = new HashMap<>(10);
    }

    public synchronized void addChannelGroupType(Bundle bundle, ChannelGroupType channelGroupType) {
        if (channelGroupType != null) {
            List<ChannelGroupType> channelGroupTypes = acquireChannelGroupTypes(bundle);

            if (channelGroupTypes != null) {
                channelGroupTypes.add(channelGroupType);
                // just make sure no old entry remains in the cache
                removeCachedChannelGroupTypes(channelGroupType);
            }
        }
    }

    public synchronized void addChannelType(Bundle bundle, ChannelType channelType) {
        if (channelType != null) {
            List<ChannelType> channelTypes = acquireChannelTypes(bundle);

            if (channelTypes != null) {
                channelTypes.add(channelType);
                // just make sure no old entry remains in the cache
                removeCachedChannelTypes(channelType);
            }
        }
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        Collection<Entry<Bundle, List<ChannelGroupType>>> channelGroupTypesList = this.bundleChannelGroupTypesMap
                .entrySet();

        if (channelGroupTypesList != null) {
            for (Entry<Bundle, List<ChannelGroupType>> channelGroupTypes : channelGroupTypesList) {
                for (ChannelGroupType channelGroupType : channelGroupTypes.getValue()) {
                    if (channelGroupType.getUID().equals(channelGroupTypeUID)) {
                        return createLocalizedChannelGroupType(channelGroupTypes.getKey(), channelGroupType, locale);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        List<ChannelGroupType> allChannelGroupTypes = new ArrayList<>(10);

        Collection<Entry<Bundle, List<ChannelGroupType>>> channelGroupTypesList = this.bundleChannelGroupTypesMap
                .entrySet();

        if (channelGroupTypesList != null) {
            for (Entry<Bundle, List<ChannelGroupType>> channelGroupTypes : channelGroupTypesList) {
                for (ChannelGroupType channelGroupType : channelGroupTypes.getValue()) {
                    ChannelGroupType localizedChannelType = createLocalizedChannelGroupType(channelGroupTypes.getKey(),
                            channelGroupType, locale);
                    allChannelGroupTypes.add(localizedChannelType);
                }
            }
        }

        return allChannelGroupTypes;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        Collection<Entry<Bundle, List<ChannelType>>> channelTypesList = this.bundleChannelTypesMap.entrySet();

        if (channelTypesList != null) {
            for (Entry<Bundle, List<ChannelType>> channelTypes : channelTypesList) {
                for (ChannelType channelType : channelTypes.getValue()) {
                    if (channelType.getUID().equals(channelTypeUID)) {
                        return createLocalizedChannelType(channelTypes.getKey(), channelType, locale);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public synchronized Collection<ChannelType> getChannelTypes(Locale locale) {
        List<ChannelType> allChannelTypes = new ArrayList<>(10);

        Collection<Entry<Bundle, List<ChannelType>>> channelTypesList = this.bundleChannelTypesMap.entrySet();

        if (channelTypesList != null) {
            for (Entry<Bundle, List<ChannelType>> channelTypes : channelTypesList) {
                for (ChannelType channelType : channelTypes.getValue()) {
                    ChannelType localizedChannelType = createLocalizedChannelType(channelTypes.getKey(), channelType,
                            locale);
                    allChannelTypes.add(localizedChannelType);
                }
            }
        }

        return allChannelTypes;
    }

    public synchronized void removeAllChannelGroupTypes(Bundle bundle) {
        if (bundle != null) {
            List<ChannelGroupType> channelGroupTypes = this.bundleChannelGroupTypesMap.get(bundle);

            if (channelGroupTypes != null) {
                this.bundleChannelGroupTypesMap.remove(bundle);
                removeCachedChannelGroupTypes(channelGroupTypes);
            }
        }
    }

    public synchronized void removeAllChannelTypes(Bundle bundle) {
        if (bundle != null) {
            List<ChannelType> channelTypes = this.bundleChannelTypesMap.get(bundle);

            if (channelTypes != null) {
                this.bundleChannelTypesMap.remove(bundle);
                removeCachedChannelTypes(channelTypes);
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

    private List<ChannelGroupType> acquireChannelGroupTypes(Bundle bundle) {
        if (bundle != null) {
            List<ChannelGroupType> channelGroupTypes = this.bundleChannelGroupTypesMap.get(bundle);

            if (channelGroupTypes == null) {
                channelGroupTypes = new ArrayList<ChannelGroupType>(10);

                this.bundleChannelGroupTypesMap.put(bundle, channelGroupTypes);
            }

            return channelGroupTypes;
        }

        return null;
    }

    private List<ChannelType> acquireChannelTypes(Bundle bundle) {
        if (bundle != null) {
            List<ChannelType> channelTypes = this.bundleChannelTypesMap.get(bundle);

            if (channelTypes == null) {
                channelTypes = new ArrayList<ChannelType>(10);

                this.bundleChannelTypesMap.put(bundle, channelTypes);
            }

            return channelTypes;
        }

        return null;
    }

    private ChannelGroupType createLocalizedChannelGroupType(Bundle bundle, ChannelGroupType channelGroupType,
            Locale locale) {

        LocalizedChannelTypeKey localizedChannelTypeKey = getLocalizedChannelTypeKey(channelGroupType.getUID(), locale);

        ChannelGroupType cachedEntry = localizedChannelGroupTypeCache.get(localizedChannelTypeKey);
        if (cachedEntry != null) {
            return cachedEntry;
        }

        if (this.thingTypeI18nUtil != null) {

            ChannelGroupTypeUID channelGroupTypeUID = channelGroupType.getUID();

            String label = this.thingTypeI18nUtil.getChannelGroupLabel(bundle, channelGroupTypeUID,
                    channelGroupType.getLabel(), locale);
            String description = this.thingTypeI18nUtil.getChannelGroupDescription(bundle, channelGroupTypeUID,
                    channelGroupType.getDescription(), locale);

            ChannelGroupType localizedChannelType = new ChannelGroupType(channelGroupTypeUID,
                    channelGroupType.isAdvanced(), label, description, channelGroupType.getChannelDefinitions());

            localizedChannelGroupTypeCache.put(localizedChannelTypeKey, localizedChannelType);
            return localizedChannelType;
        }

        return channelGroupType;
    }

    private StateDescription createLocalizedChannelState(Bundle bundle, ChannelType channelType,
            ChannelTypeUID channelTypeUID, Locale locale) {

        StateDescription state = channelType.getState();

        if (state != null) {
            String pattern = this.thingTypeI18nUtil.getChannelStatePattern(bundle, channelTypeUID, state.getPattern(),
                    locale);

            List<StateOption> localizedOptions = new ArrayList<>();
            List<StateOption> options = state.getOptions();
            for (StateOption stateOption : options) {
                String optionLabel = this.thingTypeI18nUtil.getChannelStateOption(bundle, channelTypeUID,
                        stateOption.getValue(), stateOption.getLabel(), locale);
                localizedOptions.add(new StateOption(stateOption.getValue(), optionLabel));
            }

            return new StateDescription(state.getMinimum(), state.getMaximum(), state.getStep(), pattern,
                    state.isReadOnly(), localizedOptions);
        }
        return null;
    }

    private ChannelType createLocalizedChannelType(Bundle bundle, ChannelType channelType, Locale locale) {

        LocalizedChannelTypeKey localizedChannelTypeKey = getLocalizedChannelTypeKey(channelType.getUID(), locale);

        ChannelType cachedEntry = localizedChannelTypeCache.get(localizedChannelTypeKey);
        if (cachedEntry != null) {
            return cachedEntry;
        }

        if (this.thingTypeI18nUtil != null) {

            ChannelTypeUID channelTypeUID = channelType.getUID();

            String label = this.thingTypeI18nUtil.getChannelLabel(bundle, channelTypeUID, channelType.getLabel(),
                    locale);
            String description = this.thingTypeI18nUtil.getChannelDescription(bundle, channelTypeUID,
                    channelType.getDescription(), locale);

            StateDescription state = createLocalizedChannelState(bundle, channelType, channelTypeUID, locale);

            ChannelType localizedChannelType = new ChannelType(channelTypeUID, channelType.isAdvanced(),
                    channelType.getItemType(), label, description, channelType.getCategory(), channelType.getTags(),
                    state, channelType.getConfigDescriptionURI());

            localizedChannelTypeCache.put(localizedChannelTypeKey, localizedChannelType);

            return localizedChannelType;
        }

        return channelType;
    }

    private LocalizedChannelTypeKey getLocalizedChannelTypeKey(UID uid, Locale locale) {
        String localeString = locale != null ? locale.toLanguageTag() : null;
        LocalizedChannelTypeKey localizedChannelTypeKey = new LocalizedChannelTypeKey(uid,
                locale != null ? localeString : null);
        return localizedChannelTypeKey;
    }

    private void removeCachedChannelGroupTypes(ChannelGroupType channelGroupType) {
        for (Iterator<Entry<LocalizedChannelTypeKey, ChannelGroupType>> iterator = this.localizedChannelGroupTypeCache
                .entrySet().iterator(); iterator.hasNext();) {
            Entry<LocalizedChannelTypeKey, ChannelGroupType> entry = iterator.next();
            if (entry.getKey().uid.equals(channelGroupType.getUID())) {
                iterator.remove();
            }
        }
    }

    private void removeCachedChannelGroupTypes(List<ChannelGroupType> channelGroupTypes) {
        for (ChannelGroupType channelGroupType : channelGroupTypes) {
            removeCachedChannelGroupTypes(channelGroupType);
        }
    }

    private void removeCachedChannelTypes(ChannelType channelType) {
        for (Iterator<Entry<LocalizedChannelTypeKey, ChannelType>> iterator = this.localizedChannelTypeCache.entrySet()
                .iterator(); iterator.hasNext();) {
            Entry<LocalizedChannelTypeKey, ChannelType> entry = iterator.next();
            if (entry.getKey().uid.equals(channelType.getUID())) {
                iterator.remove();
            }
        }
    }

    private void removeCachedChannelTypes(List<ChannelType> channelTypes) {
        for (ChannelType channelType : channelTypes) {
            removeCachedChannelTypes(channelType);
        }
    }

}
