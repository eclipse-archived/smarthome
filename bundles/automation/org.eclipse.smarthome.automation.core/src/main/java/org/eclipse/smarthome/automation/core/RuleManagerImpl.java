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

package org.eclipse.smarthome.automation.core;

import java.util.Set;

import org.osgi.framework.BundleContext;

/**
 * @author Yordan Mihaylov - Initial Contribution
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.automation.core.RuleManager#storeRule(org.eclipse.smarthome.automation.core.RuleImpl)
     */
    @Override
    protected void storeRule(RuleImpl rule) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.automation.core.RuleManager#loadRules()
     */
    @Override
    protected Set<RuleImpl> loadRules() {
        // TODO Auto-generated method stub
        return null;
    }

}
