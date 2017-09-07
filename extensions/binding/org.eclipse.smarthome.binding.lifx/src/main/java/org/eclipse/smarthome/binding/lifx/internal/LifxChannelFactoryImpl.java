/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.*;

import java.util.Locale;

import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link LifxChannelFactoryImpl} creates dynamic LIFX channels.
 *
 * @author Wouter Born - Add i18n support
 */
@Component(service = LifxChannelFactory.class, immediate = true)
public class LifxChannelFactoryImpl implements LifxChannelFactory {

    private static final String COLOR_ZONE_LABEL_KEY = "channel-type.lifx.colorzone.label";
    private static final String COLOR_ZONE_DESCRIPTION_KEY = "channel-type.lifx.colorzone.description";

    private static final String TEMPERATURE_ZONE_LABEL_KEY = "channel-type.lifx.temperaturezone.label";
    private static final String TEMPERATURE_ZONE_DESCRIPTION_KEY = "channel-type.lifx.temperaturezone.description";

    private Bundle bundle;
    private TranslationProvider i18nProvider;
    private LocaleProvider localeProvider;

    @Override
    public Channel createColorZoneChannel(ThingUID thingUID, int index) {
        String label = getText(COLOR_ZONE_LABEL_KEY, index);
        String description = getText(COLOR_ZONE_DESCRIPTION_KEY, index);
        return ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_COLOR_ZONE + index), "ColorItem")
                .withType(CHANNEL_TYPE_COLOR_ZONE).withLabel(label).withDescription(description).build();
    }

    @Override
    public Channel createTemperatureZoneChannel(ThingUID thingUID, int index) {
        String label = getText(TEMPERATURE_ZONE_LABEL_KEY, index);
        String description = getText(TEMPERATURE_ZONE_DESCRIPTION_KEY, index);
        return ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_TEMPERATURE_ZONE + index), "DimmerItem")
                .withType(CHANNEL_TYPE_TEMPERATURE_ZONE).withLabel(label).withDescription(description).build();
    }

    private String getDefaultText(String key) {
        return i18nProvider.getText(bundle, key, key, Locale.ENGLISH);
    }

    private String getText(String key, Object... arguments) {
        Locale locale = localeProvider != null ? localeProvider.getLocale() : Locale.ENGLISH;
        return i18nProvider != null ? i18nProvider.getText(bundle, key, getDefaultText(key), locale, arguments) : key;
    }

    @Activate
    protected void activate(ComponentContext componentContext) {
        this.bundle = componentContext.getBundleContext().getBundle();
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        this.bundle = null;
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }

    @Reference
    protected void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

}
