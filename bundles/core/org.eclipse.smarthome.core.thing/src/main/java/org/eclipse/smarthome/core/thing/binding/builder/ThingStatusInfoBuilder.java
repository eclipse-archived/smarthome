/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

/**
 * {@link ThingStatusInfoBuilder} is responsible for creating {@link ThingStatusInfo}s.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public class ThingStatusInfoBuilder {

    private ThingStatus status;

    private ThingStatusDetail statusDetail;

    private String description;

    private ThingStatusInfoBuilder(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        this.status = status;
        this.statusDetail = statusDetail;
        this.description = description;
    }

    /**
     * Creates a status info builder for the given status and detail.
     * 
     * @param status the status
     * @param statusDetail the detail of the status
     * @return status info builder
     */
    public static ThingStatusInfoBuilder create(ThingStatus status, ThingStatusDetail statusDetail) {
        return new ThingStatusInfoBuilder(status, statusDetail, null);
    }

    /**
     * Creates a status info builder for the given status.
     * 
     * @param status the status
     * @return status info builder
     */
    public static ThingStatusInfoBuilder create(ThingStatus status) {
        return new ThingStatusInfoBuilder(status, ThingStatusDetail.NONE, null);
    }

    /**
     * Appends a description to the status to build.
     * 
     * @param description the description
     * @return status info builder
     */
    public ThingStatusInfoBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Appends a status detail to the status to build.
     * 
     * @param statusDetail the status detail
     * @return status info builder
     */
    public ThingStatusInfoBuilder withStatusDetail(ThingStatusDetail statusDetail) {
        this.statusDetail = statusDetail;
        return this;
    }

    /**
     * Builds and returns the status info.
     *
     * @return status info
     */
    public ThingStatusInfo build() {
        return new ThingStatusInfo(status, statusDetail, description);
    }
}
