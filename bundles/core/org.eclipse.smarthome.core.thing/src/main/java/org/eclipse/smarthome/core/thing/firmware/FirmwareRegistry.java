/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

/**
 * The {@link FirmwareRegistry} is registered as an OSGi service and is responsible for tracking all
 * {@link FirmwareProvider}s. For this reason it is the central instance to get access to all available firmwares. If a
 * locale is given to one of its operations then the following firmware attributes are localized:
 * <ul>
 * <li>{@link Firmware#getDescription()}</li>
 * <li>{@link Firmware#getChangelog()}</li>
 * <li>{@link Firmware#getOnlineChangelog()}</li>
 * <ul>
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class FirmwareRegistry {

    private final Logger logger = LoggerFactory.getLogger(FirmwareRegistry.class);

    private final List<FirmwareProvider> firmwareProviders = new CopyOnWriteArrayList<>();

    private LocaleProvider localeProvider;

    /**
     * Returns the firmware for the given UID using the locale provided by the {@link LocaleProvider}.
     *
     * @param firmwareUID the firmware UID (must not be null)
     *
     * @return the corresponding firmware or null if no firmware was found
     *
     * @throws NullPointerException if given firmware UID is null
     */
    public Firmware getFirmware(FirmwareUID firmwareUID) {
        return getFirmware(firmwareUID, localeProvider.getLocale());
    }

    /**
     * Returns the firmware for the given UID.
     *
     * @param firmwareUID the firmware UID (must not be null)
     * @param locale the locale to be used (if null then the locale provided by the {@link LocaleProvider} is used)
     *
     * @return the corresponding firmware or null if no firmware was found
     *
     * @throws NullPointerException if given firmware UID is null
     */
    public Firmware getFirmware(FirmwareUID firmwareUID, Locale locale) {
        Preconditions.checkNotNull(firmwareUID, "Firmware UID must not be null");

        Locale loc = locale != null ? locale : localeProvider.getLocale();

        for (FirmwareProvider firmwareProvider : firmwareProviders) {
            try {
                Firmware firmware = firmwareProvider.getFirmware(firmwareUID, loc);
                if (firmware != null) {
                    return firmware;
                }
            } catch (Exception e) {
                logger.warn(String.format(
                        "Unexpected exception occurred for firmware provider %s while getting firmware for firmware UID %s.",
                        firmwareProvider.getClass().getSimpleName(), firmwareUID), e);
            }
        }

        return null;
    }

    /**
     * Returns the latest firmware for the given thing type UID using the locale provided by the {@link LocaleProvider}.
     *
     * @param thingTypeUID the thing type UID (must not be null)
     *
     * @return the corresponding latest firmware or null if no firmware was found
     *
     * @throws NullPointerException if given thing type UID is null
     */
    public Firmware getLatestFirmware(ThingTypeUID thingTypeUID) {
        return getLatestFirmware(thingTypeUID, localeProvider.getLocale());
    }

    /**
     * Returns the latest firmware for the given thing type UID and locale.
     *
     * @param thingTypeUID the thing type UID (must not be null)
     * @param locale the locale to be used (if null then the locale provided by the {@link LocaleProvider} is used)
     *
     * @return the corresponding latest firmware or null if no firmware was found
     *
     * @throws NullPointerException if given thing type UID is null
     */
    public Firmware getLatestFirmware(ThingTypeUID thingTypeUID, Locale locale) {
        Locale loc = locale != null ? locale : localeProvider.getLocale();
        return Iterables.getFirst(getFirmwares((thingTypeUID), loc), null);
    }

    /**
     * Returns the collection of available firmwares for the given thing type UID using the locale provided by the
     * {@link LocaleProvider}. The collection is sorted in descending order, i.e. the latest firmware will be the first
     * element in the collection.
     *
     * @param thingTypeUID the thing type UID for which the firmwares are to be provided (not null)
     *
     * @return the collection of available firmwares for the given thing type UID (not null)
     *
     * @throws NullPointerException if given thing type UID is null
     */
    public Collection<Firmware> getFirmwares(ThingTypeUID thingTypeUID) {
        return getFirmwares(thingTypeUID, localeProvider.getLocale());
    }

    /**
     * Returns the collection of available firmwares for the given thing type UID and locale. The collection is
     * sorted in descending order, i.e. the latest firmware will be the first element in the collection.
     *
     * @param thingTypeUID the thing type UID for which the firmwares are to be provided (not null)
     * @param locale the locale to be used (if null then the locale provided by the {@link LocaleProvider} is used)
     *
     * @return the collection of available firmwares for the given thing type UID (not null)
     *
     * @throws NullPointerException if given thing type UID is null
     */
    public Collection<Firmware> getFirmwares(ThingTypeUID thingTypeUID, Locale locale) {
        Preconditions.checkNotNull(thingTypeUID, "Thing type UID must not be null");

        Locale loc = locale != null ? locale : localeProvider.getLocale();

        Set<Firmware> firmwares = new TreeSet<>();
        for (FirmwareProvider firmwareProvider : firmwareProviders) {
            try {
                Collection<Firmware> result = firmwareProvider.getFirmwares(thingTypeUID, loc);
                if (result != null) {
                    firmwares.addAll(result);
                }
            } catch (Exception e) {
                logger.warn(String.format(
                        "Unexpected exception occurred for firmware provider %s while getting firmwares for thing type UID %s.",
                        firmwareProvider.getClass().getSimpleName(), thingTypeUID), e);
            }
        }

        return Collections.unmodifiableCollection(firmwares);
    }

    protected void addFirmwareProvider(FirmwareProvider firmwareProvider) {
        firmwareProviders.add(firmwareProvider);
    }

    protected void removeFirmwareProvider(FirmwareProvider firmwareProvider) {
        firmwareProviders.remove(firmwareProvider);
    }

    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }
}
