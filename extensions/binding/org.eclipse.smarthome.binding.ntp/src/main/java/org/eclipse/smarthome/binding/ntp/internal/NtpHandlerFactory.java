/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.ntp.internal;

import static org.eclipse.smarthome.binding.ntp.NtpBindingConstants.*;

import org.eclipse.smarthome.binding.ntp.handler.NtpHandler;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link NtpHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Markus Rathgeb - Add locale provider support
 */
@Component(service = ThingHandlerFactory.class, immediate = true)
public class NtpHandlerFactory extends BaseThingHandlerFactory {

    private LocaleProvider localeProvider;

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_NTP)) {
            return new NtpHandler(thing, localeProvider);
        }

        return null;
    }
}
