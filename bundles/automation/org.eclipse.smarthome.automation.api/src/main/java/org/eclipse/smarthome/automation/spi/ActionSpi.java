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

import org.eclipse.smarthome.automation.Action;

/**
 * This is general interface for all Action services. When proceed to 'THEN'
 * clause of a {@link Rule} the RuleEngine is going through all {@link Action}s
 * defined by the {@link Rule}. After needed information is taken from the
 * {@link Action} module a service that has implemented this interface is being
 * called.
 * 
 */
public interface ActionSpi extends ModuleSpi {

  /**
   * Method that will be called by the RuleEngine.
   * 
   * @param configValues configuration of the Action
   * @param inputs the values of the input of the Action
   * @return values of outputs in case when the action has Outputs or null.
   */
  public Map<String, Object> execute(Map<String, ?> configValues, Map<String, ?> inputs);

}
