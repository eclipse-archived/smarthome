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

import org.eclipse.smarthome.automation.Module;

/**
 * This class is a factory of {@link ModuleSpi} instances. It is used to create
 * {@link TriggerSpi}, {@link ConditionSpi} and {@link ActionSpi} objects base
 * on passed configuration properties. The ModuleSpiFactory is register as
 * service in OSGi framework and it is used by automation parser to associate
 * {@link ModuleSpi} instance to a {@link Module} instance.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface ModuleSpiFactory {

  /**
   * This method is used to create ModuleSpi instance base on passed
   * configuration properties.
   * 
   * @param configValues configuration property values
   * @return ModuleSpi instance.
   */
  public <T extends ModuleSpi> T create(Class<T> spiType, Map<String, ?> configValues);

  /**
   * This method is use to destroy module spi instance and to release associated
   * instance resources
   * 
   * @param moduleSpi a module spi instance which have to be destroyed
   * @return true when the operation is successes.
   */
  public boolean remove(ModuleSpi moduleSpi);
}
