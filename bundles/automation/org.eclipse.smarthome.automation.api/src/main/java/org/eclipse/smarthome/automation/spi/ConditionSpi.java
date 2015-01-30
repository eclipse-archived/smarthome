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

import org.eclipse.smarthome.automation.Condition;

/**
 * This spi interface is used by condition. When proceed to 'IF' clause of a
 * {@link Rule} the RuleEngine is going through all {@link Condition}s defined
 * by the {@link Rule}. After needed information is taken from the
 * {@link Condition} module a service that has implemented this interface is
 * being called.
 * 
 */
public interface ConditionSpi extends ModuleSpi {

  /**
   * Method that states if the Condition is satisfied or not
   * 
   * @param configValues configuration of the Condition
   * @param inputs the values of condition inputs.
   * @return true if Condition is satisfied, false otherwise
   */
  public boolean isSatisfied(Map<String, ?> configValues, Map<String, ?> inputs);

}
