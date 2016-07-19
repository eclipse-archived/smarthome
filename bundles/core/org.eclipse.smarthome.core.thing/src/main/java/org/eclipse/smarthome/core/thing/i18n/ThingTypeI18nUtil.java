/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.i18n;

import java.util.Locale;

import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.framework.Bundle;

/**
 * {@link ThingTypeI18nUtil} uses the {@link I18nProvider} to resolve
 * the localized texts. It automatically infers the key if the default text is
 * not a constant.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ThingTypeI18nUtil {

    private I18nProvider i18nProvider;

    public ThingTypeI18nUtil(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public String getChannelDescription(Bundle bundle, ChannelTypeUID channelTypeUID, String defaultDescription,
            Locale locale) {
        String key = I18nUtil.isConstant(defaultDescription) ? I18nUtil.stripConstant(defaultDescription)
                : inferChannelKey(channelTypeUID, "description");
        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    public String getChannelGroupDescription(Bundle bundle, ChannelGroupTypeUID channelGroupTypeUID,
            String defaultDescription, Locale locale) {
        String key = I18nUtil.isConstant(defaultDescription) ? I18nUtil.stripConstant(defaultDescription)
                : inferChannelKey(channelGroupTypeUID, "description");
        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    public String getChannelGroupLabel(Bundle bundle, ChannelGroupTypeUID channelGroupTypeUID, String defaultLabel,
            Locale locale) {
        String key = I18nUtil.isConstant(defaultLabel) ? I18nUtil.stripConstant(defaultLabel) : inferChannelKey(
                channelGroupTypeUID, "label");
        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    public String getChannelLabel(Bundle bundle, ChannelTypeUID channelTypeUID, String defaultLabel, Locale locale) {
        String key = I18nUtil.isConstant(defaultLabel) ? I18nUtil.stripConstant(defaultLabel) : inferChannelKey(
                channelTypeUID, "label");
        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    public String getChannelStateOption(Bundle bundle, ChannelTypeUID channelTypeUID, String optionValue,
            String defaultOptionLabel, Locale locale) {
        String key = I18nUtil.isConstant(defaultOptionLabel) ? I18nUtil.stripConstant(defaultOptionLabel)
                : inferChannelKey(channelTypeUID, "state.option." + optionValue);
        return i18nProvider.getText(bundle, key, defaultOptionLabel, locale);
    }

    public String getChannelStatePattern(Bundle bundle, ChannelTypeUID channelTypeUID, String defaultPattern,
            Locale locale) {
        String key = I18nUtil.isConstant(defaultPattern) ? I18nUtil.stripConstant(defaultPattern) : inferChannelKey(
                channelTypeUID, "state.pattern");
        return i18nProvider.getText(bundle, key, defaultPattern, locale);
    }

    public String getDescription(Bundle bundle, ThingTypeUID thingTypeUID, String defaultDescription, Locale locale) {
        String key = I18nUtil.isConstant(defaultDescription) ? I18nUtil.stripConstant(defaultDescription)
                : inferThingTypeKey(thingTypeUID, "description");
        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    public String getLabel(Bundle bundle, ThingTypeUID thingTypeUID, String defaultLabel, Locale locale) {
        String key = I18nUtil.isConstant(defaultLabel) ? I18nUtil.stripConstant(defaultLabel) : inferThingTypeKey(
                thingTypeUID, "label");
        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    private String inferChannelKey(ChannelGroupTypeUID channelGroupTypeUID, String lastSegment) {
        return "channel-group-type." + channelGroupTypeUID.getBindingId() + "." + channelGroupTypeUID.getId() + "."
                + lastSegment;
    }

    private String inferChannelKey(ChannelTypeUID channelTypeUID, String lastSegment) {
        return "channel-type." + channelTypeUID.getBindingId() + "." + channelTypeUID.getId() + "." + lastSegment;
    }

    private String inferThingTypeKey(ThingTypeUID thingTypeUID, String lastSegment) {
        return "thing-type." + thingTypeUID.getBindingId() + "." + thingTypeUID.getId() + "." + lastSegment;
    }

}
