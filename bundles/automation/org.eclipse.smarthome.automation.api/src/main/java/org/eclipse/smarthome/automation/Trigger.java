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

package org.eclipse.smarthome.automation;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.automation.descriptor.TriggerDescriptor;

/**
 * Trigger module is used in the 'ON' section of {@link Rule} definition. It
 * defines what triggers the {@link Rule} (what starts Rule execution). Building
 * elements of trigger ({@link ConfigDescriptionParameter}s and {@link Output}s)
 * are defined by {@link TriggerDescriptor} Trigger don't have {@link Input}
 * elements.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface Trigger extends Module {

  public TriggerDescriptor getDescriptor();
}
