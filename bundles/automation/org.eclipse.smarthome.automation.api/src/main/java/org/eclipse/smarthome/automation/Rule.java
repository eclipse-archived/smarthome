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
package org.eclipse.smarthome.automation;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.Identifiable;

/**
 * This interface is used to represent automation {@link Rule}s which receiving real-time data, reasoning on that data
 * and invoking automated actions based on the result of that reasoning process. Automation Rules eliminate the need of
 * human intervention when monitoring, managing and controlling of various devices is needed. They are built from
 * {@link Module}s that belong to three sections:
 * <ul>
 * <li><b>Triggers:</b> a list of {@link Trigger} modules. Each {@link Trigger} from this list can start the evaluation
 * of the Rule. A Rule with an empty list of {@link Trigger}s can only be triggered through the
 * {@link #run(boolean, java.util.Map)} method, or directly executed with the {@link #run()} method.
 * <li><b>Conditions:</b> a list of {@link Condition} modules. When a Rule is triggered, the evaluation of the Rule
 * {@link Condition}s will determine if the Rule's {@link Action}s will be executed. A Rule will be executed only when
 * all it's {@link Condition}s are satisfied. If the {@link Condition}s list is empty, the Rule is considered satisfied.
 * <li><b>Actions:</b> a list of {@link Action} modules. These modules determine the actions that will be performed when
 * a Rule is executed.
 * </ul>
 * <p>
 * Additionally, Rules can have {@code tags} - non-hierarchical keywords or terms for describing them. They can help the
 * user to classify or label the Rules, and to filter and search them.
 * <p>
 * Manage the state (<b>enabled</b> or <b>disabled</b>) of the Rules:
 * <ul>
 * <li>A newly created Rule is always <b>enabled</b>.</li>
 * <li>To check a Rule's state, use the {@link #isEnabled()} method.</li>
 * <li>To change a Rule's state, use the {@link #setEnabled(boolean)} method.</li>
 * <li>The status of a Rule enabled with {@link #setEnabled(boolean)}, is first set to {@link RuleStatus#INITIALIZING}.
 * Further managing of the status is performed by {@link RuleManager}.</li>
 * <li>The status of a Rule disabled with {@link #setEnabled(boolean)}, is set to {@link RuleStatus#UNINITIALIZED} with
 * {@link RuleStatusDetail#DISABLED}.</li>
 * <li>To check a Rule's status info, use the {@link #getStatusInfo()} method.</li>
 * <li>To check a Rule's status, use the {@link #getStatus()} method.</li>
 * </ul>
 *
 * @author Kai Kreuzer - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 *
 */
@NonNullByDefault
public interface Rule extends Identifiable<String> {

    /**
     * Gets the unique identifier of the Rule. It can be specified by the {@link Rule}'s creator, or
     * randomly generated.
     *
     * @return the unique identifier of this {@link Rule}. Can't be {@code null}.
     */
    @Override
    String getUID();

    /**
     * Gets the unique {@link RuleTemplate} identifier of the template the {@link Rule} was created from. It will be
     * used by the {@link RuleRegistry} to resolve the {@link Rule}: to validate the {@link Rule}'s configuration, as
     * well as to create and configure the {@link Rule}'s modules. If a {@link Rule} has not been created from a
     * template, or has been successfully resolved by the {@link RuleRegistry}, this method will return {@code null}.
     *
     * @return the identifier of the {@link Rule}'s {@link RuleTemplate}, or {@code null} if the {@link Rule} has
     *         not been created from a template, or has been successfully resolved by the {@link RuleRegistry}.
     */
    @Nullable
    String getTemplateUID();

    /**
     * Gets the {@link Rule}'s human-readable name.
     *
     * @return the {@link Rule}'s human-readable name, or {@code null}.
     */
    @Nullable
    String getName();

    /**
     * Gets the {@link Rule}'s assigned tags.
     *
     * @return the {@link Rule}'s assigned tags.
     */
    Set<String> getTags();

    /**
     * Gets the human-readable description of the purpose and consequences of the {@link Rule}'s execution.
     *
     * @return the {@link Rule}'s human-readable description, or {@code null}.
     */
    @Nullable
    String getDescription();

    /**
     * Gets the {@link Rule}'s {@link Visibility}.
     *
     * @return the {@link Rule}'s {@link Visibility} value.
     */
    Visibility getVisibility();

    /**
     * Gets the {@link Rule}'s {@link Configuration}.
     *
     * @return current configuration values, or an empty {@link Configuration}.
     */
    Configuration getConfiguration();

    /**
     * Gets the {@link List} with {@link ConfigDescriptionParameter}s defining meta info for configuration properties
     * of the {@link Rule}.
     *
     * @return a {@link List} of {@link ConfigDescriptionParameter}s.
     */
    List<ConfigDescriptionParameter> getConfigurationDescriptions();

    /**
     * Gets the conditions participating in the {@link Rule}.
     *
     * @return a list with the conditions that belong to this {@link Rule}.
     */
    List<Condition> getConditions();

    /**
     * Gets the actions participating in the {@link Rule}.
     *
     * @return a list with the actions that belong to this {@link Rule}.
     */
    List<Action> getActions();

    /**
     * Gets the triggers participating in the {@link Rule}.
     *
     * @return a list with the triggers that belong to this {@link Rule}.
     */
    List<Trigger> getTriggers();

    /**
     * Gets the modules participating in the {@link Rule}.
     *
     * @return the modules of the {@link Rule} or empty list if the {@link Rule} has no modules.
     */
    List<Module> getModules();

    /**
     * Gets a {@link Module} participating in the {@link Rule}.
     *
     * @param moduleId specifies the identifier of a module belonging to this {@link Rule}.
     * @return module with specified identifier or {@code null} if such does not belong to this {@link Rule}.
     */
    default @Nullable Module getModule(String moduleId) {
        for (Module module : getModules()) {
            if (module.getId().equals(moduleId)) {
                return module;
            }
        }
        return null;
    }
}
