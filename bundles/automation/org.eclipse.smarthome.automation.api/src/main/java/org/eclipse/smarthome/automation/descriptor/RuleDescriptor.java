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

package org.eclipse.smarthome.automation.descriptor;

import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * The RuleDescriptor is a {@link Descriptor} which defines {@link Rule}
 * instances.<br/>
 * It describes building parts of the {@link Rule}. They are separated into
 * three sections:
 * <ul>
 * <li>"ON" section - this is the section of triggers. The triggers defines what
 * the Rule is listen to.
 * <li>"IF" section - the section of the conditions. It contains an ordered list
 * of conditions which has to be satisfied to continue rule execution.
 * <li>"THEN" section - the action section. It contains ordered list of actions
 * which have to be executed when all condition have been satisfied.
 * </ul>
 * The RuleDescriptor defines module instances, their order, connections between
 * them and {@link ConfigDescriptionParameter}s of the {@link Rule}.
 * 
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public class RuleDescriptor extends Descriptor {

  private LinkedHashSet<Trigger> triggers;
  private LinkedHashSet<Condition> conditions;
  private LinkedHashSet<Action> actions;

  /**
   * Constructor of simple RuleDescriptor. It contains one trigger, an optional
   * condition and one action.
   * 
   * @param configInfo is a {@link Map} of {@link ConfigDescriptionParameter}s.
   *          Each entry of the map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the
   *          {@link ConfigDescriptionParameter} ,
   *          <li><code>value</code> - {@link ConfigDescriptionParameter}
   *          instance
   *          </ul>
   * @param trigger the {@link Trigger} participating in the {@link Rule}
   * @param condition the {@link Condition} participating in the {@link Rule}.
   *          It can be null.
   * @param action the {@link Action} participating in the {@link Rule}.
   */
  public RuleDescriptor(Map<String, ConfigDescriptionParameter> configInfo,
                        Trigger trigger, Condition condition, Action action) {
    super(configInfo);
    LinkedHashSet<Trigger> triggers = new LinkedHashSet<Trigger>(3);
    triggers.add(trigger);
    setTriggers(triggers);
    LinkedHashSet<Condition> conditions = new LinkedHashSet<Condition>(3);
    conditions.add(condition);
    setConditions(conditions);
    LinkedHashSet<Action> actions = new LinkedHashSet<Action>(3);
    actions.add(action);
    setActions(actions);
  }

  /**
   * Constructs RuleDescriptor with multiple Triggers, Conditions and Actions
   * 
   * @param triggers ordered set of {@link Trigger}s
   * @param conditions ordered set of {@link Condition}s. It is an optional
   *          parameter.
   * @param actions ordered set of {@link Action}s
   */
  public RuleDescriptor(Map<String, ConfigDescriptionParameter> configInfo,
                        LinkedHashSet<Trigger> triggers,
                        LinkedHashSet<Condition> conditions, LinkedHashSet<Action> actions) {
    super(configInfo);
    setTriggers(triggers);
    setConditions(conditions);
    setActions(actions);
  }

  /**
   * This method is used for getting the {@link Trigger}s of the Rule.
   * 
   * @return ordered set of {@link Trigger}s of the Rule
   */
  public LinkedHashSet<Trigger> getTriggers() {
    return triggers;
  }

  /**
   * This method is used for getting the {@link Condition}(s) of the Rule.
   * 
   * @return ordered set of {@link Condition}s of the Rule
   */
  public LinkedHashSet<Condition> getConditions() {
    return conditions;
  }

  /**
   * This method is used for getting the {@link Action}(s) of the Rule.
   * 
   * @return the ordered set of {@link Action}s of the Rule.
   */
  public LinkedHashSet<Action> getActions() {
    return actions;
  }

  /**
   * This method is used for setting the {@link Trigger}s of the Rule.
   * 
   * @param triggers ordered set of {@link Trigger}s of the Rule.
   */
  private void setTriggers(LinkedHashSet<Trigger> triggers) {
    // TODO validate trigger unique ids
    this.triggers = triggers;
  }

  /**
   * This method is used for setting the {@link Condition}(s) of the Rule
   * 
   * @param conditions is a ordered set of {@link Condition}s of the Rule.
   */
  private void setConditions(LinkedHashSet<Condition> conditions) {
    // TODO validate trigger unique ids
    this.conditions = conditions;
  }

  /**
   * This method is used for setting the {@link Action}(s) of the Rule.
   * 
   * @param actions the ordered set of {@link Action}s of the Rule.
   */
  private void setActions(LinkedHashSet<Action> actions) {
    // TODO validate trigger unique ids
    this.actions = actions;
  }

}
