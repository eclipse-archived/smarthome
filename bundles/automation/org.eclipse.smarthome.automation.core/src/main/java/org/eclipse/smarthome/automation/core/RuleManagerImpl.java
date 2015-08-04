/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;


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
    }

}
