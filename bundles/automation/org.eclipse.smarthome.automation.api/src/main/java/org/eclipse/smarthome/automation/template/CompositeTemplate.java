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

package org.eclipse.smarthome.automation.template;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Input;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Output;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.automation.descriptor.CompositeDescriptor;
import org.eclipse.smarthome.automation.descriptor.Descriptor;

/**
 * This interface provides common functionality for creating composite
 * {@link Module} instances. The composite modules are {@link Module}s which
 * logically combine module instances of the same type ({@link Trigger}s,
 * {@link Condition}s or {@link Action}s) and hide compositeity of configuration
 * and internal connections of participating modules. The composite module can
 * be used as any other module in the {@link Rule}. Each composite module
 * template is defined by an unique type and {@link CompositeDescriptor}. The
 * composite module descriptor contains meta info for
 * {@link ConfigDescriptionParameter}s, {@link Input}s and {@link Output}s of
 * created composite {@link Module} instances.
 * 
 * @param T type of {@link Module}. It can be {@link Trigger}, {@link Condition}
 *          s or {@link Action}s
 * 
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 *
 */
public interface CompositeTemplate<T extends Module> extends Template {

  /**
   * This method is used to get the descriptor of Template.
   * 
   * @return {@link Descriptor} of Template.
   */
  public CompositeDescriptor<T> getDescriptor();

}
