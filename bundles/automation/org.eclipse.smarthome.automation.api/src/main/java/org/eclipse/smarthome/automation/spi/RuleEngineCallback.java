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

package org.eclipse.smarthome.automation.spi;

import java.util.Map;

import org.eclipse.smarthome.automation.Output;
import org.eclipse.smarthome.automation.Trigger;

/**
 * This is a callback interface to RuleEngine which is used by the
 * {@link TriggerSpi} to notify the RuleEngine about triggering of the
 * {@link Trigger}. These calls from {@link Trigger}s must be stored in a queue
 * and applied ty the RuleAngine in order of their appearance.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface RuleEngineCallback {

  /**
   * This method is used by the {@link TriggerSpi} to notify the RuleEngine
   * that, the trigger defined by trigger id, is triggered and its output values
   * are applied into second parameter outputValues.
   * 
   * @param triggerSpi instance of trigger spi object corresponding to the
   *          Trigger
   * @param outputValues is a {@link Map} of output values of the triggered
   *          {@link Trigger}. Each entry of the map contains:
   *          <ul>
   *          <li><code>key</code> - the id of the {@link Output} ,
   *          <li><code>value</code> - represents output value of the
   *          {@link Trigger}'s {@link Output}
   *          </ul>
   */
  public void triggered(TriggerSpi triggerSpi, Map<String, ?> outputValues);
}
