package org.eclipse.smarthome.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
 * evaluation of the Rule {@link Condition}s will determine if the Rule will be executed.
 * A Rule will be executed only when all it's {@link Condition}s are satisfied. If the {@link Condition}s
 * list is empty, the Rule is considered satisfied.
 * <li><b>Actions:</b> a list of {@link Action} modules. These modules determine the actions that
 * will be performed when a Rule is executed.
 * </ul>
 * Additionally, Rules can have <code><b>tags</b></code> - non-hierarchical keywords or terms for describing them.
 * They can help the user to classify or label the Rules, and to filter and search them.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 * @author Kai Kreuzer - Introduced transient status and made it implement the Rule interface
 * @author Markus Rathgeb - Drop split between interface and implementation
 */
@NonNullByDefault
public class Rule implements Identifiable<String> {

    @NonNullByDefault({})
    protected List<Trigger> triggers;
    @NonNullByDefault({})
    protected List<Condition> conditions;
    @NonNullByDefault({})
    protected List<Action> actions;
    @NonNullByDefault({})
    protected Configuration configuration;
    @NonNullByDefault({})
    protected List<ConfigDescriptionParameter> configDescriptions;
    @Nullable
    protected String templateUID;
    @NonNullByDefault({})
    protected String uid;
    @Nullable
    protected String name;
    @NonNullByDefault({})
    protected Set<String> tags;
    @NonNullByDefault({})
    protected Visibility visibility;
    @Nullable
    protected String description;

    private transient volatile RuleStatusInfo status = new RuleStatusInfo(RuleStatus.UNINITIALIZED,
            RuleStatusDetail.NONE);

    /**
     * Package protected default constructor to allow reflective instantiation.
     *
     * !!! DO NOT REMOVE - Gson needs it !!!
     */
    Rule() {
    }

