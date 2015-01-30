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

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.automation.descriptor.ActionDescriptor;
import org.eclipse.smarthome.automation.descriptor.ConditionDescriptor;
import org.eclipse.smarthome.automation.descriptor.Descriptor;
import org.eclipse.smarthome.automation.descriptor.RuleDescriptor;
import org.eclipse.smarthome.automation.descriptor.TriggerDescriptor;

/**
 * Modules are building components of the {@link Rule}s.
 * <p>
 * Each Module is identified by id. The id is unique in scope of the
 * {@link Rule}.
 * <p>
 * Each Module can have {@link Input}s, {@link Output}s and
 * {@link ConfigDescriptionParameter}s components. It is defined by
 * {@link Descriptor} containing meta info for its components.
 * 
 * <p>
 * Setters of the module don't have immediate effect on the Rule. To be applied
 * changes, the module has to be updated into the {@link RuleDescriptor} and the
 * {@link RuleDescriptor} has to be set on the {@link Rule}.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 *
 * @param <T> is an extension of {@link Descriptor}. For example
 *          {@link TriggerDescriptor}, {@link ConditionDescriptor} or
 *          {@link ActionDescriptor}.
 */
public interface Module {

  /**
   * This method is used for getting the id of the {@link Module}.
   * 
   * @return module id
   */
  public String getId();

  /**
   * This method is used for getting the {@link Descriptor} of the
   * {@link Module}.
   * 
   * @return {@link Descriptor} of the Module.
   */
  public Descriptor getDescriptor();

  /**
   * This method is used for getting the label of the Module. The label is a
   * short, user friendly name of the Module defined by this descriptor.
   * 
   * @return the label of the module.
   */
  public String getLabel();

  /**
   * This method is used for setting the label of the Module. The label is a
   * short, user friendly name of the Module defined by this descriptor.
   * 
   * @param label of the module.
   */
  public void setLabel(String label);

  /**
   * This method is used for getting the description of the Module. The
   * description is a long, user friendly description of the Module defined by
   * this descriptor.
   * 
   * @return the description of the module.
   */
  public String getDescription();

  /**
   * This method is used for setting the description of the Module. The
   * description is a long, user friendly description of the Module defined by
   * this descriptor.
   * 
   * @param description of the module.
   */
  public void setDescription(String description);

  /**
   * This method is used for getting Map with configuration values of the
   * {@link Module} Key -id of the {@link ConfigDescriptionParameter} Value -
   * the value of the corresponding property
   * 
   * @return current configuration values
   */
  public Map<String, Object> getConfigValues();

  /**
   * This method is used for setting the Map with configuration values of the
   * {@link Module}. Key - id of the {@link ConfigDescriptionParameter} Value -
   * the value of the corresponding property
   * 
   * @param config new configuration values.
   */
  public void setConfigValues(Map<String, ?> config);

}
