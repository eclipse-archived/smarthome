/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding.xml.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.AbstractXmlConfigDescriptionProvider;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentBundleTracker;
import org.eclipse.smarthome.config.xml.osgi.XmlDocumentProviderFactory;
import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
import org.eclipse.smarthome.core.binding.BindingInfo;
import org.eclipse.smarthome.core.binding.BindingInfoProvider;
import org.eclipse.smarthome.core.i18n.BindingI18nUtil;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link XmlBindingInfoProvider} is a concrete implementation of the {@link BindingInfoProvider} service interface.
 * <p>
 * This implementation manages any {@link BindingInfo} objects associated to specific modules. If a specific module
 * disappears, any registered {@link BindingInfo} objects associated with that module are released.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Michael Grammling - Refactoring: Provider/Registry pattern is used, added locale support
 */
@Component
public class XmlBindingInfoProvider implements BindingInfoProvider {

    private static final String XML_DIRECTORY = "/ESH-INF/binding/";
    private Map<Bundle, List<BindingInfo>> bundleBindingInfoMap;
    private BindingI18nUtil bindingI18nUtil;
    private AbstractXmlConfigDescriptionProvider configDescriptionProvider;
    private XmlDocumentBundleTracker<BindingInfoXmlResult> bindingInfoTracker;

    public XmlBindingInfoProvider() {
        this.bundleBindingInfoMap = new HashMap<>(10);
    }

    @Activate
    public void activate(ComponentContext componentContext) {
        XmlDocumentReader<BindingInfoXmlResult> bindingInfoReader = new BindingInfoReader();

        XmlDocumentProviderFactory<BindingInfoXmlResult> bindingInfoProviderFactory = new BindingInfoXmlProviderFactory(
                this, configDescriptionProvider);

        bindingInfoTracker = new XmlDocumentBundleTracker<>(componentContext.getBundleContext(), XML_DIRECTORY,
                bindingInfoReader, bindingInfoProviderFactory);

        bindingInfoTracker.open();

    }

    @Deactivate
    public void deactivate(ComponentContext componentContext) {
        bindingInfoTracker.close();
        bindingInfoTracker = null;
    }

    private List<BindingInfo> acquireBindingInfos(Bundle bundle) {
        if (bundle != null) {
            List<BindingInfo> bindingInfos = this.bundleBindingInfoMap.get(bundle);

            if (bindingInfos == null) {
                bindingInfos = new ArrayList<BindingInfo>(10);

                this.bundleBindingInfoMap.put(bundle, bindingInfos);
            }

            return bindingInfos;
        }

        return null;
    }

    /**
     * Adds a {@link BindingInfo} object to the internal list associated with the specified module.
     * <p>
     * This method returns silently, if any of the parameters is {@code null}.
     *
     * @param bundle the module to which the binding information to be added
     * @param bindingInfo the binding information to be added
     */
    public synchronized void addBindingInfo(Bundle bundle, BindingInfo bindingInfo) {
        if (bindingInfo != null) {
            List<BindingInfo> bindingInfos = acquireBindingInfos(bundle);

            if (bindingInfos != null) {
                bindingInfos.add(bindingInfo);
            }
        }
    }

    /**
     * Removes all {@link BindingInfo} objects from the internal list
     * associated with the specified module.
     * <p>
     * This method returns silently if the module is {@code null}.
     *
     * @param bundle the module for which all associated binding informations to be removed
     */
    public synchronized void removeAllBindingInfos(Bundle bundle) {
        if (bundle != null) {
            List<BindingInfo> bindingInfos = this.bundleBindingInfoMap.get(bundle);

            if (bindingInfos != null) {
                this.bundleBindingInfoMap.remove(bundle);
            }
        }
    }

    @Override
    public synchronized BindingInfo getBindingInfo(String id, Locale locale) {
        Collection<Entry<Bundle, List<BindingInfo>>> bindingInfoList = this.bundleBindingInfoMap.entrySet();

        if (bindingInfoList != null) {
            for (Entry<Bundle, List<BindingInfo>> bindingInfos : bindingInfoList) {
                for (BindingInfo bindingInfo : bindingInfos.getValue()) {
                    if (bindingInfo.getId().equals(id)) {
                        return createLocalizedBindingInfo(bindingInfos.getKey(), bindingInfo, locale);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public synchronized Set<BindingInfo> getBindingInfos(Locale locale) {
        Set<BindingInfo> allBindingInfos = new LinkedHashSet<>(10);

        Collection<Entry<Bundle, List<BindingInfo>>> bindingInfoSet = this.bundleBindingInfoMap.entrySet();

        if (bindingInfoSet != null) {
            for (Entry<Bundle, List<BindingInfo>> bindingInfos : bindingInfoSet) {
                for (BindingInfo bindingInfo : bindingInfos.getValue()) {
                    BindingInfo localizedBindingInfo = createLocalizedBindingInfo(bindingInfos.getKey(), bindingInfo,
                            locale);

                    allBindingInfos.add(localizedBindingInfo);
                }
            }
        }

        return allBindingInfos;
    }

    @Reference
    public void setTranslationProvider(TranslationProvider i18nProvider) {
        this.bindingI18nUtil = new BindingI18nUtil(i18nProvider);
    }

    public void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.bindingI18nUtil = null;
    }

    @Reference(target = "(esh.scope=core.xml.binding)")
    public void setConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = (AbstractXmlConfigDescriptionProvider) configDescriptionProvider;
    }

    public void unsetConfigDescriptionProvider(ConfigDescriptionProvider configDescriptionProvider) {
        this.configDescriptionProvider = null;
    }

    private BindingInfo createLocalizedBindingInfo(Bundle bundle, BindingInfo bindingInfo, Locale locale) {

        if (this.bindingI18nUtil != null) {
            String name = this.bindingI18nUtil.getName(bundle, bindingInfo.getId(), bindingInfo.getName(), locale);
            String description = this.bindingI18nUtil.getDescription(bundle, bindingInfo.getId(),
                    bindingInfo.getDescription(), locale);

            return new BindingInfo(bindingInfo.getId(), name, description, bindingInfo.getAuthor(),
                    bindingInfo.getServiceId(), bindingInfo.getConfigDescriptionURI());
        } else {
            return bindingInfo;
        }
    }

}
