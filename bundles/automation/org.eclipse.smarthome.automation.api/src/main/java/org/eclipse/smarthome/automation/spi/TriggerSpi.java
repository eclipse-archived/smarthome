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

import org.eclipse.smarthome.automation.Trigger;

/**
 * This SPI interface is used by {@link Trigger} modules to set a callback
 * object to TriggerSpi instance, which is created by external implementations
 * of {@link ModuleSpiFactory} service. The callback has to implemented
 * {@link RuleEngineCallback} interface and it is used to notify the RuleEngine
 * when {@link Trigger} module is triggered
 * 
 */
public interface TriggerSpi extends ModuleSpi {

  /**
   * This method is used to set a callback object to the RuleEngine
   * 
   * @param reCallback a callback object to the RuleEngine.
   */
  public void setRuleEngineCallback(RuleEngineCallback reCallback);
}
