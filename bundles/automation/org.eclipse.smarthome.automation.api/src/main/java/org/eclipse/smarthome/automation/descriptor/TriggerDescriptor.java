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

import org.eclipse.smarthome.automation.Output;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class is a {@link Descriptor} for {@link Trigger} Module. It defines
 * {@link ConfigDescriptionParameter}s and {@link Output}s of the trigger.
 */
public abstract class TriggerDescriptor extends Descriptor {

  private Map<String, Output> outputsInfo;

  /**
   * Default constructor of TriggerDecsriptor. Constructs an empty
   * TriggerDecsriptor.
   * 
   * @param configInfo is a {@link Map} of {@link ConfigDescriptionParameter}s.
   *          Each entry of the map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the
   *          {@link ConfigDescriptionParameter} ,
   *          <li><code>value</code> - {@link ConfigDescriptionParameter}
   *          instance
   *          </ul>
   * @param outputInfo is a {@link Map} of {@link Output}s. Each entry of the
   *          map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the {@link Output} ,
   *          <li><code>value</code> - {@link ConfigDescriptionParameter}
   *          instance
   *          </ul>
   */
  public TriggerDescriptor(Map<String, ConfigDescriptionParameter> configInfo,
                           Map<String, Output> outputInfo) {
    super(configInfo);
    this.outputsInfo = outputInfo;
  }

  /**
   * This method is used for getting the Map with {@link Output}s defined by
   * this descriptor.<br/>
   * 
   * @return a {@link Map} of {@link Output}s. Each entry of the map contains:
   *         <ul>
   *         <li><code>key</code> - the name of the {@link Output} ,
   *         <li><code>value</code> - {@link Output} instance
   *         </ul>
   */
  public Map<String, Output> getOutputs() {
    return outputsInfo;
  }

}
