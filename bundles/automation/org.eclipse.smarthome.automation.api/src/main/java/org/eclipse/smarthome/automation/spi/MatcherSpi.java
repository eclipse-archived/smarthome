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
 * An OSGi service registered with this interface is used by automation parser
 * to overwrite default matching between configuration values and corresponding
 * module spi.
 * 
 * * @author Yordan Mihaylov - Initial Contribution
 */
public interface MatcherSpi {

  /**
   * This method is matching configuration values of {@link Module} instance to
   * corresponding {@link ModuleSpi}.
   * 
   * @param configValues values for configuration properties
   * @return service PID of matched {@link ModuleSpi}
   */
  public String match(Map<String, ?> configValues);
}
