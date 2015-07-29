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

import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This interface is used to create module instances. It is registered as
 * service in OSGi registry.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public interface AutomationFactory {

    /**
     * This method creates and configures {@link Trigger} instance
     * 
     * @param id unique id of {@link Module} in scope of Rule.
     * @param typeUID unique id of existing ModuleType containing meta info for
     *            this {@link Trigger}.
     * @param configurations is a {@link Map} of configuration property values.
     *            Each entry of the map contains:
     *            <ul>
     *            <li><code>key</code> - the name of the {@link ConfigDescriptionParameter} ,
     *            <li><code>value</code> - value of the {@link ConfigDescriptionParameter}
     *            </ul>
     * 
     * @return {@link Trigger}
     * @throws IllegalArgumentException when the module id is already used or
     *             module type UID is not registered.
     */
    public Trigger createTrigger(String id, String typeUID, Map<String, ?> configurations);

    /**
     * This method creates and configures {@link Condition} instance
     * 
     * @param id unique id of {@link Module} in scope of Rule.
     * @param typeUID unique id of existing ModuleType containing meta info for
     *            this {@link Condition}
     * @param config is a {@link Map} of configuration property values. Each entry
     *            of the map contains:
     *            <ul>
     *            <li><code>key</code> - the name of the {@link ConfigDescriptionParameter} ,
     *            <li><code>value</code> - value of the {@link ConfigDescriptionParameter}
     *            </ul>
     * @param connections a {@link Set} of input {@link Connection}s.
     * 
     * @return {@link Condition}
     * @throws IllegalArgumentException when the module id is already used or UID
     *             of module type is not registered.
     */
    public Condition createCondition(String id, String typeUID, Map<String, ?> config, Set<Connection> connections);

    /**
     * This method creates and configures {@link Action} instance
     * 
     * @param id unique id of {@link Module} in scope of Rule. for
     * @param typeUID unique id of existing ModuleType containing meta info for
     *            this Action
     * @param configurations is a {@link Map} of configuration property values.
     *            Each entry of the map contains:
     *            <ul>
     *            <li><code>key</code> - the name of the {@link ConfigDescriptionParameter} ,
     *            <li><code>value</code> - value of the {@link ConfigDescriptionParameter}
     *            </ul>
     * @param connections is a a {@link Set} of input {@link Connection}s.
     * 
     * @return {@link Action}
     * @throws IllegalArgumentException when the module id is already used or UID
     *             of module type is not registered.
     */
    public Action createAction(String id, String typeUID, Map<String, ?> configurations, Set<Connection> connections);

    /**
     * This method is used to create a {@link Rule} instance.
     * 
     * @param triggers list of {@link Trigger}s of the {@link Rule}
     * @param conditions is a list of {@link Condition}s of the {@link Rule}
     * @param actions list of {@link Action} of the {@link Rule}
     * @param configDescriptions meta info of properties of the {@link Rule}.
     * @param configurations are configuration values of the {@link Rule}
     * 
     * @return {@link Rule} instance.
     * @throws IllegalArgumentException is thrown if the connections between modules are incorrect.
     */
    public Rule createRule(
            List<Trigger> triggers, //
            List<Condition> conditions, List<Action> actions, Set<ConfigDescriptionParameter> configDescriptions,
            Map<String, ?> configurations);

    /**
     * This method is used to create a {@link Rule} instance from {@link RuleTemplate}.
     * 
     * @param ruleTemplateUID is an unique identifier of the {@link RuleTemplate} instance.
     * @param configurations are configuration values of the {@link Rule} instance.
     * @return {@link Rule} instance.
     * @throws Exception is thrown when the configuration values for required configuration properties are missing or
     *             their type is wrong.
     */
    public Rule createRule(String ruleTemplateUID, Map<String, Object> configurations);

}
