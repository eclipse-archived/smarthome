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

import java.util.Map;

import org.eclipse.smarthome.automation.descriptor.Descriptor;
import org.eclipse.smarthome.automation.descriptor.RuleDescriptor;
import org.eclipse.smarthome.core.common.registry.Registry;


/**
 * @author Yordan Mihaylov
 */
public interface RuleRegistry extends Registry<Rule, String> {
  
  /**
   * This method is used to create a rule from descriptor and configuration 
   * @param descriptor {@link RuleDescriptor} of this rule.
   * @param configValues configuration values of this rule.
   * @param idenitity an identity scope to which this rule belongs to.
   * @return a new rule associated with concrete identity scope.
   * @throws SecurityException when the caller does not have permissions to access
   * all identity scopes.
   */
  public Rule create(RuleDescriptor descriptor, Map<String, ?> configValues);

  /**
   * This method is used for changing enable state of the Rule.  
   * @param uid unique identifier of the rule  
   * @param isEnabled a new active state of the rule.
   */
  public void setEnabled(String uid, boolean isEnabled);
  
  /**
   * This method is used for checking active state of the Rule. Only inactive Rules can change their properties.
   * @param uid unique identifier of the rule
   * @return true when the rule is active.
   */
  public boolean isEnabled(String uid);
  
  /**
   * This method is used for checking the running state of the Rule. 
   * The Rule is running if it is triggered and execution of its actions are not finished. 
   * @param uid unique identifier of the rule 
   * @return true when the Rule is running.
   */
  public boolean isRunning(String uid);

  
}
