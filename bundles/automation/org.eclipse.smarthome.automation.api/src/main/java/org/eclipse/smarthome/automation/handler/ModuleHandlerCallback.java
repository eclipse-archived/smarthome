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
package org.eclipse.smarthome.automation.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleManager;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.RuleStatusDetail;
import org.eclipse.smarthome.automation.RuleStatusInfo;

/**
 * This class is responsible to provide a mechanism for communication between {@link ModuleHandler}s and the
 * {@link RuleManager}. An instance of it is provided to every {@link ModuleHandler} to listen for changes when
 * a {@link Rule} has been added, updated, enabled, disabled or removed and to involve {@link RuleManager} to
 * enable / disable or to process the {@code run} command for a {@link Rule}.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public interface ModuleHandlerCallback {

    /**
     * Gets the <b>enabled</b> {@link RuleStatus} for a {@link Rule}. The <b>enabled</b> rule statuses are
     * {@link RuleStatus#UNINITIALIZED}, {@link RuleStatus#INITIALIZING}, {@link RuleStatus#IDLE} and
     * {@link RuleStatus#RUNNING}. The <b>disabled</b> rule status is {@link RuleStatus#UNINITIALIZED} with
     * {@link RuleStatusDetail#DISABLED}.
     *
     * @param uid the unique identifier of the {@link Rule}.
     * @return {@code true} when the {@link RuleStatus} is one of the {@link RuleStatus#UNINITIALIZED},
     *         {@link RuleStatus#INITIALIZING}, {@link RuleStatus#IDLE} and {@link RuleStatus#RUNNING},
     *         {@code false} when it is {@link RuleStatus#UNINITIALIZED} with {@link RuleStatusDetail#DISABLED}.
     */
    @Nullable
    Boolean isEnabled(String ruleUID);

    /**
     * Changes the <b>enabled</b> {@link RuleStatus} of the {@link Rule}. The <b>enabled</b> rule statuses are
     * {@link RuleStatus#UNINITIALIZED}, {@link RuleStatus#INITIALIZING}, {@link RuleStatus#IDLE} and
     * {@link RuleStatus#RUNNING}. The <b>disabled</b> rule status is {@link RuleStatus#UNINITIALIZED} with
     * {@link RuleStatusDetail#DISABLED}.
     *
     * @param uid       the unique identifier of the {@link Rule}.
     * @param isEnabled a new <b>enabled / disabled</b> status of the {@link Rule}.
     * @throws IllegalStateException when the rule status can't be persisted because of missing Storage service
     *                               or when the rule has already removed from the registry or the rule is not in an
     *                               appropriate state for the requested operation.
     */
    void setEnabled(String uid, boolean isEnabled);

    /**
     * Gets the current {@link RuleStatusInfo} object associated with the looking {@link Rule} instance. The status info
     * consists of the {@link RuleStatus} itself, the {@link RuleStatusDetail} and a status description.
     *
     * @param ruleUID the unique identifier of the looking {@link Rule} instance.
     * @return the current {@link RuleStatusInfo} object associated with the looking {@link Rule} or {@code null} when a
     *         rule with specified UID does not exists.
     */
    @Nullable
    RuleStatusInfo getStatusInfo(String ruleUID);

    /**
     * Utility method which gets {@link RuleStatus} of the specified {@link Rule}.
     *
     * @param ruleUID UID of the {@link Rule}
     * @return {@link RuleStatus} object containing status of the looking {@link Rule} or null when a rule with
     *         specified UID does not exists.
     */
    @Nullable
    RuleStatus getStatus(String ruleUID);

    /**
     * Skips the triggers and the conditions and directly executes the actions of the {@link Rule}. This should always
     * be possible unless an action has a mandatory input that is linked to a trigger. In that case the action is
     * skipped and the RuleEngine continues execution of rest actions.
     *
     * @param ruleUID the unique identifier of the rule whose actions have to be executed.
     * @throws IllegalStateException when the rule is not in the appropriate state.
     */
    void runNow(String uid);

    /**
     * Skips the triggers and enable or disable evaluation of the conditions defined in the target rule. If the
     * conditions are satisfied executes the actions of the rule. Also the context can be set here but also might be
     * {@code null}.
     *
     * @param ruleUID            the unique identifier of the rule whose actions have to be executed.
     * @param considerConditions if {@code true} the conditions of the rule will be checked.
     * @param context            the context that is passed to the conditions and the actions of the rule.
     * @throws IllegalStateException when the rule is not in the appropriate state.
     */
    void runNow(String uid, boolean considerConditions, @Nullable Map<String, Object> context);

}
