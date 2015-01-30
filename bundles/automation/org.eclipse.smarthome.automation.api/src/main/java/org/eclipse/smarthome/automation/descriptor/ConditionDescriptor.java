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

import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Input;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class is a {@link Descriptor} for {@link Condition} Module. It defines
 * {@link ConfigDescriptionParameter}s and {@link Input}s of the condition.
 */
public class ConditionDescriptor extends Descriptor {

  private Map<String, Input> inputInfo;

  /**
   * Default constructor of ConditionDescriptor. Constructs an empty
   * ConditionDescriptor.
   * 
   * @param configInfo is a {@link Map} of {@link ConfigDescriptionParameter}s.
   *          Each entry of the map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the
   *          {@link ConfigDescriptionParameter} ,
   *          <li><code>value</code> - {@link ConfigDescriptionParameter}
   *          instance
   *          </ul>
   * @param inputInfo is a {@link Map} of {@link Input}s. Each entry of the map
   *          contains:
   *          <ul>
   *          <li><code>key</code> - the name of the {@link Input} ,
   *          <li><code>value</code> - {@link Input} instance
   *          </ul>
   */
  public ConditionDescriptor(Map<String, ConfigDescriptionParameter> configInfo,
                             Map<String, Input> inputInfo) {
    super(configInfo);
    this.inputInfo = inputInfo;
  }

  /**
   * This method is used for getting the Map with {@link Input}s defined by this
   * descriptor.<br/>
   * 
   * @return a {@link Map} of condition {@link Input}s or null. Each entry of
   *         the map contains:
   *         <ul>
   *         <li><code>key</code> - the name of the {@link Input} ,
   *         <li><code>value</code> - {@link Input} instance
   *         </ul>
   */
  public Map<String, Input> getInputs() {
    return inputInfo;
  }

}
