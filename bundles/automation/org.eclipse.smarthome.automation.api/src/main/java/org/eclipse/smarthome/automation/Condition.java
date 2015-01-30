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
import org.eclipse.smarthome.automation.descriptor.ConditionDescriptor;

/**
 * Condition module is used into "IF" section of the {@link Rule} definition.
 * The "IF" section defines conditions which must be satisfied to continue
 * {@link Rule} execution. Building elements of condition (
 * {@link ConfigDescriptionParameter}s and {@link Input}s are defined by
 * {@link ConditionDescriptor} Conditions don't have {@link Output} elements.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface Condition extends Module {

  public ConditionDescriptor getDescriptor();

  /**
   * This method is used to get input connections of the Condition. The
   * connections are links between {@link Input}s of the {@link Module} and
   * {@link Output}s of other {@link Module}s.
   * 
   * @return a {@link Map} of input connections. Each entry of the map contains:
   *         <ul>
   *         <li><code>key</code> - the name of the {@link Input} ,
   *         <li><code>value</code> - represents output id of the {@link Module}
   *         in form: ModuleId.OutputName
   *         </ul>
   */
  public Map<String, String> getConnections();

  /**
   * This method is used to connect {@link Input}s of the Condition to
   * {@link Output}s of other {@link Module}s.
   * 
   * @param connections is a {@link Map} of input connections. Each entry of the
   *          map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the {@link Input} ,
   *          <li><code>value</code> - represents output id of the
   *          {@link Module} in form: ModuleId.OutputName
   *          </ul>
   */
  public void setConnections(Map<String, String> connections);

}
