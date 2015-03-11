/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Rule is built from {@link Module}s and consists of three sections:
 * ON/IF/THEN.
 * <ul>
 * <li>ON - contains {@link Trigger} modules. The triggers defines what fires the the Rule.
 * <li>IF - contains {@link Condition} modules which determinate if the Rule is satisfied or not. When all conditions
 * are satisfied then the Rule can proceed with execution of THEN part.
 * <li>THEN - contains actions which have to be executed by the {@link Rule}
 * </ul>
 * Rules can have <code>tags</code> - non-hierarchical keywords or terms for
 * describing them. They help for classifying the items and allow them to be
 * found.
 *
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface Rule {

    /**
     * This method is used for getting the unique identifier of the Rule. This
     * property is set by the RuleEngine when the {@link Rule} is added.
     *
     * @return unique id of this {@link Rule}
     */
    public String getUID();

    /**
     * This method is used for getting the user friendly name of the {@link Rule}.
     * It's optional property.
     *
     * @return the name of rule or null.
     */
    public String getName();

    /**
     * This method is used for setting a friendly name of the Rule. This property
     * can be changed only when the Rule is not in active state.
     *
     * @param ruleName a new name.
     * @throws IllegalStateException when the rule is in active state
     */
    public void setName(String ruleName) throws IllegalStateException;

    /**
     * Rules can have <li><code>tags</code> - non-hierarchical keywords or terms for describing them. This method is
     * used for getting the tags assign to this Rule. The tags are used to filter the rules.
     *
     * @return a list of tags
     */
    public Set<String> getTags();

    /**
     * Rules can have <li><code>tags</code> - non-hierarchical keywords or terms for describing them. This method is
     * used for setting the tags to this rule. This property can be changed only when the Rule is not in active state.
     * The tags are used to filter the rules.
     *
     * @param ruleTags list of tags assign to this Rule.
     * @throws IllegalStateException IllegalStateException when the rule is in
     *             active state.
     */
    public void setTags(Set<String> ruleTags) throws IllegalStateException;

    /**
     * This method is used for getting the description of the Rule. The
     * description is a long, user friendly description of the Rule defined by
     * this descriptor.
     *
     * @return the description of the Rule.
     */
    public String getDescription();

    /**
     * This method is used for setting the description of the Rule. The
     * description is a long, user friendly description of the Rule defined by
     * this descriptor.
     *
     * @param ruleDescription of the Rule.
     */
    public void setDescription(String ruleDescription);

    /**
     * This method is used for getting the Set with {@link ConfigDescriptionParameter}s defining meta info for
     * configuration
     * properties of the Rule.<br/>
     *
     * @return a {@link Set} of {@link ConfigDescriptionParameter}s.
     */
    public Set<ConfigDescriptionParameter> getConfigurationDescriptions();

    /**
     * This method is used for getting Map with configuration values of the {@link Rule} Key -id of the
     * {@link ConfigDescriptionParameter} Value - the
     * value of the corresponding property
     *
     * @return current configuration values
     */
    public Map<String, Object> getConfiguration();

    /**
     * This method is used for setting the Map with configuration values of the {@link Rule}. Key - id of the
     * {@link ConfigDescriptionParameter} Value -
     * the value of the corresponding property
     *
     * @param ruleConfiguration new configuration values.
     */
    public void setConfiguration(Map<String, ?> ruleConfiguration);

    /**
     * This method is used to get a module participating in Rule
     *
     * @param moduleId unique id of the module in this rule.
     * @return module with specified id or null when it does not exist.
     */
    public <T extends Module> T getModule(String moduleId);

    /**
     * This method is used to return a group of module of this rule
     *
     * @param moduleClazz optional parameter defining type looking modules. The
     *            types are {@link Trigger}, {@link Condition} or {@link Action}
     * @return list of modules of defined type or all modules when the type is not
     *         specified.
     */
    public <T extends Module> List<T> getModules(Class<T> moduleClazz);

    /**
     * This method is used to get the identity scope of this Rule. The identity
     * defines a scope where the rule belongs to. It is set automatically by the
     * RuleEngine and is based on identity of rule creator.<br>
     * For example the identity can be application name or user name of creator.
     *
     * @return Rule's identity.
     */
    public String getScopeIdentifier();

}
