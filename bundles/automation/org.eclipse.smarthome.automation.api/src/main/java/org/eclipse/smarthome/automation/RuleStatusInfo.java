/**
 * Copyright (c) 2015, 2017 by Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import java.util.Collections;
import java.util.Map;

/**
 * This class is used to present status of rule. The status consists out of three parts:
 * The main status, a status detail and a string description.
 *
 * @author Yordan Mihaylov - Initial contribution
 * @author Kai Kreuzer - Refactored to match ThingStatusInfo implementation
 */
public class RuleStatusInfo {

    private RuleStatus status;
    private RuleStatusDetail statusDetail;
    private String description;
    private Map<String, ?> properties;
    private static final RuleStatusInfo DISABLED = new RuleStatusInfo(RuleStatus.DISABLED, RuleStatusDetail.NONE, null,
            null);
    private static final RuleStatusInfo UNINITIALIZED = new RuleStatusInfo(RuleStatus.UNINITIALIZED,
            RuleStatusDetail.NONE, null, null);
    private static final RuleStatusInfo INITIALIZING = new RuleStatusInfo(RuleStatus.INITIALIZING,
            RuleStatusDetail.NONE, null, null);
    private static final RuleStatusInfo IDLE = new RuleStatusInfo(RuleStatus.IDLE, RuleStatusDetail.NONE, null, null);
    private static final RuleStatusInfo TRIGGERED = new RuleStatusInfo(RuleStatus.TRIGGERED, RuleStatusDetail.NONE,
            null, null);
    private static final RuleStatusInfo RUNNING = new RuleStatusInfo(RuleStatus.RUNNING, RuleStatusDetail.NONE, null,
            null);

    /**
     * Default constructor for deserialization e.g. by Gson.
     */
    protected RuleStatusInfo() {
    }

    /**
     * Constructs a status info.
     *
     * @param status the status (must not be null)
     *
     * @throws IllegalArgumentException if status is null
     */
    public static RuleStatusInfo create(RuleStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        switch (status) {
            case DISABLED:
                return RuleStatusInfo.DISABLED;
            case UNINITIALIZED:
                return RuleStatusInfo.UNINITIALIZED;
            case INITIALIZING:
                return RuleStatusInfo.INITIALIZING;
            case IDLE:
                return RuleStatusInfo.IDLE;
            case TRIGGERED:
                return RuleStatusInfo.TRIGGERED;
            case RUNNING:
                return RuleStatusInfo.RUNNING;
            default:
                throw new IllegalArgumentException("Invalid status value.");
        }

    }

    /**
     * Constructs a status info.
     *
     * @param status the status (must not be null)
     * @param statusDetail the detail of the status (must not be null)
     *
     * @throws IllegalArgumentException if status or status detail is null
     */
    public static RuleStatusInfo create(RuleStatus status, RuleStatusDetail statusDetail)
            throws IllegalArgumentException {
        if (statusDetail == null || statusDetail == statusDetail.NONE) {
            return create(status);
        } else {
            return create(status, statusDetail, null);
        }
    }

    public static RuleStatusInfo create(RuleStatus status, RuleStatusDetail statusDetail, String description) {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        if (description == null && (statusDetail == null || statusDetail == statusDetail.NONE)) {
            return create(status);
        } else {
            return new RuleStatusInfo(status, statusDetail, description, null);
        }
    }

    public static RuleStatusInfo createTriggeredStatusInfo(Map<String, ?> properties) {
        if (properties == null || properties.isEmpty()) {
            return RuleStatusInfo.TRIGGERED;
        } else {
            return new RuleStatusInfo(RuleStatus.TRIGGERED, RuleStatusDetail.NONE, null, properties);
        }
    }

    /**
     * Constructs a status info.
     *
     * @param status the status (must not be null)
     * @param statusDetail the detail of the status (must not be null)
     * @param description the description of the status
     * @param properties additional properties related to this status.
     *
     * @throws IllegalArgumentException if status or status detail is null
     */
    private RuleStatusInfo(RuleStatus status, RuleStatusDetail statusDetail, String description,
            Map<String, ?> properties) throws IllegalArgumentException {
        if (status == null) {
            throw new IllegalArgumentException("Thing status must not be null");
        }
        if (statusDetail == null) {
            throw new IllegalArgumentException("Thing status detail must not be null");
        }
        this.status = status;
        this.statusDetail = statusDetail;
        this.description = description;
        this.properties = properties;
    }

    /**
     * Gets the status itself.
     *
     * @return the status (not null)
     */
    public RuleStatus getStatus() {
        return status;
    }

    /**
     * Gets the detail of the status.
     *
     * @return the status detail (not null)
     */
    public RuleStatusDetail getStatusDetail() {
        return statusDetail;
    }

    /**
     * Gets the description of the status.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return additional properties related to the current status.
     */
    public Map<String, ?> getProperties() {
        return properties != null ? properties : Collections.emptyMap();
    }

    @Override
    public String toString() {
        boolean hasDescription = getDescription() != null && !getDescription().isEmpty();
        return getStatus() + (getStatusDetail() == RuleStatusDetail.NONE ? "" : " (" + getStatusDetail() + ")")
                + (hasDescription ? ": " + getDescription() : "");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((statusDetail == null) ? 0 : statusDetail.hashCode());
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
        RuleStatusInfo other = (RuleStatusInfo) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        if (statusDetail != other.statusDetail) {
            return false;
        }
        return properties == null ? other.getProperties() == null : properties.equals(other.getProperties());

    }
}
