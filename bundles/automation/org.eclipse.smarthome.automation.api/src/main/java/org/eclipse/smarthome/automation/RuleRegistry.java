/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import java.util.Collection;
import java.util.Map;

import org.eclipse.smarthome.core.common.registry.Registry;

/**
 * The {@link RuleRegistry} provides basic functionality for managing {@link Rule}s.
 * It can be used to
 * <ul>
 * <li>Add Rules with the {@link Registry#add(Object)} method.</li>
 * <li>Get the existing rules with the {@link #getByTag(String)}, {@link #getByTags(String[])} methods.</li>
 * <li>Update the existing rules with the {@link Registry#update(Object)} method.</li>
 * <li>Remove Rules with the {@link Registry#remove(Object)} method.</li>
 * <li>Manage the state (<b>enabled</b> or <b>disabled</b>) of the Rules:
 * <ul>
 * <li>A newly added Rule is always <b>enabled</b>.</li>
 * <li>To check a Rule's state, use the {@link #isEnabled(String)} method.</li>
 * <li>To change a Rule's state, use the {@link #setEnabled(String, boolean)} method.</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * The {@link RuleRegistry} manages the status of the Rules:
 * <ul>
 * <li>To check a Rule's status info, use the {@link #getStatusInfo(String)} method.</li>
 * <li>The status of a Rule enabled with {@link #setEnabled(String, boolean)}, is first set to
 * {@link RuleStatus#UNINITIALIZED}.</li>
 * <li>After a Rule is enabled, a verification procedure is initiated. If the verification of the modules IDs,
 * connections between modules and configuration values of the modules is successful, and the module handlers are
 * correctly set, the status is set to {@link RuleStatus#IDLE}.</li>
 * <li>If some of the module handlers disappear, the Rule will become {@link RuleStatus#UNINITIALIZED} again.</li>
 * <li>If one of the Rule's Triggers is triggered, the Rule becomes {@link RuleStatus#RUNNING}.
 * When the execution is complete, it will become {@link RuleStatus#IDLE} again.</li>
 * <li>If a Rule is disabled with {@link #setEnabled(String, boolean)}, it's status is set to
 * {@link RuleStatus#DISABLED}.</li>
 * </ul>
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface RuleRegistry extends Registry<Rule, String> {

    /**
     * This method is used to get collection of {@link Rule}s which shares same tag.
     *
     * @param tag tag set to the rules
     * @return collection of {@link Rule}s having specified tag.
     */
    public Collection<Rule> getByTag(String tag);

    /**
     * This method is used to get collection of {@link Rule}s which has specified tags.
     *
     * @param tags set of {@link Rule}'s tags
     * @return collection of {@link Rule}s having specified tags.
     */
    public Collection<Rule> getByTags(String... tags);

    /**
     * This method is used for changing <b>enabled</b> state of the {@link Rule}.
     * The <b>enabled</b> rule statuses are {@link RuleStatus#UNINITIALIZED}, {@link RuleStatus#IDLE} and
     * {@link RuleStatus#RUNNING}.
     * The <b>disabled</b> rule status is {@link RuleStatus#DISABLED}.
     *
     * @param uid the unique identifier of the {@link Rule}.
     * @param isEnabled a new <b>enabled / disabled</b> state of the {@link Rule}.
     */
    public void setEnabled(String uid, boolean isEnabled);

    /**
     * This method gets {@link RuleStatusInfo} of the specified {@link Rule}.
     *
     * @param ruleUID UID of the {@link Rule}
     * @return {@link RuleStatusInfo} object containing status of the looking {@link Rule} or null when the rule with
     *         specified uid does not exists.
     */
    public RuleStatusInfo getStatusInfo(String ruleUID);

    /**
     * Utility method which gets {@link RuleStatus} of the specified {@link Rule}.
     *
     * @param ruleUID UID of the {@link Rule}
     * @return {@link RuleStatus} object containing status of the looking {@link Rule} or null when the rule with
     *         specified uid does not exists..
     */
    public RuleStatus getStatus(String ruleUID);

    /**
     * This method gets <b>enabled</b> {@link RuleStatus} for a {@link Rule}.
     * The <b>enabled</b> rule statuses are {@link RuleStatus#UNINITIALIZED}, {@link RuleStatus#IDLE} and
     * {@link RuleStatus#RUNNING}.
     * The <b>disabled</b> rule status is {@link RuleStatus#DISABLED}.
     *
     * @param ruleUID UID of the {@link Rule}
     * @return <code>true</code> when the {@link RuleStatus} is one of the {@link RuleStatus#UNINITIALIZED},
     *         {@link RuleStatus#IDLE} and {@link RuleStatus#RUNNING}, <code>false</code> when it is
     *         {@link RuleStatus#DISABLED} and <code>null</code> when it is not available.
     */
    public Boolean isEnabled(String ruleUID);

    /**
     * The method "runNow(ruleUID)" skips the triggers and the conditions and directly executes the actions of the rule.
     * This should always be possible unless an action has a mandatory input that is linked to a trigger.
     * In that case the action is skipped and the RuleEngine continues execution of rest actions.
     *
     * @param ruleUID id of rule whose actions have to be executed
     *
     */
    public void runNow(String ruleUID);

    /**
     * Same as {@link RuleRegistry#runNow(String)} with additional option to enable/disable evaluation of
     * conditions defined in the target rule. The context can be set here, too but also might be null.
     *
     * @param ruleUID id of rule whose actions have to be executed
     * @param considerConditions if <code>true</code> the conditions will be checked
     * @param context the context that is passed to the conditions and the actions
     */
    public void runNow(String ruleUID, boolean considerConditions, Map<String, Object> context);

}
