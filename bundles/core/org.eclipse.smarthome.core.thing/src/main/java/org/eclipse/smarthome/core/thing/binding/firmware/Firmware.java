/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.firmware;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.firmware.FirmwareProvider;
import org.eclipse.smarthome.core.thing.firmware.FirmwareRegistry;
import org.eclipse.smarthome.core.thing.firmware.FirmwareStatusInfo;
import org.eclipse.smarthome.core.thing.firmware.FirmwareUpdateService;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * <p>
 * The {@link Firmware} is the description of a firmware to be installed on the physical device of a {@link Thing}. A
 * firmware relates always to exactly one {@link ThingType}. By its {@link FirmwareUID} it is ensured that there is only
 * one firmware in a specific version for a thing type available. Firmwares can be easily created by the
 * {@link Firmware.Builder}.
 *
 * <p>
 * Firmwares are made available to the system by {@link FirmwareProvider}s that are tracked by the
 * {@link FirmwareRegistry}. The registry can be used to get a dedicated firmware or to get all available firmwares for
 * a specific {@link ThingType}.
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
 */
public final class Firmware implements Comparable<Firmware> {

    /** The key for the requires a factory reset property. */
    public static final String PROPERTY_REQUIRES_FACTORY_RESET = "requiresFactoryReset";

    private static final Logger logger = LoggerFactory.getLogger(Firmware.class);

    private final FirmwareUID uid;
    private final String vendor;
    private final String model;
    private final String description;
    private final String version;
    private final String prerequisiteVersion;
    private final String changelog;
    private final URL onlineChangelog;
    private final transient InputStream inputStream;
    private final String md5Hash;
    private final Map<String, String> properties;

    private transient byte[] bytes;

    private final Version internalVersion;
    private final Version internalPrerequisiteVersion;

    private Firmware(Builder builder) {
        this.uid = builder.uid;
        this.version = builder.uid.getFirmwareVersion();
        this.vendor = builder.vendor;
        this.model = builder.model;
        this.description = builder.description;
        this.prerequisiteVersion = builder.prerequisiteVersion;
        this.changelog = builder.changelog;
        this.onlineChangelog = builder.onlineChangelog;
        this.inputStream = builder.inputStream;
        this.md5Hash = builder.md5Hash;
        this.properties = Collections
                .unmodifiableMap(builder.properties != null ? builder.properties : Collections.emptyMap());

        this.internalVersion = new Version(this.version);
        this.internalPrerequisiteVersion = this.prerequisiteVersion != null ? new Version(this.prerequisiteVersion)
                : null;
    }

    /**
     * Returns the UID of the firmware.
     *
     * @return the UID of the firmware (not null)
     */
    public FirmwareUID getUID() {
        return uid;
    }

    /**
     * Returns the vendor of the firmware.
     *
     * @return the vendor of the firmware (can be null)
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Returns the model of the firmware.
     *
     * @return the model of the firmware (can be null)
     */
    public String getModel() {
        return model;
    }

    /**
     * Returns the description of the firmware.
     *
     * @return the description of the firmware (can be null)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the version of the firmware.
     *
     * @return the version of the firmware (not null)
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the prerequisite version of the firmware.
     *
     * @return the prerequisite version of the firmware (can be null)
     */
    public String getPrerequisiteVersion() {
        return prerequisiteVersion;
    }

    /**
     * Returns the changelog of the firmware.
     *
     * @return the changelog of the firmware (can be null)
     */
    public String getChangelog() {
        return changelog;
    }

    /**
     * Returns the URL to the online changelog of the firmware.
     *
     * @return the URL the an online changelog of the firmware (can be null)
     */
    public URL getOnlineChangelog() {
        return onlineChangelog;
    }

    /**
     * Returns the input stream for the binary content of the firmware.
     *
     * @return the input stream for the binary content of the firmware (can be null)
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns the MD5 hash value of the firmware.
     *
     * @return the MD5 hash value of the firmware (can be null)
     */
    public String getMd5Hash() {
        return md5Hash;
    }

