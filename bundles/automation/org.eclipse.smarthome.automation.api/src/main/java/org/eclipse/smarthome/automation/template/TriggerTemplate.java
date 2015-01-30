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

import org.eclipse.smarthome.automation.Output;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.automation.descriptor.Descriptor;
import org.eclipse.smarthome.automation.descriptor.TriggerDescriptor;

/**
 * This interface provides common functionality for creating {@link Trigger}
 * instances. The triggers are part of "ON" section of the {@link Rule}. Each
 * trigger template is defined by unique type and {@link TriggerDescriptor}. The
 * trigger descriptor contains meta info for {@link ConfigDescriptionParameter}s
 * and {@link Output}s of created {@link Trigger} instances.
 * 
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface TriggerTemplate extends Template {

  /**
   * This method is used to get the descriptor of Template.
   * 
   * @return {@link Descriptor} of Template.
   */
  public TriggerDescriptor getDescriptor();

}