    /**
     * Constructor for creating an empty {@link Rule} with a specified rule identifier.
     * When {@code null} is passed for the {@code uid} parameter, the {@link Rule}'s identifier will
     * be randomly generated.
     *
     * @param uid the rule's identifier, or {@code null} if a random identifier should be generated.
     */
    public Rule(@Nullable String uid) {
        this(uid, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Utility constructor for creating a {@link Rule} from a set of modules, or from a template.
     * When {@code null} is passed for the {@code uid} parameter, the {@link Rule}'s identifier will be randomly
     * generated.
     *
     * @param uid the {@link Rule}'s identifier, or {@code null} if a random identifier should be
     *            generated.
     * @param name the rule's name
     * @param description the rule's description
     * @param tags the tags
     * @param triggers the {@link Rule}'s triggers list, or {@code null} if the {@link Rule} should
     *            have no
     *            triggers or
     *            will be created from a template.
     * @param conditions the {@link Rule}'s conditions list, or {@code null} if the {@link Rule} should
     *            have no
     *            conditions, or will be created from a template.
     * @param actions the {@link Rule}'s actions list, or {@code null} if the {@link Rule} should
     *            have no
     *            actions, or will be created from a template.
     * @param configDescriptions metadata describing the configuration of the {@link Rule}.
     * @param configuration the values that will configure the modules of the {@link Rule}.
     * @param templateUID the {@link RuleTemplate} identifier of the template that will be used by the
     *            {@link RuleRegistry} to validate the {@link Rule}'s configuration, as well as to
     *            create and
     *            configure
     *            the {@link Rule}'s modules, or null if the {@link Rule} should not be created
     *            from a template.
     * @param visibility the {@link Rule}'s visibility
     */
    public Rule(@Nullable String uid, final @Nullable String name, final @Nullable String description,
            final @Nullable Set<String> tags, @Nullable List<Trigger> triggers, @Nullable List<Condition> conditions,
            @Nullable List<Action> actions, @Nullable List<ConfigDescriptionParameter> configDescriptions,
            @Nullable Configuration configuration, @Nullable String templateUID, @Nullable Visibility visibility) {
        this.uid = uid == null ? UUID.randomUUID().toString() : uid;
        this.name = name;
        this.description = description;
        this.tags = tags == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(tags));
        this.triggers = triggers == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(triggers));
        this.conditions = conditions == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(conditions));
        this.actions = actions == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(actions));
        this.configDescriptions = configDescriptions == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(configDescriptions));
        this.configuration = configuration == null ? new Configuration()
                : new Configuration(configuration.getProperties());
        this.templateUID = templateUID;
        this.visibility = visibility == null ? Visibility.VISIBLE : visibility;
    }

    /**
     * This method is used to obtain the identifier of the Rule. It can be specified by the {@link Rule}'s
     * creator, or randomly generated.
     *
     * @return an identifier of this {@link Rule}. Can't be {@code null}.
     */
    @Override
    public String getUID() {
        return uid;
    }

    /**
     * This method is used to obtain the {@link RuleTemplate} identifier of the template the {@link Rule} was created
     * from. It will be used by the {@link RuleRegistry} to resolve the {@link Rule}: to validate the {@link Rule}'s
     * configuration, as well as to create and configure the {@link Rule}'s modules. If a {@link Rule} has not been
     * created from a template, or has been successfully resolved by the {@link RuleRegistry}, this method will return
     * {@code null}.
     *
     * @return the identifier of the {@link Rule}'s {@link RuleTemplate}, or {@code null} if the {@link Rule} has not
     *         been created from a template, or has been successfully resolved by the {@link RuleRegistry}.
     */
    @Nullable
    public String getTemplateUID() {
        return templateUID;
    }

    /**
     * This method is used to obtain the {@link Rule}'s human-readable name.
     *
     * @return the {@link Rule}'s human-readable name, or {@code null}.
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * This method is used to obtain the {@link Rule}'s assigned tags.
     *
     * @return the {@link Rule}'s assigned tags.
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * This method is used to obtain the human-readable description of the purpose and consequences of the
     * {@link Rule}'s execution.
     *
     * @return the {@link Rule}'s human-readable description, or {@code null}.
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * This method is used to obtain the {@link Rule}'s {@link Visibility}.
     *
     * @return the {@link Rule}'s {@link Visibility} value.
     */
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * This method is used to obtain the {@link Rule}'s {@link Configuration}.
     *
     * @return current configuration values, or an empty {@link Configuration}.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * This method is used to obtain the {@link List} with {@link ConfigDescriptionParameter}s defining meta info for
     * configuration properties of the {@link Rule}.
     *
     * @return a {@link List} of {@link ConfigDescriptionParameter}s.
     */
    public List<ConfigDescriptionParameter> getConfigurationDescriptions() {
        return configDescriptions;
    }

    /**
     * This method is used to get the conditions participating in {@link Rule}.
     *
     * @return a list with the conditions that belong to this {@link Rule}.
     */
    public List<Condition> getConditions() {
        return conditions;
    }

    /**
     * This method is used to get the actions participating in {@link Rule}.
     *
     * @return a list with the actions that belong to this {@link Rule}.
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * This method is used to get the triggers participating in {@link Rule}.
     *
     * @return a list with the triggers that belong to this {@link Rule}.
     */
    public List<Trigger> getTriggers() {
        return triggers;
    }

    /**
     * This method is used to get a {@link Module} participating in {@link Rule}
     *
     * @param moduleId specifies the id of a module belonging to this {@link Rule}.
     * @return module with specified id or {@code null} if it does not belong to this {@link Rule}.
     */
    public @Nullable Module getModule(String moduleId) {
        for (Module module : getModules()) {
            if (module.getId().equals(moduleId)) {
                return module;
            }
        }
        return null;
    }

    /**
     * Obtains the modules of the {@link Rule}.
     *
     * @return the modules of the {@link Rule} or empty list if the {@link Rule} has no modules.
     */
    public List<Module> getModules() {
        final List<Module> result;
        List<Module> modules = new ArrayList<Module>();
        modules.addAll(triggers);
        modules.addAll(conditions);
        modules.addAll(actions);
        result = Collections.unmodifiableList(modules);
        return result;
    }

    /**
     * Gets the status of a rule. In order to get all status information (status, status detail and status description)
     * please use {@link Rule#getStatusInfo()}.
     *
     * @return the status
     */
    public RuleStatus getStatus() {
        return status.getStatus();
    }

    /**
     * Gets the status info of a rule. The status info consists of the status itself, the status detail and a status
     * description.
     *
     * @return the status info
     */
    public RuleStatusInfo getStatusInfo() {
        return status;
    }

    /**
     * Provides information on whether the rule is currently enabled or not.
     *
     * @return true, if the rule is enabled
     */
    public boolean isEnabled() {
        return status.getStatusDetail() != RuleStatusDetail.DISABLED;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + uid.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Rule)) {
            return false;
        }
        Rule other = (Rule) obj;
        if (!uid.equals(other.uid)) {
            return false;
        }
        return true;
    }

    public synchronized void setStatusInfo(RuleStatusInfo statusInfo) {
        this.status = statusInfo;
    }

}