/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;

/**
 * {@link ThingStatusInfoBuilder} is responsible for creating {@link ThingStatusInfo}s.
 *
 * @author Stefan Bußweiler - Initial contribution
 * @author Dennis Nobel - Added null checks
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
     * @param status the status (must not be null)
     * @param statusDetail the detail of the status (must not be null)
     * @return status info builder
     *
     * @throws IllegalArgumentException if thing status or thing status detail is null
     */
    public static @NonNull ThingStatusInfoBuilder create(@NonNull ThingStatus status,
            @NonNull ThingStatusDetail statusDetail) throws IllegalArgumentException {
        return new ThingStatusInfoBuilder(status, statusDetail, null);
    }

    /**
     * Creates a status info builder for the given status.
     *
     * @param status the status (must not be null)
     * @return status info builder
     *
     * @throws IllegalArgumentException if thing status is null
     */
    public static @NonNull ThingStatusInfoBuilder create(@NonNull ThingStatus status) throws IllegalArgumentException {
        return create(status, ThingStatusDetail.NONE);
    }

    /**
     * Appends a description to the status to build.
     *
     * @param description the description
     * @return status info builder
     */
    public @NonNull ThingStatusInfoBuilder withDescription(String description) throws IllegalArgumentException {
        this.description = description;
        return this;
    }

    /**
     * Appends a status detail to the status to build.
     *
     * @param statusDetail the status detail (must not be null)
     * @return status info builder
     *
     * @throws IllegalArgumentException if thing status detail is null
     */
    public @NonNull ThingStatusInfoBuilder withStatusDetail(@NonNull ThingStatusDetail statusDetail)
            throws IllegalArgumentException {
        this.statusDetail = statusDetail;
        return this;
    }

    /**
     * Builds and returns the status info.
     *
     * @return status info
     */
    public @NonNull ThingStatusInfo build() {
        return new ThingStatusInfo(status, statusDetail, description);
    }
}
