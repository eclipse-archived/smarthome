/*
 * Copyright (c) 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of ProSyst Software GmbH. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ProSyst.
 */
package org.eclipse.smarthome.automation.parser.json;

import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;

/**
 * @author Yordan Mihaylov
 *
 */
public class ModuleTypeManagerImpl implements ModuleTypeManager {

  private ModuleTypeRegistry moduleTypeRegistry;

  public ModuleTypeManagerImpl(ModuleTypeRegistry mtRegistry) {
    this.moduleTypeRegistry = mtRegistry;
  }

  public <T extends ModuleType> T getType(String typeUID) {
    return moduleTypeRegistry.get(typeUID, null);
  }

}
