/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.thing.binding.firmware;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.firmware.FirmwareProvider;
import org.eclipse.smarthome.core.thing.firmware.FirmwareRegistry;
import org.eclipse.smarthome.core.thing.firmware.FirmwareStatusInfo;
import org.eclipse.smarthome.core.thing.firmware.FirmwareUpdateService;

/**
 * <p>
 * The {@link Firmware} is the description of a firmware to be installed on the physical device of a {@link Thing}. A
 * firmware relates always to exactly one {@link Thing}. By using the model restrictions (see
 * {@link #isModelRestricted()}) and the firmware version it is ensured that there is only one firmware in a specific
 * version for a thing available. Firmwares can be easily created by the {@link FirmwareBuilder}.
 *
 * <p>
 * Firmwares are made available to the system by {@link FirmwareProvider}s that are tracked by the
 * {@link FirmwareRegistry}. The registry can be used to get a dedicated firmware or to get all available firmwares for
 * a specific {@link Thing}.
 *
 * <p>
 * The {@link FirmwareUpdateService} is responsible to provide the current {@link FirmwareStatusInfo} of a thing.
 * Furthermore this service is the central instance to start a firmware update process. In order that the firmware of a
 * thing can be updated the hander of the thing has to implement the {@link FirmwareUpdateHandler} interface.
 *
 * <p>
 * The {@link Firmware} implements the {@link Comparable} interface in order to be able to sort firmwares based on their
 * versions. Firmwares are sorted in a descending sequence, i.e. that the latest firmware will be the first
 * element in a sorted result set. The implementation of {@link Firmware#compareTo(Firmware)} splits the firmware
 * version by the delimiters ".", "-" and "_" and compares the different parts of the firmware version. As a result the
 * firmware version <i>2-0-1</i> is newer then firmware version <i>2.0.0</i> which again is newer than firmware version
 * <i>1-9_9.9_abc</i>. Consequently <i>2.0-0</i>, <i>2-0_0</i> and <i>2_0.0</i> represent the same firmware version.
 * Furthermore firmware version <i>xyz_1</i> is newer than firmware version <i>abc.2</i> which again is newer than
 * firmware version <i>2-0-1</i>.
 *
 * <p>
 * A {@link Firmware} consists of various meta information like a version, a vendor or a description. Additionally
 * {@link FirmwareProvider}s can specify further meta information in form of properties (e.g. a factory reset of the
 * device is required afterwards) so that {@link FirmwareUpdateHandler}s can handle this information accordingly.
 *
 * @author Thomas Höfer - Initial contribution
 * @author Dimitar Ivanov - Firmware is extracted as interface with default implementation
 */
@NonNullByDefault
public interface Firmware extends Comparable<Firmware> {

    /** The key for the requires a factory reset property. */
    public static final String PROPERTY_REQUIRES_FACTORY_RESET = "requiresFactoryReset";

    /**
     * Returns the thing type UID, that this firmware is associated with.
     *
     * @return the thing type UID (not null)
     */
    public ThingTypeUID getThingTypeUID();

    /**
     * Returns the vendor of the firmware.
     *
     * @return the vendor of the firmware (can be null)
     */
    @Nullable
    public String getVendor();

    /**
     * Returns the model of the firmware.
     *
     * @return the model of the firmware (can be null)
     */
    @Nullable
    public String getModel();

    /**
     * Returns whether this firmware is restricted to things with the model provided by the {@link #getModel()} method.
     *
     * @return whether the firmware is restricted to a particular model
     */
    public boolean isModelRestricted();

    /**
     * Returns the description of the firmware.
     *
     * @return the description of the firmware (can be null)
     */
    @Nullable
    public String getDescription();

    /**
     * Returns the version of the firmware.
     *
     * @return the version of the firmware (not null)
     */
    public String getVersion();

    /**
     * Returns the prerequisite version of the firmware.
     *
     * @return the prerequisite version of the firmware (can be null)
     */
    @Nullable
    public String getPrerequisiteVersion();

    /**
     * Returns the changelog of the firmware.
     *
     * @return the changelog of the firmware (can be null)
     */
    @Nullable
    public String getChangelog();

    /**
     * Returns the URL to the online changelog of the firmware.
     *
     * @return the URL the an online changelog of the firmware (can be null)
     */
    @Nullable
    public URL getOnlineChangelog();

    /**
     * Returns the input stream for the binary content of the firmware.
     *
     * @return the input stream for the binary content of the firmware (can be null)
     */
    @Nullable
    public InputStream getInputStream();

    /**
     * Returns the MD5 hash value of the firmware.
     *
     * @return the MD5 hash value of the firmware (can be null)
     */
    @Nullable
    public String getMd5Hash();

    /**
     * Returns the binary content of the firmware using the firmware´s input stream. If the firmware provides a MD5 hash
     * value then this operation will also validate the MD5 checksum of the firmware.
     *
     * @return the binary content of the firmware (can be null)
     * @throws IllegalStateException if the MD5 hash value of the firmware is invalid
     */
    public byte @Nullable [] getBytes();

    /**
     * Returns the immutable properties of the firmware.
     *
     * @return the immutable properties of the firmware (not null)
     */
    public Map<String, String> getProperties();

    /**
     * Returns true, if this firmware is a successor version of the given firmware version, otherwise false. If the
     * given firmware version is null, then this operation will return false.
     *
     * @param firmwareVersion the firmware version to be compared
     * @return true, if this firmware is a successor version for the given firmware version, otherwise false
     */
    public boolean isSuccessorVersion(@Nullable String firmwareVersion);

    /**
     * Returns true, if this firmware is a valid prerequisite version of the given firmware version, otherwise false.
     * If this firmware does not have a prerequisite version or if the given firmware version is null, then this
     * operation will return false.
     *
     * @param firmwareVersion the firmware version to be checked if this firmware is valid prerequisite version of the
     *            given firmware version
     * @return true, if this firmware is valid prerequisite version of the given firmware version, otherwise false
     */
    public boolean isPrerequisiteVersion(@Nullable String firmwareVersion);

    /**
     * Checks whether this firmware is suitable for the given thing by checking the thing type and the model
     * restrictions (if any).
     *
     * @param thing to be checked for suitability with the current firmware
     * @return <code>true</code> if the current firmware is suitable for the given thing and <code>false</code>
     *         otherwise.
     */
    public boolean isSuitableFor(Thing thing);
}
