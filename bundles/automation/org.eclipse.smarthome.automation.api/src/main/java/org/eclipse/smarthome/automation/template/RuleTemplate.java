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

package org.eclipse.smarthome.automation.template;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.automation.descriptor.RuleDescriptor;

/**
 * This interface provides common functionality for creating {@link Rule}
 * instances. The {@link Rule}s are main tasks executed by Rule Engine. Each
 * rule has "ON", "IF" and "THEN" sections:
 * <ul>
 * <li>ON - contains {@link Trigger} modules. The triggers defines what the Rule
 * is listen to.
 * <li>IF - contains {@link Condition} modules which determinate if the Rule is
 * satisfied or not. When all conditions are satisfied Rule can proceed with
 * execution of THEN part.
 * <li>THEN - contains {@link Action} modules. The {@link Action}s are the
 * result of the Rule.
 * </ul>
 * The rule template returns {@link RuleDescriptor}. This descriptor along with
 * configuration values can be used to create a {@link Rule} instance.
 * Each {@link RuleTemplate} is defined by unique type and
 * {@link RuleDescriptor}. The type is unique in scope of all templates
 * registered in Rule Engine. The rule descriptor contains meta info for
 * {@link ConfigDescriptionParameter}s, {@link Module} instances created by rule
 * template.
 * 
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface RuleTemplate extends Template {

  /**
   * This method is used to get the descriptor of Template.
   * 
   * @return {@link RuleDescriptor} of Template.
   */
  public RuleDescriptor getDescriptor();

}