    /**
     * Returns the binary content of the firmware using the firmware´s input stream. If the firmware provides a MD5 hash
     * value then this operation will also validate the MD5 checksum of the firmware.
     *
     * @return the binary content of the firmware (can be null)
     *
     * @throws IllegalStateException if the MD5 hash value of the firmware is invalid
     */
    public synchronized byte[] getBytes() {
        if (inputStream == null) {
            return null;
        }

        if (bytes == null) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");

                try (DigestInputStream dis = new DigestInputStream(inputStream, md)) {
                    bytes = IOUtils.toByteArray(dis);
                } catch (IOException ioEx) {
                    logger.error("Cannot read firmware with UID {}.", uid, ioEx);
                    return null;
                }

                byte[] digest = md.digest();

                if (md5Hash != null && digest != null) {
                    StringBuilder digestString = new StringBuilder();
                    for (byte b : digest) {
                        digestString.append(String.format("%02x", b));
                    }

                    if (!md5Hash.equals(digestString.toString())) {
                        bytes = null;
                        throw new IllegalStateException(
                                String.format("Invalid MD5 checksum. Expected %s, but was %s.", md5Hash, digestString));
                    }
                }
            } catch (NoSuchAlgorithmException e) {
                logger.error("Cannot calculate MD5 checksum.", e);
                bytes = null;
                return null;
            }
        }

        return bytes;
    }

    /**
     * Returns the immutable properties of the firmware.
     *
     * @return the immutable properties of the firmware (not null)
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Returns true, if this firmware is a successor version of the given firmware version, otherwise false. If the
     * given firmware version is null, then this operation will return false.
     *
     * @param firmwareVersion the firmware version to be compared
     *
     * @return true, if this firmware is a successor version for the given firmware version, otherwise false
     */
    public boolean isSuccessorVersion(String firmwareVersion) {
        if (firmwareVersion == null) {
            return false;
        }
        return internalVersion.compare(new Version(firmwareVersion)) > 0;
    }

    /**
     * Returns true, if this firmware is a valid prerequisite version of the given firmware version, otherwise false.
     * If this firmware does not have a prerequisite version or if the given firmware version is null, then this
     * operation will return false.
     *
     * @param firmwareVersion the firmware version to be checked if this firmware is valid prerequisite version of the
     *            given firmware version
     *
     * @return true, if this firmware is valid prerequisite version of the given firmware version, otherwise false
     */
    public boolean isPrerequisiteVersion(String firmwareVersion) {
        if (internalPrerequisiteVersion == null || firmwareVersion == null) {
            return false;
        }

        return new Version(firmwareVersion).compare(internalPrerequisiteVersion) >= 0;
    }

    @Override
    public int compareTo(Firmware firmware) {
        return -internalVersion.compare(new Version(firmware.getVersion()));
    }

    private static class Version {

        private static final int NO_INT = -1;

        private final String[] parts;

        private Version(String versionString) {
            this.parts = versionString.split("-|_|\\.");
        }

        private int compare(Version theVersion) {
            int max = Math.max(parts.length, theVersion.parts.length);

            for (int i = 0; i < max; i++) {
                String partA = i < parts.length ? parts[i] : null;
                String partB = i < theVersion.parts.length ? theVersion.parts[i] : null;

                Integer intA = partA != null && isInt(partA) ? Integer.parseInt(partA) : NO_INT;
                Integer intB = partB != null && isInt(partB) ? Integer.parseInt(partB) : NO_INT;

                if (intA != NO_INT && intB != NO_INT) {
                    if (intA < intB) {
                        return -1;
                    }
                    if (intA > intB) {
                        return 1;
                    }
                } else if (partA == null || partB == null) {
                    if (partA == null) {
                        return -1;
                    }
                    if (partB == null) {
                        return 1;
                    }
                } else {
                    int result = partA.compareTo(partB);
                    if (result != 0) {
                        return result;
                    }
                }
            }

            return 0;
        }

        private boolean isInt(String s) {
            return s.matches("^-?\\d+$");
        }

    }

    /**
     * The builder to create a {@link Firmware}.
     */
    public static final class Builder {

        private final FirmwareUID uid;
        private String vendor;
        private String model;
        private String description;
        private String prerequisiteVersion;
        private String changelog;
        private URL onlineChangelog;
        private transient InputStream inputStream;
        private String md5Hash;
        private Map<String, String> properties;

        /**
         * Creates a new builder.
         *
         * @param uid the UID of the firmware to be created (must not be null)
         *
         * @throws NullPointerException if given uid is null
         */
        public Builder(FirmwareUID uid) {
            Preconditions.checkNotNull(uid, "Firmware UID must not be null.");
            this.uid = uid;
        }

        /**
         * Adds the vendor to the builder.
         *
         * @param vendor the vendor to be added to the builder
         *
         * @return the updated builder
         */
        public Builder withVendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        /**
         * Adds the model to the builder.
         *
         * @param model the model to be added to the builder
         *
         * @return the updated builder
         */
        public Builder withModel(String model) {
            this.model = model;
            return this;
        }

        /**
         * Adds the description to the builder.
         *
         * @param description the description to be added to the builder
         *
         * @return the updated builder
         */
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Adds the prerequisite version to the builder.
         *
         * @param prerequisiteVersion the prerequisite version to be added to the builder
         *
         * @return the updated builder
         */
        public Builder withPrerequisiteVersion(String prerequisiteVersion) {
            this.prerequisiteVersion = prerequisiteVersion;
            return this;
        }

        /**
         * Adds the changelog to the builder.
         *
         * @param changelog the changelog to be added to the builder
         *
         * @return the updated builder
         */
        public Builder withChangelog(String changelog) {
            this.changelog = changelog;
            return this;
        }

        /**
         * Adds the online changelog to the builder.
         *
         * @param onlineChangelog the online changelog to be added to the builder
         *
         * @return the updated builder
         */
        public Builder withOnlineChangelog(URL onlineChangelog) {
            this.onlineChangelog = onlineChangelog;
            return this;
        }

        /**
         * Adds the input stream for the binary content to the builder.
         *
         * @param inputStream the input stream for the binary content to be added to the builder
         *
         * @return the updated builder
         */
        public Builder withInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        /**
         * Adds the properties to the builder.
         *
         * @param properties the properties to be added to the builder
         *
         * @return the updated builder
         */
        public Builder withProperties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        /**
         * Adds the given md5 hash value to the builder.
         *
         * @param md5Hash the md5 hash value to be added to the builder
         *
         * @return the updated builder
         */
        public Builder withMd5Hash(String md5Hash) {
            this.md5Hash = md5Hash;
            return this;
        }

        /**
         * Builds the firmware.
         *
         * @return the firmware instance based on this builder
         */
        public Firmware build() {
            return new Firmware(this);
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((changelog == null) ? 0 : changelog.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((md5Hash == null) ? 0 : md5Hash.hashCode());
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((onlineChangelog == null) ? 0 : onlineChangelog.hashCode());
        result = prime * result + ((prerequisiteVersion == null) ? 0 : prerequisiteVersion.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        result = prime * result + ((vendor == null) ? 0 : vendor.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
        Firmware other = (Firmware) obj;
        if (changelog == null) {
            if (other.changelog != null) {
                return false;
            }
        } else if (!changelog.equals(other.changelog)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (md5Hash == null) {
            if (other.md5Hash != null) {
                return false;
            }
        } else if (!md5Hash.equals(other.md5Hash)) {
            return false;
        }
        if (model == null) {
            if (other.model != null) {
                return false;
            }
        } else if (!model.equals(other.model)) {
            return false;
        }
        if (onlineChangelog == null) {
            if (other.onlineChangelog != null) {
                return false;
            }
        } else if (!onlineChangelog.equals(other.onlineChangelog)) {
            return false;
        }
        if (prerequisiteVersion == null) {
            if (other.prerequisiteVersion != null) {
                return false;
            }
        } else if (!prerequisiteVersion.equals(other.prerequisiteVersion)) {
            return false;
        }
        if (uid == null) {
            if (other.uid != null) {
                return false;
            }
        } else if (!uid.equals(other.uid)) {
            return false;
        }
        if (vendor == null) {
            if (other.vendor != null) {
                return false;
            }
        } else if (!vendor.equals(other.vendor)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Firmware [uid=" + uid + ", vendor=" + vendor + ", model=" + model + ", description=" + description
                + ", version=" + version + ", prerequisiteVersion=" + prerequisiteVersion + ", changelog=" + changelog
                + ", onlineChangelog=" + onlineChangelog + ", md5Hash=" + md5Hash + ", properties=" + properties + "]";
    }

}
