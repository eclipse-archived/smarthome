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
package org.eclipse.smarthome.automation.core;

import java.util.Set;

import org.osgi.framework.BundleContext;

/**
 * @author danchom
 *
 */
public class RuleManagerImpl extends RuleManager {

  /**
   * @param bc
   */
  public RuleManagerImpl(BundleContext bc) {
    super(bc);
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see org.eclipse.smarthome.automation.core.RuleManager#storeRule(org.eclipse.smarthome.automation.core.RuleImpl)
   */
  @Override
  protected void storeRule(RuleImpl rule) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.eclipse.smarthome.automation.core.RuleManager#loadRules()
   */
  @Override
  protected Set<RuleImpl> loadRules() {
    // TODO Auto-generated method stub
    return null;
  }

}
