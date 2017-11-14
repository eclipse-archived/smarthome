/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware;

import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID;

/**
 * The {@link FirmwareProvider} is registered as an OSGi service and is responsible for providing firmwares. If a locale
 * is given to one of its operations then the following firmware attributes are to be localized:
 * <ul>
 * <li>{@link Firmware#getDescription()}</li>
 * <li>{@link Firmware#getChangelog()}</li>
 * <li>{@link Firmware#getOnlineChangelog()}</li>
 * <ul>
 *
 * @author Thomas Höfer - Initial contribution
 */
public interface FirmwareProvider {

    /**
     * Returns the firmware for the given UID.
     *
     * @param firmwareUID the firmware UID (not null)
     *
     * @return the corresponding firmware or null if no firmware was found
     */
    Firmware getFirmware(FirmwareUID firmwareUID);

    /**
     * Returns the firmware for the given UID and the given locale.
     *
     * @param firmwareUID the firmware UID (not null)
     * @param locale the locale to be used (if null then the default locale is to be used)
     *
     * @return the corresponding firmware for the given locale or null if no firmware was found
     */
    Firmware getFirmware(FirmwareUID firmwareUID, Locale locale);

    /**
     * Returns the set of available firmwares for the given thing type UID.
     *
     * @param thingTypeUID the thing type UID for which the firmwares are to be provided (not null)
     *
     * @return the set of available firmwares for the given thing type UID (can be null)
     */
    Set<Firmware> getFirmwares(ThingTypeUID thingTypeUID);

    /**
     * Returns the set of available firmwares for the given thing type UID and the given locale.
     *
     * @param thingTypeUID the thing type UID for which the firmwares are to be provided (not null)
     * @param locale the locale to be used (if null then the default locale is to be used)
     *
     * @return the set of available firmwares for the given thing type UID (can be null)
     */
    Set<Firmware> getFirmwares(ThingTypeUID thingTypeUID, Locale locale);

}
