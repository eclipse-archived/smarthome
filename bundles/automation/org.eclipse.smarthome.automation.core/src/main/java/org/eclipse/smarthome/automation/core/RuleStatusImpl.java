/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import org.eclipse.smarthome.automation.RuleStatus;

/**
 * This class is used to present status of rule. The status has following properties:
 * The rule can be enable/disable - this property can be set by the user when the rule
 * must stop work for temporary period of time. The rule can me running when it is
 * executing triggered data. The rule can be not initialized when some of module handlers
 * are not available.
 *
 * @author Yordan Mihaylov
 */
public class RuleStatusImpl implements RuleStatus {

    private StatusDetail statusDetail;
    private Status status;
    private String description;

    public RuleStatusImpl(Status status) {
        this(status, null, null);
    }

    public RuleStatusImpl(Status status, StatusDetail statusDetail, String description) {
        if (status == null) {
            throw new IllegalArgumentException("The status must be specified!");
        }

        this.status = status;
        this.statusDetail = statusDetail;
        this.description = description;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public StatusDetail getStatusDetail() {
        return statusDetail == null ? StatusDetail.NONE : statusDetail;
    }

    public void setStatusDetail(StatusDetail error) {
        this.statusDetail = error;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getStatusName(status));
        if (statusDetail != null) {
            sb.append(" (").append(getStatusDetailName(statusDetail)).append(")");
        }
        if (description != null) {
            sb.append(" : ").append(getDescription());
        }
        return sb.toString();
    }

    private String getStatusName(Status status) {
        switch (status) {
            case DISABLED:
                return "NOT ENABLED";
            case NOT_INITIALIZED:
                return "NOT INITIALIZED";
            case IDLE:
                return "IDLE";
            case RUNNING:
                return "RUNNING";

            default:
                return "UNKNOWN STATUS VALUE";
        }
    }

    private String getStatusDetailName(StatusDetail statusDetail) {
        switch (statusDetail) {
            case NONE:
                return "none";
            case HANDLER_MISSING_ERROR:
                return "Missing handler";
            case HANDLER_INITIALIZING_ERROR:
                return "Handler initialization error";
            case CONFIGURATION_ERROR:
                return "Configuration error";

            default:
                return "Uknown status detail";
        }
    }

}
