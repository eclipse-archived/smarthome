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

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Input;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This interface is used to create module instances. It is registered as
 * service in OSGi registry.
 * 
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface ModuleFactory {

  /**
   * This method creates and configures {@link Trigger} instance
   * 
   * @param id unique id of {@link Module} in scope of Rule.
   * @param descriptor descriptor of the {@link Module}. It defines meta info
   *          for
   * @param configValues is a {@link Map} of configuration property values. Each
   *          entry of the map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the
   *          {@link ConfigDescriptionParameter} ,
   *          <li><code>value</code> - value of the
   *          {@link ConfigDescriptionParameter}
   *          </ul>
   * 
   * @return {@link Trigger}
   */
  public Trigger createTrigger(String id, TriggerDescriptor descriptor,
                               Map<String, ?> configValues);

  /**
   * This method creates and configures {@link Condition} instance
   * 
   * @param id unique id of {@link Module} in scope of Rule.
   * @param descriptor descriptor of the {@link Module}. It defines meta info
   *          for
   * @param config is a {@link Map} of configuration property values. Each entry
   *          of the map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the
   *          {@link ConfigDescriptionParameter} ,
   *          <li><code>value</code> - value of the
   *          {@link ConfigDescriptionParameter}
   *          </ul>
   * @param connections is a {@link Map} of input connections. Each entry of the
   *          map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the {@link Input} ,
   *          <li><code>value</code> - represents output id of the
   *          {@link Module} (in form: ModuleId.OutputName), where the input is
   *          connected to.
   *          </ul>
   * 
   * @return {@link Condition}
   */
  public Condition createCondition(String id, ConditionDescriptor descriptor,
                                   Map<String, ?> config, Map<String, String> connections);

  /**
   * This method creates and configures {@link Action} instance
   * 
   * @param id unique id of {@link Module} in scope of Rule.
   * @param descriptor descriptor of the {@link Module}. It defines meta info
   *          for
   * @param configValues is a {@link Map} of configuration property values. Each
   *          entry of the map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the
   *          {@link ConfigDescriptionParameter} ,
   *          <li><code>value</code> - value of the
   *          {@link ConfigDescriptionParameter}
   *          </ul>
   * @param connections is a {@link Map} of input connections. Each entry of the
   *          map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the {@link Input} ,
   *          <li><code>value</code> - represents output id of the
   *          {@link Module} (in form: ModuleID.OutputName), where the input is
   *          connected to.
   *          </ul>
   * 
   * @return {@link Action}
   */
  public Action createAction(String id, ActionDescriptor descriptor,
                             Map<String, ?> configValues, Map<String, String> connections);

  /**
   * This method creates and configures composite module instance
   * 
   * @param id unique id of {@link Module} in scope of Rule.
   * @param descriptor descriptor of the {@link Module}. It defines meta info
   *          for
   * @param configValues is a {@link Map} of configuration property values. Each
   *          entry of the map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the
   *          {@link ConfigDescriptionParameter} ,
   *          <li><code>value</code> - value of the
   *          {@link ConfigDescriptionParameter}
   *          </ul>
   * @param connections is a {@link Map} of input connections. Each entry of the
   *          map contains:
   *          <ul>
   *          <li><code>key</code> - the name of the {@link Input} ,
   *          <li><code>value</code> - represents output id of the
   *          {@link Module} (in form: ModuleId.OutputName), where the input is
   *          connected to.
   *          </ul>
   * 
   * @return {@link Module} of type T
   * 
   */
  public <T extends Module> T createComposite(String id,
                                              CompositeDescriptor<T> descriptor,
                                              Map<String, ?> configValues,
                                              Map<String, String> connections);

}
