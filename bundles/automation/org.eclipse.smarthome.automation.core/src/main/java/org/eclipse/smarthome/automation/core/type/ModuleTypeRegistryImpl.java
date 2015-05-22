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
package org.eclipse.smarthome.automation.core.type;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;

/**
 * @author Yordan Mihaylov
 *
 */
public class ModuleTypeRegistryImpl extends AbstractRegistry<ModuleType, String> implements ModuleTypeRegistry {
  
  private ModuleTypeManager moduleTypeManager;

  public ModuleTypeRegistryImpl(ModuleTypeManager moduleTypeManager) {
    this.moduleTypeManager = moduleTypeManager;
  }

  /* (non-Javadoc)
   * @see org.eclipse.smarthome.core.common.registry.Registry#get(java.lang.Object)
   */
  @Override
  public ModuleType get(String key) {
    return moduleTypeManager.getType(key);
  }

  /* (non-Javadoc)
   * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#get(java.lang.String, java.util.Locale)
   */
  @Override
  public <T extends ModuleType> T get(String moduleTypeUID, Locale locale) {
    return moduleTypeManager.getType(moduleTypeUID, locale);
  }

  /* (non-Javadoc)
   * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#getByTag(java.lang.String, java.util.Locale)
   */
  @Override
  public <T extends ModuleType> Collection<T> getByTag(String moduleTypeTag, Locale locale) {
    return moduleTypeManager.getTypesByTag(moduleTypeTag, locale);
  }

  /* (non-Javadoc)
   * @see org.eclipse.smarthome.automation.type.ModuleTypeRegistry#get(java.lang.Class, java.util.Locale)
   */
  @Override
  public <T extends ModuleType> Collection<T> get(Class<T> moduleType, Locale locale) {
    return moduleTypeManager.getTypes(moduleType, locale);
  }

  /**
   * 
   */
  public void dispose() {
    moduleTypeManager.dispose();
  }


}
