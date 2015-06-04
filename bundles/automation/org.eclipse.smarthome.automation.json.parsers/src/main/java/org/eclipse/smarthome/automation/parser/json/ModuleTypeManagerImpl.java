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
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;

/**
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public class ModuleTypeManagerImpl implements ModuleTypeManager {

    private ModuleTypeRegistry moduleTypeRegistry;

    public ModuleTypeManagerImpl(ModuleTypeRegistry mtRegistry) {
        this.moduleTypeRegistry = mtRegistry;
    }

    @Override
    public <T extends ModuleType> T getType(String typeUID) {
        return moduleTypeRegistry.get(typeUID, null);
    }

}
