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

import org.eclipse.smarthome.automation.Input;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Output;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.automation.template.Template;

/**
 * Base class for all (<code>Module</code>)Descriptors and
 * {@link RuleDescriptor}.<br/>
 * It is used as a meta info holder for:
 * <ul>
 * <li>{@link Module}s - defines {@link ConfigDescriptionParameter}s,
 * {@link Input}s and {@link Output} of the module
 * <li>{@link Rule}s - defines {@link ConfigDescriptionParameter}s and
 * {@link Module}s of the {@link Rule}
 * </ul>
 * 
 * The Descriptor is defined by following properties:<br/>
 * <ul>
 * <li><code>type</code> - serves for identification of the descriptor. It is
 * unique in the RuleEngine scope.
 * <li><code>label</code> - a short (one word) user friendly description of a
 * Module/Rule.</li>
 * <li><code>description</code> - a long user friendly description of the
 * module/Rule.</li>
 * <li><code>configInfo</code> - meta info for
 * {@link ConfigDescriptionParameter}(s) of the Module/Rule.</li>
 * </ul>
 * Descriptors are used for creation {@link Template}s, {@link Module}s and
 * {@link Rule}s. They contains meta info for these objects.
 * 
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public abstract class Descriptor {

  protected Map<String, ConfigDescriptionParameter> configInfo;

  /**
   * Default constructor of Descriptor. Constructs an empty Descriptor.
   * 
   * @param configInfo is a {@link Map} of {@link ConfigDescriptionParameter}s.
   *          Each entry of the map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the
   *          {@link ConfigDescriptionParameter} ,
   *          <li><code>value</code> - {@link ConfigDescriptionParameter}
   *          instance
   *          </ul>
   */
  public Descriptor(Map<String, ConfigDescriptionParameter> configInfo) {
    this.configInfo = configInfo;
  }

  /**
   * This method is used for getting the Map with
   * {@link ConfigDescriptionParameter}s define by this descriptor.<br/>
   * 
   * @return a {@link Map} of {@link ConfigDescriptionParameter}s. Each entry of
   *         the map contains:
   *         <ul>
   *         <li><code>key</code> - the name of the
   *         {@link ConfigDescriptionParameter} ,
   *         <li><code>value</code> - {@link ConfigDescriptionParameter}
   *         instance
   *         </ul>
   */
  public Map<String, ConfigDescriptionParameter> getConfigurationInfo() {
    return configInfo;
  }

}
