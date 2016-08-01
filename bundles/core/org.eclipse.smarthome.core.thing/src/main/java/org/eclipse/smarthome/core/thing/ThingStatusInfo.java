/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import org.apache.commons.lang.StringUtils;

/**
 * A {@link ThingStatusInfo} represents status information of a thing which consists of
 * <ul>
 * <li>the status itself </il>
 * <li>detail of the status</il>
 * <li>and a description of the status</il>
 * </ul>
 *
 * @author Stefan Bußweiler - Initial contribution
 * @author Dennis Nobel - Added null checks
 */
public class ThingStatusInfo {

    private ThingStatus status;

    private ThingStatusDetail statusDetail;

    private String description;

    /**
     * Default constructor for deserialization e.g. by Gson.
     */
    protected ThingStatusInfo() {
    }

    /**
     * Constructs a status info.
     *
     * @param status the status (must not be null)
     * @param statusDetail the detail of the status (must not be null)
     * @param description the description of the status
     *
     * @throws IllegalArgumentException if thing status or thing status detail is null
     */
    public ThingStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description)
            throws IllegalArgumentException {
        if (status == null) {
            throw new IllegalArgumentException("Thing status must not be null");
        }
        if (statusDetail == null) {
            throw new IllegalArgumentException("Thing status detail must not be null");
        }
        this.status = status;
        this.statusDetail = statusDetail;
        this.description = description;
    }

    /**
     * Gets the status itself.
     *
     * @return the status (not null)
     */
    public ThingStatus getStatus() {
        return status;
    }

    /**
     * Gets the detail of the status.
     *
     * @return the status detail (not null)
     */
    public ThingStatusDetail getStatusDetail() {
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

    @Override
    public String toString() {
        return getStatus() + (getStatusDetail() == ThingStatusDetail.NONE ? "" : " (" + getStatusDetail() + ")")
                + (StringUtils.isBlank(getDescription()) ? "" : ": " + getDescription());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((statusDetail == null) ? 0 : statusDetail.hashCode());
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
        ThingStatusInfo other = (ThingStatusInfo) obj;
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
        return true;
    }

}
