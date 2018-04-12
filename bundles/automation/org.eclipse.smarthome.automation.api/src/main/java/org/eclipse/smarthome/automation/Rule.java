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
 * An automation Rule is built from {@link Module}s and consists of three parts:
 * <ul>
 * <li><b>Triggers:</b> a list of {@link Trigger} modules. Each {@link Trigger} from this list
 * can start the evaluation of the Rule. A Rule with an empty list of {@link Trigger}s can
 * only be triggered through the {@link RuleRegistry#runNow(String, boolean, java.util.Map)} method,
 * or directly executed with the {@link RuleRegistry#runNow(String)} method.
 * <li><b>Conditions:</b> a list of {@link Condition} modules. When a Rule is triggered, the
 * evaluation of the Rule {@link Condition}s will determine if the RuleImpl will be executed.
 * A Rule will be executed only when all it's {@link Condition}s are satisfied. If the {@link Condition}s
 * list is empty, the Rule is considered satisfied.
 * <li><b>Actions:</b> a list of {@link Action} modules. These modules determine the actions that
 * will be performed when a Rule is executed.
 * </ul>
 * Additionally, Rules can have <code><b>tags</b></code> - non-hierarchical keywords or terms for describing them.
 * They can help the user to classify or label the Rules, and to filter and search them.
 *
 * @author Kai Kreuzer - Initial Contribution
 */
@NonNullByDefault
public interface Rule extends Identifiable<String> {

    /**
     * This method is used to obtain the identifier of the RuleImpl. It can be specified by the {@link RuleImpl}'s
     * creator, or randomly generated.
     *
     * @return an identifier of this {@link RuleImpl}. Can't be {@code null}.
     */
    @Override
    String getUID();

    /**
     * This method is used to obtain the {@link RuleTemplate} identifier of the template the {@link RuleImpl} was
     * created
     * from. It will be used by the {@link RuleRegistry} to resolve the {@link RuleImpl}: to validate the
     * {@link RuleImpl}'s
     * configuration, as well as to create and configure the {@link RuleImpl}'s modules. If a {@link RuleImpl} has not
     * been
     * created from a template, or has been successfully resolved by the {@link RuleRegistry}, this method will return
     * {@code null}.
     *
     * @return the identifier of the {@link RuleImpl}'s {@link RuleTemplate}, or {@code null} if the {@link RuleImpl}
     *         has not
     *         been created from a template, or has been successfully resolved by the {@link RuleRegistry}.
     */
    @Nullable
    String getTemplateUID();

    /**
     * This method is used to obtain the {@link RuleImpl}'s human-readable name.
     *
     * @return the {@link RuleImpl}'s human-readable name, or {@code null}.
     */
    @Nullable
    String getName();

    /**
     * This method is used to obtain the {@link RuleImpl}'s assigned tags.
     *
     * @return the {@link RuleImpl}'s assigned tags.
     */
    Set<String> getTags();

    /**
     * This method is used to obtain the human-readable description of the purpose and consequences of the
     * {@link RuleImpl}'s execution.
     *
     * @return the {@link RuleImpl}'s human-readable description, or {@code null}.
     */
    @Nullable
    String getDescription();

    /**
     * This method is used to obtain the {@link RuleImpl}'s {@link Visibility}.
     *
     * @return the {@link RuleImpl}'s {@link Visibility} value.
     */
    Visibility getVisibility();

    /**
     * This method is used to obtain the {@link RuleImpl}'s {@link Configuration}.
     *
     * @return current configuration values, or an empty {@link Configuration}.
     */
    Configuration getConfiguration();

    /**
     * This method is used to obtain the {@link List} with {@link ConfigDescriptionParameter}s
     * defining meta info for configuration properties of the {@link RuleImpl}.
     *
     * @return a {@link List} of {@link ConfigDescriptionParameter}s.
     */
    List<ConfigDescriptionParameter> getConfigurationDescriptions();

    /**
     * This method is used to get the conditions participating in {@link RuleImpl}.
     *
     * @return a list with the conditions that belong to this {@link RuleImpl}.
     */
    List<? extends Condition> getConditions();

    /**
     * This method is used to get the actions participating in {@link RuleImpl}.
     *
     * @return a list with the actions that belong to this {@link RuleImpl}.
     */
    List<? extends Action> getActions();

    /**
     * This method is used to get the triggers participating in {@link RuleImpl}
     *
     * @return a list with the triggers that belong to this {@link RuleImpl}.
     */
    List<? extends Trigger> getTriggers();

    /**
     * This method is used to obtain the modules of the {@link RuleImpl}, corresponding to the specified class.
     *
     * @param moduleClazz defines the class of the looking modules. It can be {@link Module}, {@link Trigger},
     *            {@link Condition} or {@link Action}.
     * @return the modules of defined type or empty list if the {@link RuleImpl} has no modules that belong to the
     *         specified
     *         type.
     */
    <T extends Module> List<T> getModules(Class<T> moduleClazz);

    /**
     * Gets the status of a rule.
     *
     * In order to get all status information (status, status detail and status description) please use
     * {@link RuleImpl#getStatusInfo()}.
     *
     * @return the status
     */
    RuleStatus getStatus();

    /**
     * Gets the status info of a rule.
     *
     * The status info consists of the status itself, the status detail and a status description.
     *
     * @return the status info
     */
    RuleStatusInfo getStatusInfo();

    /**
     * Provides information on whether the rule is currently enabled or not.
     *
     * @return true, if the rule is enabled
     */
    boolean isEnabled();

}