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

package org.eclipse.smarthome.automation.parser.json;

import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
interface ModuleTypeManager {

    /**
     * This method is used to get ModuleType defined by its UID. The returned
     * module type is localized by default locale.
     *
     * @param typeUID the an unique id in scope of all registered ModuleTypes
     * @return ModuleType instance or null when module type with specified UID
     *         does not exists.
     */
    <T extends ModuleType> T getType(String typeUID);

}
