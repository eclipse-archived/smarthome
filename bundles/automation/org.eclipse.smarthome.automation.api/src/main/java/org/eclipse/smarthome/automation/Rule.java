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
import org.eclipse.smarthome.automation.descriptor.RuleDescriptor;

/**
 * Rule is built from {@link Module}s and consists of three sections:
 * ON/IF/THEN.
 * <ul>
 * <li>ON - contains {@link Trigger} modules. The triggers defines what the Rule
 * is listen to.
 * <li>IF - contains {@link Condition} modules which determinate if the Rule is
 * satisfied or not. When all conditions are satisfied Rule can proceed with
 * execution of THEN part.
 * </ul>
 * Participating {@link Module} instances and {@link ConfigDescriptionParameter}
 * s of the Rule are defined by {@link RuleDescriptor} Rules can have <li>
 * <code>tags</code> - non-hierarchical keywords or terms for describing them.
 * They help for classifying the items and allow them to be found.
 * 
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface Rule {

  /**
   * This method is used for getting the unique identifier of the Rule. This
   * property is set by the RuleEngine when the {@link Rule} is added.
   * 
   * @return uid of this {@link Rule}
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
   * @param name a new name.
   * @throws IllegalStateException when the rule is in active state
   */
  public void setName(String name) throws IllegalStateException;

  /**
   * This method is used for getting the label of the Rule. The label is a
   * short, user friendly name of the Rule defined by this descriptor.
   * 
   * @return the label of the Rule.
   */
  public String getLabel();

  /**
   * This method is used for setting the label of the Rule. The label is a
   * short, user friendly name of the Rule defined by this descriptor.
   * 
   * @param label of the Rule.
   */
  public void setLabel(String label);

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
   * @param description of the Rule.
   */
  public void setDescription(String description);

  /**
   * This method is used for getting Map with configuration values of the
   * {@link Rule} Key -id of the {@link ConfigDescriptionParameter} Value - the
   * value of the corresponding property
   * 
   * @return current configuration values
   */
  public Map<String, Object> getConfigValues();

  /**
   * This method is used for setting the Map with configuration values of the
   * {@link Rule}. Key - id of the {@link ConfigDescriptionParameter} Value -
   * the value of the corresponding property
   * 
   * @param config new configuration values.
   */
  public void setConfigValues(Map<String, ?> config);

  /**
   * Rules can have <li><code>tags</code> - non-hierarchical keywords or terms
   * for describing them. This method is used for getting the tags assign to
   * this Rule. The tags are used to filter the rules.
   * 
   * @return a list of tags
   */
  public Set<String> getTags();

  /**
   * This method is used to get a module participating in Rule
   * 
   * @param id unique id of the module in this rule.
   * @return module with specified id or null when it does not exist.
   */
  public <T extends Module> T getModule(String id);

  /**
   * This method is used to return a group of module of this rule
   * 
   * @param clazz optional parameter defining type looking modules. The types
   *          are {@link Trigger}, {@link Condition} or {@link Action}
   * @return list of modules of defined type or all modules when the type is not
   *         specified.
   */
  public <T extends Module> List<T> getModules(T clazz);

  /**
   * Rules can have <li><code>tags</code> - non-hierarchical keywords or terms
   * for describing them. This method is used for setting the tags to this rule.
   * This property can be changed only when the Rule is not in active state. The
   * tags are used to filter the rules.
   * 
   * @param tags list of tags assign to this Rule.
   * @throws IllegalStateException IllegalStateException when the rule is in
   *           active state.
   */
  public void setTags(Set<String> tags) throws IllegalStateException;

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
