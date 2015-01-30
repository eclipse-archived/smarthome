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

import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.smarthome.automation.Input;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * CompositeDescriptor is a {@link Descriptor} for logically combined
 * {@link Module}s of the same type (Triggers, Conditions or Actions). The
 * composite module is used as a regular {@link Module} in the rule but it hides
 * internal logic and connections between participating elements. The descriptor
 * of the composite module defines {@link ConfigDescriptionParameter}s,
 * {@link Input}, {@link Output}, internal {@link Module}s and connections
 * between them .
 * 
 * @param <T> type of {@link Module}
 */
public class CompositeDescriptor<T extends Module> extends Descriptor {

  private LinkedHashSet<T> modules;
  protected Map<String, Input> inputs;
  protected Map<String, Output> outputs;

  /**
   * Creates a CompositeDescriptor with ordered set of {@link Module}s
   * 
   * @param configInfo configuration parameters for the
   *          {@link CompositeDescriptor}
   * @param modules LinkedHashSet of {@link Module}(s)
   * @param inputs map of {@link Input}s
   * @param outputs map of {@link Output}s
   */
  public CompositeDescriptor(Map<String, ConfigDescriptionParameter> configInfo,
                             LinkedHashSet<T> modules, Map<String, Input> inputs,
                             Map<String, Output> outputs) {
    super(configInfo);
    setModules(modules);
    this.inputs = inputs;
    this.outputs = outputs;

  }

  /**
   * This method is used for getting modules of the CompositeDescriptor.
   * 
   * @return ordered set of modules of this CompositeDescriptor
   */
  public LinkedHashSet<T> getModules() {
    return modules;
  }

  /**
   * This method is used for setting ordered set of modules.
   * 
   * @param modules an modules of type T.
   */
  private void setModules(LinkedHashSet<T> modules) {
    // TODO verify unique module ids
    this.modules = modules;
  }

  /**
   * This method is used for getting the Map with {@link Input}s defined by this
   * descriptor.<br/>
   * 
   * @return a {@link Map} of {@link Input}s. Each entry of the map contains:
   *         <ul>
   *         <li><code>key</code> - the name of the {@link Input} ,
   *         <li><code>value</code> - {@link Input} instance of the composite
   *         module. This {@link Input} is input of some of the participating
   *         modules in the composite module.
   *         </ul>
   */
  public Map<String, Input> getInputs() {
    return inputs;
  }

  /**
   * This method is used for getting the Map with {@link Output}s defined by
   * this descriptor.<br/>
   * 
   * @return a {@link Map} of {@link Output}s. Each entry of the map contains:
   *         <ul>
   *         <li><code>key</code> - the name of the {@link Output} ,
   *         <li><code>value</code> - {@link Output} instance of the composite
   *         module. This {@link Output} is output of some of the participating
   *         modules in the composite module.
   *         </ul>
   */
  public Map<String, Output> getOutputs() {
    return outputs;
  }

}
