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
import org.eclipse.smarthome.automation.Input;
import org.eclipse.smarthome.automation.Output;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.automation.descriptor.ActionDescriptor;

/**
 * This interface provides common functionality for creating {@link Action}
 * instances. The actions are part of "THEN" section of the {@link Rule}. Each
 * action template is defined by unique type and {@link ActionDescriptor}. The
 * action descriptor contains meta info for {@link ConfigDescriptionParameter}s,
 * {@link Input}s and {@link Output}s of created {@link Action} instances.
 * 
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface ActionTemplate extends Template {

  /**
   * This method is used to get the descriptor of Template.
   * 
   * @return {@link ActionDescriptor} of Template.
   */
  public ActionDescriptor getDescriptor();

}
